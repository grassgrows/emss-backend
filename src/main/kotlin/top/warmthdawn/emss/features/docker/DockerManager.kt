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
import org.slf4j.LoggerFactory
import top.warmthdawn.emss.features.docker.dto.ContainerInfo
import top.warmthdawn.emss.features.docker.dto.ImageMoreInfo
import top.warmthdawn.emss.features.docker.vo.ImageStatus
import top.warmthdawn.emss.features.statistics.ContainerStatistics
import top.warmthdawn.emss.utils.event.impl.newConcurrentHashSet
import java.io.Closeable
import java.io.InputStream
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit


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
    private val dockerClient: DockerClient
    private val log = LoggerFactory.getLogger(DockerManager::class.java)

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

    fun ping() {
        dockerClient.pingCmd()
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
    fun createContainer(
        containerName: String, imageName: String,
        portBinding: List<PortBinding>,
        volumeBind: List<Bind>,
        workingDir: String,
        cmd: MutableList<String>,
        exposedPorts: List<ExposedPort>,
    ): String? {

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

        return container.id
    }


    // 开启容器
    fun startContainer(containerId: String) {
        dockerClient
            .startContainerCmd(containerId)
            .exec()
    }

    // 关闭容器
    fun stopContainer(containerId: String) {
        dockerClient
            .stopContainerCmd(containerId)
            .exec()
    }

    // 强制关闭容器
    fun terminateContainer(containerId: String) {
        dockerClient
            .killContainerCmd(containerId)
            .exec()
    }


    // 获取镜像信息
    fun inspectImage(imageName: String): ImageMoreInfo? {

        return try {
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
    fun inspectContainer(containerId: String?): ContainerInfo {
        if (containerId == null) {
            throw ContainerException(ContainerExceptionMsg.CONTAINER_GET_INFO_FAILED)
        }
        try {
            val container = dockerClient
                .inspectContainerCmd(containerId)
                .exec()
            val myDateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
            val createTime = LocalDateTime.parse(container.created, myDateTimeFormatter)
            return ContainerInfo(
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
                }
            )
        } catch (e: NotFoundException) {
            throw ContainerException(ContainerExceptionMsg.CONTAINER_NOT_FOUND)
        }
        catch (e: Exception) {
            throw ContainerException(ContainerExceptionMsg.CONTAINER_GET_INFO_FAILED)
        }
    }

    // 监控状态
    fun statsContainer(containerId: String): ContainerStatistics {

        val callback = dockerClient
            .statsCmd(containerId)
            .withNoStream(true)
            .exec(AsyncResultCallback())
        val result  = callback.awaitResult()!!

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
        var diskWriteValue = 0L
        var diskReadValue = 0L
        result.blkioStats?.ioServiceBytesRecursive?.forEach { entry ->
            when (entry.op.lowercase()) {
                "read" -> {
                    diskReadValue += entry.value
                }
                "write" -> {
                    diskWriteValue += entry.value
                }
            }
        }

        //网络
        val networksIn = result.networks?.values?.asSequence()
            ?.map { net -> net.rxBytes }
            ?.filterNotNull()
            ?.reduce { a, b -> a + b }
            ?: 0


        val netWorksOut = result.networks?.values?.asSequence()
            ?.map { net -> net.txBytes }
            ?.filterNotNull()
            ?.reduce { a, b -> a + b }
            ?: 0

        callback.close()
        return ContainerStatistics(
            cpuPercent,
            memoryUsage,
            totalMemory,
            netWorksOut,
            networksIn,
            diskReadValue,
            diskWriteValue,
        )
    }


    // 删除镜像
    fun removeImage(imageName: String) {
        dockerClient
            .removeImageCmd(imageName)
            .exec()
    }


    // 删除容器
    fun removeContainer(containerId: String) {
        dockerClient
            .removeContainerCmd(containerId)
            .exec()
    }


    // 获取容器输入输出流
    fun attachContainer(
        containerId: String,
        inputStream: InputStream,
        callback: (Frame) -> Unit,
    ): ResultCallback.Adapter<Frame> {
        return dockerClient
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
            })
    }

    fun logContainer(containerId: String,
                     callback: (Frame) -> Unit,) {
        dockerClient
            .logContainerCmd(containerId)
            .withTail(100)
            .withStdErr(true)
            .withStdOut(true)
            .exec<ResultCallback.Adapter<Frame>>(object : ResultCallback.Adapter<Frame>() {
                override fun onNext(frame: Frame?) {
                    super.onNext(frame)
                    frame?.let(callback)
                }
            })
            .awaitCompletion()
    }

    fun waitContainer(containerId: String): ResultCallback<WaitResponse> {
        return dockerClient
            .waitContainerCmd(containerId)
            .exec(ResultCallback.Adapter())
            .awaitCompletion()
    }


}


