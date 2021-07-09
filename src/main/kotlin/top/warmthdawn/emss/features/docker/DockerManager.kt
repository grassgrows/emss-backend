package top.warmthdawn.emss.features.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.model.*
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.time.Duration
import javax.swing.text.html.HTML

object DockerManager {
    val registryUrl = "https://index.docker.io/v1/"
    private val dockerClient: DockerClient
    private val log = LoggerFactory.getLogger(DockerManager::class.java)

    // 初始化并连接Docker
    init {
        val dockerHost = if (System.getProperty("os.name").contains(Regex("Windows")))
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

    private data class DownloadingStatus(
        var current: Long,
        var total: Long
    )

    fun pullImage(repository: String, tag: String? = null): PullImageResultCallback {

        val imageName = repository + if (!tag.isNullOrEmpty()) ":$tag" else ":latest"
        val status = mutableMapOf<String, DownloadingStatus>()
        var time: Long? = null
        var schedule: Double
        var lastCurrent: Long = 0


        val callback = dockerClient
            .pullImageCmd(imageName)
            .exec(object : PullImageResultCallback() {
                override fun onStart(closeable: Closeable) {
                    super.onStart(closeable)
                    log.info("开始下载镜像：$imageName")
                }

                override fun onNext(pullResponseItem: PullResponseItem) {
                    super.onNext(pullResponseItem)
                    when (pullResponseItem.status!!) {
                        "Pulling fs layer" -> status[pullResponseItem.id!!] = DownloadingStatus(0, 1)
                        "Verifying Checksum", "Download complete" -> status.remove(pullResponseItem.status)
                        "Downloading" -> {
                            status[pullResponseItem.id]!!.current = pullResponseItem.progressDetail!!.current!!
                            status[pullResponseItem.id]!!.total = pullResponseItem.progressDetail!!.total!!
                            var allTotal = 0.00
                            var allCurrent = 0L
                            status.values.forEach {
                                allTotal += it.total
                                allCurrent += it.current
                            }
                            schedule = allCurrent / allTotal

                            if (time != null) {
                                val thisTime = System.currentTimeMillis()
                                val speed = (allCurrent - lastCurrent) / (thisTime - time!!)
                                lastCurrent = allCurrent
                                log.info("下载进度：${schedule * 100}%, 下载速度：${speed * 1000.0 / (1024 * 1024 * 8)}MB/S")
                            }
                            time = System.currentTimeMillis()
                        }
                    }
                }

                override fun onError(throwable: Throwable) {
                    super.onError(throwable)
                    log.error("镜像下载失败：$imageName", throwable.message)

                }

                override fun onComplete() {
                    super.onComplete()
                    log.info("镜像下载完成：$imageName")
                }
            })

        return callback;

    }

    // 创建容器(需添加启动命令)
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
    fun startContainer(containerName: String) {
        dockerClient
            .startContainerCmd(containerName)
            .exec()
    }

    // 关闭容器
    fun stopContainer(containerName: String) {
        dockerClient
            .stopContainerCmd(containerName)
            .exec()
    }
}

