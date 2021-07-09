package top.warmthdawn.emss.features.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.exception.InternalServerErrorException
import com.github.dockerjava.api.exception.NotFoundException
import com.github.dockerjava.api.model.*
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import org.slf4j.LoggerFactory
import top.warmthdawn.emss.features.docker.vo.ImageStatus
import top.warmthdawn.emss.features.docker.dto.ContainerInfo
import java.io.Closeable
import java.time.Duration


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

        val httpClient = ApacheDockerHttpClient.Builder()
            .dockerHost(clientConfig.dockerHost)
            .sslConfig(clientConfig.sslConfig)
            .maxConnections(1)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build()

        dockerClient = DockerClientImpl.getInstance(clientConfig, httpClient)
    }

    fun pullImage(
        repository: String, tag: String? = null,
        onStateUpdate: (ImageStatus, Map<String, DownloadingStatus>, speed: Double) -> Unit
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
        var averageSpeed = -1.0
        var lastSpeed = -1.0
        val smoothFactor = 0.05
        val lastSpeedWeight = 0.3

        var execStatus: ImageStatus = ImageStatus.Unknown


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

                                val process = downloaded * 1.0 / total
                                log.debug("下载进度：${process * 100}%, 下载速度：${speed / (1024 * 1024)}MB/S")

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
                    if(execStatus != ImageStatus.Failed){
                        execStatus = ImageStatus.Downloaded
                    }
                    onStateUpdate(execStatus, status, averageSpeed)
                }
            });

    }

    // 创建容器
    fun createContainer(
        containerName: String, imageName: String,
        hostIp: String, hostPortId: Int, exposedPortId: Int,
        bind: Bind, cmd: List<String>
    ): String {

        val exposedPort = ExposedPort(exposedPortId)
        val binding = Ports.Binding(hostIp, hostPortId.toString())
        val portBinding = PortBinding(binding, exposedPort)
        val container = dockerClient.createContainerCmd(imageName)
            .withName(containerName)
            .withExposedPorts()
            .withHostConfig(
                HostConfig.newHostConfig()
                    .withBinds(bind).withPortBindings(portBinding)
            )
            .withCmd(cmd)
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
                container.state.status
            )
        } catch (e: Exception) {
            null
        }
    }
}


