package top.warmthdawn.emss.features.server

import io.ebean.Database
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QImage
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.features.docker.ContainerService
import top.warmthdawn.emss.features.docker.DockerManager
import top.warmthdawn.emss.features.docker.vo.ContainerStatus
import top.warmthdawn.emss.features.server.dto.ServerInfoDTO
import top.warmthdawn.emss.features.server.vo.ServerVO
import java.io.File
import java.time.LocalDateTime

/**
 *
 * @author sunday7994,takanashi
 * @date 2021/7/9
 */

class ServerService(
    private val db: Database,
    private val config: AppConfig,
    private val containerService: ContainerService,
) {

    suspend fun getServerInfo(): List<ServerVO> {
        val list: MutableList<ServerVO> = mutableListOf()
        for (row in QServer(db).findList()) {
            val serverVO = ServerVO(
                row.id!!,
                row.name,
                row.aliasName,
                row.abbr,
                row.location,
                row.startCommand,
                row.lastCrashDate,
                row.lastStartDate,
                row.imageId,
                row.portBindings,
                row.volumeBind,
                row.containerId,
                containerService.getContainerName(row.containerId),
                containerService.getContainerCreateTime(row.containerId),
                containerService.getContainerStatusEnum(row.containerId),
            )
            list.add(serverVO)
        }

        return list
    }

    suspend fun createServerInfo(serverInfoDTO: ServerInfoDTO) {
        val server = Server(
            name = serverInfoDTO.name,
            aliasName = serverInfoDTO.aliasName ?: "",
            abbr = serverInfoDTO.abbr,
            location = serverInfoDTO.location,
            startCommand = serverInfoDTO.startCommand,
            imageId = serverInfoDTO.imageId,
            workingDir = serverInfoDTO.workingDir,
            portBindings = serverInfoDTO.portBindings,
            volumeBind = serverInfoDTO.volumeBind,
        )

        QImage(db).id.eq(serverInfoDTO.imageId).findOne() ?: return

        server.insert()

    }

    suspend fun start(id: Long) {
        if (config.testing) {
            return
        }
        val server = QServer(db).id.eq(id).findOne()!!

        //TODO: 服务器编辑只会，要删除
        if (server.containerId == null) {

            val containerName = "emss_container_" + server.abbr

//            val bind = Bind(QSetting(db).type.eq(SettingType.ServerRootDirectory).findOne()!!.value+server.location,Volume("/data/"))
            val image = QImage().id.eq(server.imageId).findOne()!!

            val volumeBind = mutableMapOf<String,String>()
            volumeBind.putAll(server.volumeBind)
            val rootPath = QSetting(db).type.eq(SettingType.ServerRootDirectory).findOne()!!.value+ File.separator+server.location
            print(rootPath)
            volumeBind[rootPath] = server.workingDir
            val id = ContainerService(db).createContainer(
                containerName,
                image.imageId,
                server.portBindings,
                volumeBind,
                server.workingDir,
                server.startCommand
            )
            server.containerId = id
        }

        val containerId = server.containerId!!
        if(ContainerService(db).getContainerStatusEnum(containerId) != ContainerStatus.Stopped)
        {
            // TODO 不可重复启动
            return
        }
        DockerManager.startContainer(containerId)

        server.lastStartDate = LocalDateTime.now()
        server.update()

    }

    suspend fun stop(id: Long) {
        if (config.testing) {
            return
        }
        val server = QServer(db).id.eq(id).findOne()!!
        val containerId = server.containerId!!
        DockerManager.stopContainer(containerId)
        server.lastStartDate = LocalDateTime.now()
        server.update()
    }

    suspend fun restart(id: Long) {
        stop(id)
        start(id)
    }

    suspend fun terminate(id: Long) {
        if (config.testing) {
            return
        }
        val containerId = QServer().id.eq(db.find(Server::class.java, id)!!.id).findOne()!!.containerId!!
        DockerManager.stopContainer(containerId)

    }

}