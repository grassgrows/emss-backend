package top.warmthdawn.emss.features.docker

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.model.*
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientImpl
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.time.Duration
import kotlin.coroutines.resume

class DockerManager(
    //registryUser: String,
    //registryPass: String,
    //registryMail: String,
    registryUrl: String
) {
    private val dockerClient: DockerClient

    // 初始化并连接Docker
    init {
        val dockerHost = if (System.getProperty("os.name").contains(Regex("Windows")))
            "npipe:////./pipe/docker_engine"
        else
            "unix:///var/run/docker.sock"
        val clientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost(dockerHost)
//            .withDockerTlsVerify(docker_tls_verity)
//            .withDockerCertPath(docker_cert_path)
            //.withRegistryUsername(registryUser)
            //.withRegistryPassword(registryPass)
            //.withRegistryEmail(registryMail)
            .withRegistryUrl(registryUrl)
            .build()

        val httpClient = ApacheDockerHttpClient.Builder()
            .dockerHost(clientConfig.getDockerHost())
            .sslConfig(clientConfig.getSSLConfig())
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build()

        dockerClient = DockerClientImpl.getInstance(clientConfig, httpClient)
    }

    // 拉取镜像
    suspend fun pullImage(imageName: String)
    {
        suspendCancellableCoroutine<Boolean> {
            val callback = dockerClient
                .pullImageCmd(imageName)
                .start()

            callback.awaitCompletion()
            it.resume(true)
        }
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

    // 重启容器
    fun restartContainer(containerName: String) {
        dockerClient
            .restartContainerCmd(containerName)
            .exec()
    }


    //region 废弃功能
    /*
    fun createDockerfile(path: String, text: String) {
        val file = File(path)
        file.writeText(text)
    }

    // 根据Dockerfile构建镜像
    fun buildImage(dockerfile: String, imageName: String): String {


        val imageid = dockerClient.buildImageCmd()
            .withDockerfilePath(dockerfile)
            .withTags(setOf(imageName))
            .start()
            .awaitImageId()


        return imageid
    }

     */
    //endregion
}