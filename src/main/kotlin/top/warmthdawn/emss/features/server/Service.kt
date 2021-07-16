package top.warmthdawn.emss.features.server

import io.ebean.Database
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.ServerRealTime
import top.warmthdawn.emss.database.entity.query.QImage
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.database.entity.query.QServerRealTime
import top.warmthdawn.emss.features.docker.*
import top.warmthdawn.emss.features.docker.ContainerStatus
import top.warmthdawn.emss.features.docker.vo.ImageStatus
import top.warmthdawn.emss.features.dockerStats.*
import top.warmthdawn.emss.features.file.FileService
import top.warmthdawn.emss.features.server.dto.ServerInfoDTO
import top.warmthdawn.emss.features.server.vo.ServerVO
import top.warmthdawn.emss.features.settings.ImageService
import java.time.LocalDateTime

/**
 *
 * @author sunday7994,takanashi
 * @date 2021/7/9
 */

class ServerService(
    private val db: Database,
    private val config: AppConfig,
    private val imageService: ImageService,
    private val fileService: FileService,
    private val statsService: StatsService
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
                if (server.containerId != null) DockerManager.inspectContainer(server.containerId!!).name else null,
                if (server.containerId != null) DockerManager.inspectContainer(server.containerId!!).createTime else null,
                if (server.containerId != null) DockerManager.inspectContainer(server.containerId!!).status else ContainerStatus.Unknown,
                serverRealTime.lastCrashDate,
                serverRealTime.lastStartDate,
                serverRealTime.status
            )
            list.add(serverVO)
        }

        return list
    }

    suspend fun getServerInfo(id: Long): ServerVO {
        val server = QServer(db).id.eq(id).findOne()
        val serverRealTime = QServerRealTime(db).id.eq(id).findOne()
        if (server == null || serverRealTime == null)
            throw ServerException(ServerExceptionMsg.SERVER_NOT_FOUND)
        return ServerVO(
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
            if (server.containerId != null) DockerManager.inspectContainer(server.containerId!!).name else null,
            if (server.containerId != null) DockerManager.inspectContainer(server.containerId!!).createTime else null,
            if (server.containerId != null) DockerManager.inspectContainer(server.containerId!!).status else ContainerStatus.Unknown,
            serverRealTime.lastCrashDate,
            serverRealTime.lastStartDate,
            serverRealTime.status
        )
    }

    suspend fun createServerInfo(serverInfoDTO: ServerInfoDTO) {
        if (!QImage(db).id.eq(serverInfoDTO.imageId).exists())
            throw ImageException(ImageExceptionMsg.IMAGE_NOT_FOUND)
        if (imageService.getImageStatus(serverInfoDTO.imageId).status != ImageStatus.Downloaded)
            throw ImageException(ImageExceptionMsg.IMAGE_NOT_DOWNLOADED)

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

        val serverRealTime = ServerRealTime(
            status = ServerStatus.Stopped
        )
        serverRealTime.insert()

        //创建监控信息
        statsService.serverStatsInfoMap[server.id!!] = ServerStatsInfo(
            CpuUsage(mutableListOf(), mutableListOf(), 0.0),
            MemoryUsage(mutableListOf(), mutableListOf(), 0, 0),
            Disk(mutableListOf(), mutableListOf(), mutableListOf(),0,0),
            Network(mutableListOf(), mutableMapOf())
        )

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
            server.volumeBind.forEach {
                volumeBind.put(fileService.processPath(it.key).toString(), it.value)
            }
            val rootPath = fileService.processPath("/root/${server.location}").toString()
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
        if(DockerManager.inspectContainer(containerId).status == ContainerStatus.Running
           && serverRealTime.status == ServerStatus.Running)
        {
            // TODO 不可重复启动
            return
        }
        DockerManager.startContainer(containerId)

        serverRealTime.lastStartDate = LocalDateTime.now()
        serverRealTime.status = ServerStatus.Running
        server.update()
        serverRealTime.update()

        //开始监控
        statsService.startStats(id,60000,60)
    }

    suspend fun stop(id: Long) {
        if (config.testing) {
            return
        }
        //停止监控
        statsService.serverStatsProxy[id]!!.callback.close()
        val server = QServer(db).id.eq(id).findOne()!!
        val serverRealTime = QServerRealTime(db).id.eq(id).findOne()!!
        val containerId = server.containerId!!
        DockerManager.stopContainer(containerId)
        serverRealTime.lastStartDate = LocalDateTime.now()
        serverRealTime.status = ServerStatus.Stopped
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

    suspend fun removeServer(id: Long)
    {
        if (config.testing) {
            return
        }
        if(!QServer().id.eq(id).exists())
            throw ServerException(ServerExceptionMsg.SERVER_NOT_FOUND)

        val server = QServer().id.eq(id).findOne()
        if(server!!.containerId != null) {
            if (DockerManager.inspectContainer(server.containerId!!).status == ContainerStatus.Running)
                stop(id)

            try {
                DockerManager.removeContainer(server.containerId!!)
            } catch (e: Exception) {
                throw ServerException(ServerExceptionMsg.SERVER_REMOVE_FAILED)
            }
        }

        //删除监控信息
        statsService.serverStatsInfoMap.remove(server.id)

        val serverRealTime = QServerRealTime().id.eq(id).findOne()
        if(!server.delete()||!serverRealTime!!.delete())
            throw ServerException(ServerExceptionMsg.SERVER_DATABASE_REMOVE_FAILED)



    }





}