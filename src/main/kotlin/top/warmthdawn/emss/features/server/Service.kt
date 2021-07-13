package top.warmthdawn.emss.features.server

import io.ebean.Database
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QImage
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.database.entity.query.QServerRealTime
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.features.docker.*
import top.warmthdawn.emss.features.docker.ContainerStatus
import top.warmthdawn.emss.features.docker.vo.ImageStatus
import top.warmthdawn.emss.features.server.dto.ServerInfoDTO
import top.warmthdawn.emss.features.server.vo.ServerVO
import top.warmthdawn.emss.features.settings.ImageService
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
    private val imageService: ImageService
) {

    suspend fun getServerInfo(): List<ServerVO> {
        val list: MutableList<ServerVO> = mutableListOf()
        for (i in QServer(db).findList().indices) {
            val server = QServer(db).findList().elementAt(i)
            val serverRealTime = QServerRealTime(db).findList().elementAt(i)
            val serverVO = ServerVO(
                server.id!!,
                server.name,
                server.aliasName,
                server.abbr,
                server.location,
                server.startCommand,
                server.imageId,
                server.workingDir,
                server.portBindings,
                server.volumeBind,
                server.containerId,
                containerService.getContainerName(server.containerId),
                containerService.getContainerCreateTime(server.containerId),
                containerService.getContainerStatus(server.containerId),
                serverRealTime.lastCrashDate,
                serverRealTime.lastStartDate,
                serverRealTime.status
            )
            list.add(serverVO)
        }

        return list
    }

    suspend fun createServerInfo(serverInfoDTO: ServerInfoDTO) {
        if(QImage(db).id.eq(serverInfoDTO.imageId).findOne() == null)
            throw ImageNotFoundException("查无对应镜像！")  //TODO 异常
        if(imageService.getImageStatus(serverInfoDTO.imageId).status != ImageStatus.Downloaded)
            throw ImageNotDownloadedException("镜像未下载！")

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
        server.insert()
    }

    suspend fun start(id: Long) {
        if (config.testing) {
            return
        }
        val server = QServer(db).id.eq(id).findOne()!!
        val serverRealTime = QServerRealTime(db).id.eq(id).findOne()!!

        //TODO: 服务器编辑只会，要删除
        if (server.containerId == null) {

//            val bind = Bind(QSetting(db).type.eq(SettingType.ServerRootDirectory).findOne()!!.value+server.location,Volume("/data/"))

            val containerName = "emss_container_" + server.abbr
            val image = QImage().id.eq(server.imageId).findOne()!!

            val volumeBind = mutableMapOf<String,String>()
            volumeBind.putAll(server.volumeBind)
            //TODO Path
            val rootPath = QSetting(db).type.eq(SettingType.ServerRootDirectory).findOne()!!.value+ File.separator+server.location
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
        if(ContainerService(db).getContainerStatus(containerId) == ContainerStatus.Running
           && serverRealTime.status == ServerStatus.Running)
        {
            // TODO 不可重复启动
            return
        }
        DockerManager.startContainer(containerId)

        serverRealTime.lastStartDate = LocalDateTime.now()
        server.update()
        serverRealTime.update()

    }

    suspend fun stop(id: Long) {
        if (config.testing) {
            return
        }
        val server = QServer(db).id.eq(id).findOne()!!
        val serverRealTime = QServerRealTime(db).id.eq(id).findOne()!!
        val containerId = server.containerId!!
        DockerManager.stopContainer(containerId)
        serverRealTime.lastStartDate = LocalDateTime.now()
        serverRealTime.update()
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