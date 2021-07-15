package top.warmthdawn.emss.features.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.model.*
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.core.InvocationBuilder.AsyncResultCallback
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient
import org.slf4j.LoggerFactory
import top.warmthdawn.emss.features.docker.dto.ContainerInfo
import top.warmthdawn.emss.features.docker.dto.ImageMoreInfo
import top.warmthdawn.emss.features.docker.timerTask.StatsTimerTask
import top.warmthdawn.emss.features.docker.timerTask.TimerTaskInfo
import top.warmthdawn.emss.features.docker.vo.ImageStatus
import top.warmthdawn.emss.features.server.vo.*
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream
import java.time.Duration
import java.util.*


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
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build()

        dockerClient = DockerClientImpl.getInstance(clientConfig, httpClient)
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
            });

    }

    // 创建容器
    fun createContainer(
        containerName: String, imageName: String,
        portBinding: MutableList<PortBinding>,
        volumeBind: MutableList<Bind>,workingDir: String, cmd: MutableList<String>,
    ): String? {

        val container = dockerClient.createContainerCmd(imageName)
            .withName(containerName)
            .withHostConfig(
                HostConfig.newHostConfig()
                    .withBinds(volumeBind).withPortBindings(portBinding)
            )
            .withCmd(cmd)
            .withWorkingDir(workingDir)
            .withAttachStdin(true)
            .withStdinOpen(true)
            .exec()

        return container.id
    }


    // 开启容器
    fun startContainer(containerId: String): Boolean {
        return try {
            dockerClient
                .startContainerCmd(containerId)
                .exec()
            true
        } catch (e: Exception) {
            false
        }
    }

    // 关闭容器
    fun stopContainer(containerId: String): Boolean {
        return try {
            dockerClient
                .stopContainerCmd(containerId)
                .exec()
            true
        } catch (e: Exception) {
            false
        }
    }


    // 获取镜像信息
    fun inspectImage(imageId: String): ImageMoreInfo? {

        return try {
            val image = dockerClient
                .inspectImageCmd(imageId)
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
    fun inspectContainer(containerId: String): ContainerInfo? {

        return try {
            val container = dockerClient
                .inspectContainerCmd(containerId)
                .exec()

            ContainerInfo(
                container.id,
                container.name,
                container.created,
                container.imageId,
                when (container.state.status) {
                    "running" -> ContainerStatus.Running
                    "created",
                    "exited",
                    -> ContainerStatus.Stopped
                    "paused",
                    "restarting",
                    "removing",
                    "dead",
                    -> ContainerStatus.Unknown
                    else -> ContainerStatus.Unknown
                }
            )
        } catch (e: Exception) {
            null
        }

    }

    // 监控状态
    fun stats(containerId: String,
              cpuUsageVO:CpuUsageVO,
              memoryUsageVO: MemoryUsageVO,
              diskVO: DiskVO, //TODO 磁盘监控
              networkVO: NetworkVO,
              period: Long,
              timestampMax: Int
    ) {

        // TODO 上层判断服务器是否启动
        val timerTaskInfo = TimerTaskInfo(
            cpuUsageVO, memoryUsageVO, diskVO, networkVO,
            mutableListOf(), mutableListOf(),
            0, mutableMapOf()
        )

        Timer().schedule(StatsTimerTask(timerTaskInfo,timestampMax), Date(), period)

        dockerClient
            .statsCmd(containerId)
            .exec<AsyncResultCallback<Statistics>>(object : AsyncResultCallback<Statistics>() {

                override fun onNext(statistics: Statistics?) {

                    if (statistics != null) {

                        with(statistics) {

                            timerTaskInfo.cpuUsageList.add(
                                (cpuStats.cpuUsage!!.totalUsage!! - preCpuStats.cpuUsage!!.totalUsage!!) * 1.0
                                        / (cpuStats.systemCpuUsage!! - (if (preCpuStats.systemCpuUsage == null) 0 else preCpuStats.systemCpuUsage)!!)
                                        * cpuStats.onlineCpus!! * 100)

                            timerTaskInfo.memoryUsageList.add(memoryStats.usage!! - memoryStats.stats!!.cache!!)

                            timerTaskInfo.availableMemory = memoryStats.limit!!


                            for (key in networks!!.keys) {
                                if (!(timerTaskInfo.networkNew.keys.contains(key))) {
                                    timerTaskInfo.networkNew[key] = EachNetworkForSecond(
                                        mutableListOf(), mutableListOf()
                                    )
                                }
                                timerTaskInfo.networkNew[key]!!.receiveValues.add(networks!![key]!!.rxBytes!!)
                                timerTaskInfo.networkNew[key]!!.sendValues.add(networks!![key]!!.txBytes!!)
                            }

                            /* //Test
                            print("pidsStats current: " + pidsStats.current + "\n")
                            for (net in networks!!.keys) {
                                print("rxBytes: " + networks!![net]!!.rxBytes + "\n")      // √
                                print("txBytes: " + networks!![net]!!.txBytes + "\n")      // √
                            }
                            print(networks!!.keys.toString())

                            print("used memory: " + (memoryStats.usage!! - memoryStats.stats!!.cache!!) + "\n")  // √
                            print("available memory : " + memoryStats.limit + "\n")     // √

                            print("Cpu total usage: " + cpuStats.cpuUsage!!.totalUsage + "\n")
                            print("pre Cpu total usage: " + preCpuStats.cpuUsage!!.totalUsage + "\n")
                            print("system Cpu usage: " + cpuStats.systemCpuUsage + "\n")
                            print("pre system Cpu usage: " + preCpuStats.systemCpuUsage + "\n")
                            print("online cpus: " + cpuStats.onlineCpus + "\n")
                            print("cpu usage %: " +
                                    (cpuStats.cpuUsage!!.totalUsage!! - preCpuStats.cpuUsage!!.totalUsage!!) * 1.0
                                    / (cpuStats.systemCpuUsage!! - (if (preCpuStats.systemCpuUsage == null) 0 else preCpuStats.systemCpuUsage)!!)
                                    * cpuStats.onlineCpus!! * 100 + "\n")
                            print("cpu total usage: " + cpuStats.cpuUsage!!.totalUsage + "\n")
                            print("block io: " + blkioStats + "\n")
                            print("-----------------------------------------------\n")
                            */
                        }
                    }
                }
            }).awaitCompletion()
    }

    // 删除镜像
    fun removeImage(imageId: String): Boolean {

        return try {
            dockerClient
                .removeImageCmd(imageId)
                .exec()
            true
        } catch (e: Exception) {
            false
        }

    }


    // 删除容器
    fun removeContainer(containerId: String): Boolean {

        return try {
            dockerClient
                .removeContainerCmd(containerId)
                .exec()
            true
        } catch (e: Exception) {
            false
        }
    }


    // 获取容器输入输出流
    fun attachContainer(containerId: String, inputStream: InputStream, callback: (Frame)->Unit): ResultCallback.Adapter<Frame>? {

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
            }).awaitCompletion()
    }


}


