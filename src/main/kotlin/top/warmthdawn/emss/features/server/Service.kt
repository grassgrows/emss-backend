package top.warmthdawn.emss.features.server

import io.ebean.Database
import io.ktor.utils.io.*
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.database.entity.GroupServer
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.ServerRealTime
import top.warmthdawn.emss.database.entity.query.QGroupServer
import top.warmthdawn.emss.database.entity.query.QImage
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.database.entity.query.QServerRealTime
import top.warmthdawn.emss.features.docker.ContainerStatus
import top.warmthdawn.emss.features.docker.DockerService
import top.warmthdawn.emss.features.docker.ImageException
import top.warmthdawn.emss.features.docker.ImageExceptionMsg
import top.warmthdawn.emss.features.docker.vo.ImageStatus
import top.warmthdawn.emss.features.server.dto.PortBindingDTO
import top.warmthdawn.emss.features.server.dto.ServerInfoDTO
import top.warmthdawn.emss.features.server.dto.VolumeBindingDTO
import top.warmthdawn.emss.features.server.vo.ServerBriefVO
import top.warmthdawn.emss.features.server.vo.ServerVO
import top.warmthdawn.emss.features.settings.ImageService

/**
 *
 * @author sunday7994,takanashi
 * @date 2021/7/9
 */

class ServerService(
    private val db: Database,
    private val config: AppConfig,
    private val imageService: ImageService,
    private val dockerService: DockerService,
) {

    suspend fun getServersBriefInfo(): List<ServerBriefVO> {
        val list: MutableList<ServerBriefVO> = mutableListOf()
        for (server in QServer(db).findList()) {
            val running = QServerRealTime(db).id.eq(server.id).findOne()!!
            val isRunning = dockerService.isRunning(server.id!!)
            val result = ServerBriefVO(
                server.id!!,
                server.name,
                server.aliasName,
                server.abbr,
                isRunning,
                server.hostPort,
                server.imageId,
                running.lastCrashDate,
                running.serverPlayerNumber,
                running.serverMaxPlayer,
                running.serverTps,
            )
            list.add(result)
        }

        return list
    }


    suspend fun getServerInfo(id: Long): ServerVO {
        val server =
            QServer(db)
                .id.eq(id)
                .findOne()
                ?: throw ServerException(ServerExceptionMsg.SERVER_NOT_FOUND)

        val portBinding = mutableListOf(PortBindingDTO(server.containerPort, server.hostPort))
        server.portBindings.mapTo(portBinding) { PortBindingDTO(it.value, it.key) }

        val volumeBind = server.volumeBind.map { VolumeBindingDTO(it.value, it.key) }

        val permissionGroup =
            QGroupServer(db)
                .select(QGroupServer._alias.groupId)
                .serverId.eq(id)
                .findSingleAttributeList<Long>()

        return ServerVO(
            server.id!!,
            server.name,
            server.aliasName,
            server.abbr,
            server.location,
            server.startCommand,
            server.imageId,
            server.workingDir,
            portBinding,
            volumeBind,
            permissionGroup,
        )
    }


    suspend fun updateServerInfo(id: Long, serverInfoDTO: ServerInfoDTO) {

        val server = QServer(db).id.eq(id).findOne()
            ?: throw ServerException(ServerExceptionMsg.SERVER_NOT_FOUND)

        dockerService.tryRemoveContainer(id)

        server.name = serverInfoDTO.name
        server.aliasName = serverInfoDTO.aliasName ?: ""
        server.abbr = serverInfoDTO.abbr
        server.location = serverInfoDTO.location
        server.startCommand = serverInfoDTO.startCommand
        server.imageId = serverInfoDTO.imageId
        server.workingDir = serverInfoDTO.workingDir
        val mainBind = serverInfoDTO.portBindings.first()
        server.hostPort = mainBind.hostPort
        server.containerPort = mainBind.containerPort
        server.portBindings = serverInfoDTO.portBindings
            .subList(1, serverInfoDTO.portBindings.size - 1)
            .associate { it.hostPort to it.containerPort }
        server.volumeBind = serverInfoDTO.volumeBind
            .associate { it.hostVolume to it.containerVolume }
        server.update()

    }

    suspend fun createServerInfo(serverInfoDTO: ServerInfoDTO) {
        if (!QImage(db).id.eq(serverInfoDTO.imageId).exists())
            throw ImageException(ImageExceptionMsg.IMAGE_NOT_FOUND)
        if (!config.testing) {
            if (imageService.getImageStatus(serverInfoDTO.imageId).status != ImageStatus.Downloaded)
                throw ImageException(ImageExceptionMsg.IMAGE_NOT_DOWNLOADED)
        }
        val mainBind = serverInfoDTO.portBindings.first()
        val portBindings = serverInfoDTO.portBindings
            .subList(1, serverInfoDTO.portBindings.size - 1)
            .associate { it.hostPort to it.containerPort }
        val volumeBind = serverInfoDTO.volumeBind
            .associate { it.hostVolume to it.containerVolume }
        val server = Server(
            name = serverInfoDTO.name,
            aliasName = serverInfoDTO.aliasName ?: "",
            abbr = serverInfoDTO.abbr,
            location = serverInfoDTO.location,
            startCommand = serverInfoDTO.startCommand,
            imageId = serverInfoDTO.imageId,
            workingDir = serverInfoDTO.workingDir,
            portBindings = portBindings,
            volumeBind = volumeBind,
            containerPort = mainBind.containerPort,
            hostPort = mainBind.hostPort,
        )
        server.insert()

        val realTime = ServerRealTime(serverId = server.id!!)
        realTime.insert()


        db.insertAll(serverInfoDTO.permissionGroup.map {
            GroupServer(
                it,
                server.id!!,
            )
        })


    }

    suspend fun start(id: Long) {
        if (config.testing) {
            return
        }

        if(dockerService.inspectContainer(id) == ContainerStatus.Removed){
            dockerService.createContainer(id)
        }

        dockerService.startContainer(id)
    }

    suspend fun stop(id: Long) {
        if (config.testing) {
            return
        }
        dockerService.stopContainer(id)
    }

    suspend fun restart(id: Long) {
        stop(id)
        dockerService.waitContainer(id)
        start(id)
    }

    suspend fun terminate(id: Long) {
        if (config.testing) {
            return
        }
        dockerService.terminateContainer(id)

    }

    suspend fun removeServer(id: Long) {
        if (config.testing) {
            return
        }
        if (!QServer().id.eq(id).exists())
            throw ServerException(ServerExceptionMsg.SERVER_NOT_FOUND)

        dockerService.tryRemoveContainer(id)
        val server = QServer().id.eq(id).findOne()!!
        val groupServer = QGroupServer().id.eq(id)

        db.beginTransaction()
        try {
            val serverRealTime = QServerRealTime().id.eq(id).findOne()
            if (!server.delete() || !serverRealTime!!.delete() || groupServer.delete() <= 0)
                throw ServerException(ServerExceptionMsg.SERVER_DATABASE_REMOVE_FAILED)
            db.commitTransaction()
        } finally {
            db.endTransaction()
        }

    }
}