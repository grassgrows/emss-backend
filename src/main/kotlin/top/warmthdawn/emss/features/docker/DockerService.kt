package top.warmthdawn.emss.features.docker

import com.github.dockerjava.api.model.*
import io.ebean.Database
import top.warmthdawn.emss.database.entity.Image
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.query.QImage
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.features.file.FileService

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
class DockerService(
    private val db: Database,
    private val fileService: FileService,
) {
    fun createContainer(serverId: Long) {
        val server = QServer(db).id.eq(serverId).findOne()!!
        val rootPath = fileService.processPath("/root/${server.location}").toString()
        val image = QImage(db).id.eq(server.imageId).findOne()!!
        val portBindingList = server.portBindings.map { PortBinding(Ports.Binding(null, it.key.toString()), ExposedPort(it.value)) }
        val volumeBindList = server.volumeBind.map { Bind(it.key,Volume(it.value)) } +
                Bind(rootPath, Volume(server.workingDir))
        val cmd = mutableListOf("/bin/sh", "-c", server.startCommand)
        val exposedPort = server.portBindings.keys.map { ExposedPort(it) }
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

    fun getContainerId(serverId: Long): String {
        val server = QServer(db).id.eq(serverId).findOne()!!
        //inspect一次，判断容器是否存在
        DockerManager.inspectContainer(server.containerId)
        return server.containerId!!
    }

    fun startContainer(serverId: Long) {
        DockerManager.startContainer(getContainerId(serverId))
    }

    fun stopContainer(serverId: Long) {
        DockerManager.stopContainer(getContainerId(serverId))
    }

    fun removeContainer(serverId: Long) {
        DockerManager.removeContainer(getContainerId(serverId))
    }

    fun terminateContainer(serverId: Long) {
        DockerManager.stopContainer(getContainerId(serverId))
    }

    

}


val Server.containerName get() = "emss_container_${this.abbr}"