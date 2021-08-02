package top.warmthdawn.emss.features.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.exception.NotFoundException
import com.github.dockerjava.api.model.*
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.core.InvocationBuilder.AsyncResultCallback
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import kotlinx.coroutines.suspendCancellableCoroutine
import org.slf4j.LoggerFactory
import top.warmthdawn.emss.features.docker.dto.ContainerInfo
import top.warmthdawn.emss.features.docker.dto.ImageMoreInfo
import top.warmthdawn.emss.features.docker.vo.ImageStatus
import top.warmthdawn.emss.features.statistics.ContainerStatistics
import top.warmthdawn.emss.features.statistics.DockerStatsHelper
import top.warmthdawn.emss.utils.event.impl.newConcurrentHashSet
import java.io.Closeable
import java.io.InputStream
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * @author takanashi
 * @since 2021-07-07
 */

data class DownloadingStatus(
    var current: Long,
    var total: Long,
    var progress: Double,
)

object DockerManager {
    private const val registryUrl = "https://index.docker.io/v1/"
    val dockerClient: DockerClient
    private val log = LoggerFactory.getLogger(DockerManager::class.java)
    private val pools = Executors.newCachedThreadPool()

    private suspend fun <T> runTask(task: () -> T): T {
        return suspendCancellableCoroutine {
            val t = pools.submit {
                try {
                    val result = task()
                    it.resume(result)
                } catch (e: Throwable) {
                    it.resumeWithException(e)
                }
            }
            it.invokeOnCancellation {
                t.cancel(true)
            }
        }
    }

    // 初始化并连接Docker
    init {
        val dockerHost = if (System.getProperty("os.name").contains("Windows"))
            "npipe:////./pipe/docker_engine"
        else
            "unix:///var/run/docker.sock"
        val clientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost(dockerHost)
            .withRegistryUrl(registryUrl)
            .build()


        val httpClient = ZerodepDockerHttpClient.Builder()
            .dockerHost(clientConfig.dockerHost)
            .sslConfig(clientConfig.sslConfig)
            .maxConnections(4)
            .connectionTimeout(Duration.ofSeconds(3))
            .responseTimeout(Duration.ofSeconds(10))
            .build()


        dockerClient = DockerClientImpl.getInstance(clientConfig, httpClient)
    }

    suspend fun ping() = runTask {

        dockerClient.pingCmd().exec()
    }

    // 拉取镜像
    fun pullImage(
        repository: String, tag: String? = null,
        onStateUpdate: (ImageStatus, Map<String, DownloadingStatus>, speed: Double) -> Unit,
    ): PullImageResultCallback {

        val imageName = repository + if (!tag.isNullOrEmpty()) ":$tag" else ":latest"
        val status = mutableMapOf<String, DownloadingStatus>()


        //下载速度计算相关代码
        val deltaTime = 500
        //上次计算下载速度的时间
        var lastTime = System.currentTimeMillis()
        //上次计算时的下载总进度
        var lastDownloaded = 0L
        //下载速度
        var averageSpeed = 0.0
        var lastSpeed = -1.0
        val smoothFactor = 0.05
        val lastSpeedWeight = 0.3

        var execStatus: ImageStatus = ImageStatus.Ready


        return dockerClient
            .pullImageCmd(imageName)
            .exec<PullImageResultCallback>(object : PullImageResultCallback() {
                override fun onStart(closeable: Closeable) {
                    super.onStart(closeable)
                    log.info("开始下载镜像：$imageName")
                    execStatus = ImageStatus.Downloading
                    onStateUpdate(execStatus, status, averageSpeed)
                }

                override fun onNext(item: PullResponseItem) {
                    super.onNext(item)
                    when (item.status) {
                        "Pulling fs layer" -> status[item.id!!] = DownloadingStatus(0, 1, 0.0)
                        "Verifying Checksum", "Download complete" -> status.remove(item.status)
                        "Downloading" -> {
                            item.progressDetail!!.let {
                                status[item.id]!!.apply {
                                    current = it.current!!
                                    total = it.total!!
                                    progress = current * 1.0 / total
                                }
                            }
                            var total = 0L
                            var downloaded = 0L
                            status.values.forEach {
                                total += it.total
                                downloaded += it.current
                            }

                            val elapsedTime = System.currentTimeMillis() - lastTime
                            if (elapsedTime >= deltaTime) {
                                val speed = (downloaded - lastDownloaded) * 1000.0 / elapsedTime

                                if (lastSpeed < 0 || averageSpeed < 0) {
                                    lastSpeed = speed
                                    averageSpeed = speed
                                }

                                val weightedSpeed = lastSpeed * lastSpeedWeight + speed * (1 - lastSpeedWeight)
                                averageSpeed = averageSpeed * smoothFactor + weightedSpeed * (1 - smoothFactor)
                                lastSpeed = speed

                                lastDownloaded = downloaded
                                lastTime = System.currentTimeMillis()
                            }


                            onStateUpdate(execStatus, status, averageSpeed)

                        }

                    }

                }

                override fun onError(throwable: Throwable) {
                    super.onError(throwable)
                    log.error("镜像下载失败：$imageName", throwable.message)
                    execStatus = ImageStatus.Failed
                    onStateUpdate(execStatus, status, averageSpeed)
                }

                override fun onComplete() {
                    super.onComplete()
                    log.info("镜像下载完成：$imageName")
                    if (execStatus != ImageStatus.Failed) {
                        execStatus = ImageStatus.Downloaded
                    }
                    onStateUpdate(execStatus, status, averageSpeed)
                }
            })

    }

    // 创建容器
    suspend fun createContainer(
        containerName: String, imageName: String,
        portBinding: List<PortBinding>,
        volumeBind: List<Bind>,
        workingDir: String,
        cmd: List<String>,
        exposedPorts: List<ExposedPort>,
    ): String? = runTask {

        val container = dockerClient.createContainerCmd(imageName)
            .withName(containerName)
            .withHostConfig(
                HostConfig.newHostConfig()
                    .withBinds(volumeBind)
                    .withPortBindings(portBinding)
            )
            .withExposedPorts(exposedPorts)
            .withCmd(cmd)
            .withWorkingDir(workingDir)
            .withAttachStdin(true)
            .withStdinOpen(true)
            .exec()

        container.id
    }


    // 开启容器
    suspend fun startContainer(containerId: String) = runTask {
        dockerClient
            .startContainerCmd(containerId)
            .exec()
    }

    // 关闭容器
    suspend fun stopContainer(containerId: String) = runTask {
        dockerClient
            .stopContainerCmd(containerId)
            .withTimeout(1000 * 20)
            .exec()
    }

    // 强制关闭容器
    suspend fun terminateContainer(containerId: String) = runTask {
        dockerClient
            .killContainerCmd(containerId)
            .exec()
    }


    // 获取镜像信息
    suspend fun inspectImage(imageName: String): ImageMoreInfo? = runTask {
        try {
            val image = dockerClient
                .inspectImageCmd(imageName)
                .exec()

            ImageMoreInfo(
                image.id,
                image.created,
                image.size,
            )
        } catch (e: Exception) {
            null
        }
    }


    // 获取容器信息
    suspend fun inspectContainer(containerId: String?): ContainerInfo = runTask {
        if (containerId == null) {
            throw ContainerException(ContainerExceptionMsg.CONTAINER_GET_INFO_FAILED)
        }
        try {
            val container = dockerClient
                .inspectContainerCmd(containerId)
                .exec()
            val myDateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
            val createTime = LocalDateTime.parse(container.created, myDateTimeFormatter)
            ContainerInfo(
                container.id,
                container.name,
                createTime,
                container.imageId,
                when (container.state.status) {
                    "running" -> ContainerStatus.Running
                    "created",
                    "exited",
                    -> ContainerStatus.Stopped
                    "paused",
                    "restarting",
                    -> ContainerStatus.Unknown
                    "removing",
                    "dead",
                    -> ContainerStatus.Removed
                    else -> ContainerStatus.Unknown
                },
                container.state.exitCodeLong
            )
        } catch (e: NotFoundException) {
            throw ContainerException(ContainerExceptionMsg.CONTAINER_NOT_FOUND)
        } catch (e: Exception) {
            throw ContainerException(ContainerExceptionMsg.CONTAINER_GET_INFO_FAILED)
        }
    }


    // 监控状态
    suspend fun statsContainer(containerId: String): ContainerStatistics = suspendCancellableCoroutine {
        val callback = dockerClient
            .statsCmd(containerId)
            .withNoStream(true)
            .exec(object : ResultCallback.Adapter<Statistics>() {
                override fun onNext(result: Statistics) {

                    //CPU
                    val cpuDelta = (result.cpuStats.cpuUsage?.totalUsage ?: 0) - (result.preCpuStats.cpuUsage?.totalUsage ?: 0)
                    val systemCpuDelta = (result.cpuStats.systemCpuUsage ?: 0) - (result.preCpuStats.systemCpuUsage ?: 0)
                    val numberCpus = result.cpuStats.onlineCpus ?: result.cpuStats.cpuUsage?.percpuUsage?.size ?: 0
                    var cpuPercent = (cpuDelta * 1.0 / systemCpuDelta) * numberCpus.toDouble() * 100.0
                    if (cpuPercent > 100) {
                        cpuPercent = 100.0
                    }

                    //内存
                    val totalMemory = result.memoryStats.limit ?: 0
                    val memoryUsage =
                        (result.memoryStats.usage ?: 0) - (result.memoryStats.stats?.cache ?: 0)

                    //磁盘
                    val (diskWriteValue, diskReadValue) = DockerStatsHelper.calculateBlockIO(result.blkioStats!!)

                    //网络
                    val (networksIn, netWorksOut) = DockerStatsHelper.calculateNetwork(result.networks!!)


                    val data = ContainerStatistics(
                        cpuPercent,
                        totalMemory,
                        memoryUsage,
                        netWorksOut,
                        networksIn,
                        diskReadValue,
                        diskWriteValue,
                    )
                    it.resume(data)
                }

                override fun onError(e: Throwable) {
                    it.resumeWithException(e)
                }

                override fun onComplete() {
                    if(it.isActive) {
                        it.resumeWithException(EmptyResultException())
                    }
                }
            })

        it.invokeOnCancellation {
            callback.close()
        }
    }


    // 删除镜像
    suspend fun removeImage(imageName: String) = runTask {
        dockerClient
            .removeImageCmd(imageName)
            .exec()
    }


    // 删除容器
    suspend fun removeContainer(containerId: String) = runTask {
        dockerClient
            .removeContainerCmd(containerId)
            .exec()
    }


    // 获取容器输入输出流
    suspend fun attachContainer(
        containerId: String,
        inputStream: InputStream,
        callback: (Frame) -> Unit,
    ) = suspendCancellableCoroutine<Unit> {
        val closeable = dockerClient
            .attachContainerCmd(containerId)
            .withStdOut(true)
            .withStdErr(true)
            .withStdIn(inputStream)
            .withFollowStream(true)
            .exec<ResultCallback.Adapter<Frame>>(object : ResultCallback.Adapter<Frame>() {
                override fun onNext(frame: Frame?) {
                    super.onNext(frame)
                    frame?.let(callback)
                }
                override fun onComplete() {
                    if(it.isActive) {
                        it.resume(Unit)
                    }
                }
                override fun onError(throwable: Throwable) {
                    it.resumeWithException(throwable)
                }

            })
        it.invokeOnCancellation {
            closeable.close()
        }
    }

    suspend fun logContainer(
        containerId: String,
        callback: (Frame) -> Unit,
    ) = suspendCancellableCoroutine<Unit> {
        val closeable = dockerClient
            .logContainerCmd(containerId)
            .withTail(100)
            .withStdErr(true)
            .withStdOut(true)
            .exec<ResultCallback.Adapter<Frame>>(object : ResultCallback.Adapter<Frame>() {
                override fun onNext(frame: Frame?) {
                    super.onNext(frame)
                    frame?.let(callback)
                }

                override fun onError(throwable: Throwable) {
                    it.resumeWithException(throwable)
                }

                override fun onComplete() {
                    if(it.isActive) {
                        it.resume(Unit)
                    }
                }
            })

    }

    suspend fun waitContainer(containerId: String): WaitResponse = suspendCancellableCoroutine {
        val closeable = dockerClient
            .waitContainerCmd(containerId)
            .exec(object : ResultCallback.Adapter<WaitResponse>() {
                override fun onNext(result: WaitResponse) {
                    it.resume(result)
                }

                override fun onError(e: Throwable) {
                    it.resumeWithException(e)
                }

                override fun onComplete() {
                    if(it.isActive) {
                        it.resumeWithException(EmptyResultException())
                    }
                }
            })

        it.invokeOnCancellation {
            closeable.close()
        }
    }


}

class EmptyResultException: RuntimeException() {

}





