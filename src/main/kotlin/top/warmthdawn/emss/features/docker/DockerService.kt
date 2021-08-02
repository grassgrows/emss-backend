package top.warmthdawn.emss.features.docker

import com.github.dockerjava.api.model.*
import io.ebean.Database
import kotlinx.coroutines.suspendCancellableCoroutine
import org.slf4j.LoggerFactory
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.query.QImage
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.features.file.FileService
import top.warmthdawn.emss.features.server.ServerException
import top.warmthdawn.emss.features.server.ServerExceptionMsg
import top.warmthdawn.emss.features.server.dto.PortBindingDTO
import kotlin.coroutines.resume

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
class DockerService(
    private val db: Database,
    private val fileService: FileService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(DockerService::class.java)
    }
    suspend fun init() {
        try {
            DockerManager.ping()
        }catch (e: Exception) {
            logger.error("无法连接docker， 请检查你的docker并尝试重启服务器")
        }
    }
    suspend fun createContainer(serverId: Long) {
        val server = QServer(db).id.eq(serverId).findOne()!!
        val rootPath = fileService.processPath("/root/${server.location}").toString()
        val image = QImage(db).id.eq(server.imageId).findOne()!!
        val volumeBindList = server.volumeBind.map { Bind(it.key, Volume(it.value)) } +
                Bind(rootPath, Volume(server.workingDir))
//        val cmd = mutableListOf("/bin/sh", "-c", server.startCommand)
        val cmd = server.startCommand.split(' ')

        val portBinding = mutableListOf(PortBindingDTO(server.containerPort, server.hostPort))
        server.portBindings.mapTo(portBinding) { PortBindingDTO(it.value, it.key) }

        val exposedPort = portBinding.map { ExposedPort(it.hostPort) }
        val portBindingList =
            portBinding.map { PortBinding(Ports.Binding(null, it.hostPort.toString()), ExposedPort(it.containerPort)) }

        val containerId = DockerManager
            .createContainer(
                server.containerName,
                image.imageName,
                portBindingList,
                volumeBindList,
                server.workingDir,
                cmd,
                exposedPort
            )
        server.containerId = containerId
        server.update()
    }

    suspend fun getContainerId(serverId: Long): String {
        val server = QServer(db).id.eq(serverId).findOne()!!
        //inspect一次，判断容器是否存在
        DockerManager.inspectContainer(server.containerId)
        return server.containerId!!
    }

    suspend fun startContainer(serverId: Long) {
        DockerManager.startContainer(getContainerId(serverId))
    }
    suspend fun startContainer(containerId: String) {
        DockerManager.startContainer(containerId)
    }

    suspend fun stopContainer(serverId: Long) {
        DockerManager.stopContainer(getContainerId(serverId))
    }

    suspend fun tryRemoveContainer(serverId: Long) {

        val server = QServer(db).id.eq(serverId).findOne()!!
        try {
            val status = DockerManager.inspectContainer(server.containerId)
            if(status.status == ContainerStatus.Running) {
                throw ServerException(ServerExceptionMsg.SERVER_NOT_STOPPED)
            }
            DockerManager.removeContainer(getContainerId(serverId))
        }catch (e: ContainerException) {
            if(e.containerExceptionMsg != ContainerExceptionMsg.CONTAINER_NOT_FOUND) {
                throw e
            }
        }
    }

    suspend fun terminateContainer(serverId: Long) {
        DockerManager.terminateContainer(getContainerId(serverId))
    }

    suspend fun isRunning(serverId: Long): Boolean {
        return inspectContainer(serverId) == ContainerStatus.Running
    }

    suspend fun waitContainer(serverId: Long): Long {
        val containerId = getContainerId(serverId)
        val inspect = DockerManager.inspectContainer(containerId)
        if(inspect.status == ContainerStatus.Stopped) {
            return inspect.exitCode ?: 0
        }
        val result =  DockerManager.waitContainer(containerId)
        return result.statusCode?.toLong() ?: 0L
    }

    suspend fun inspectContainer(serverId: Long): ContainerStatus {
        val server = QServer(db).id.eq(serverId).findOne()!!
        return kotlin.runCatching {
            DockerManager.inspectContainer(server.containerId).status
        }.getOrDefault(ContainerStatus.Removed)
    }


}


val Server.containerName get() = "emss_container_${this.abbr}"