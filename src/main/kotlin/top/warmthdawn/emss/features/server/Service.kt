package top.warmthdawn.emss.features.server

import io.ebean.Database
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.database.entity.GroupServer
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.ServerRealTime
import top.warmthdawn.emss.database.entity.query.QGroupServer
import top.warmthdawn.emss.database.entity.query.QImage
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.database.entity.query.QServerRealTime
import top.warmthdawn.emss.features.docker.DockerService
import top.warmthdawn.emss.features.docker.ImageException
import top.warmthdawn.emss.features.docker.ImageExceptionMsg
import top.warmthdawn.emss.features.docker.vo.ImageStatus
import top.warmthdawn.emss.features.server.dto.ServerInfoDTO
import top.warmthdawn.emss.features.server.entity.ServerState
import top.warmthdawn.emss.features.server.vo.ServerBriefVO
import top.warmthdawn.emss.features.server.vo.ServerVO
import top.warmthdawn.emss.features.settings.ImageService
import top.warmthdawn.emss.features.statistics.impl.StatisticsService
import top.warmthdawn.emss.utils.server.ServerInstanceHolder

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
    private val statisticsService: StatisticsService,
    private val serverInstanceHolder: ServerInstanceHolder,
) {

    suspend fun getServersBriefInfo(): List<ServerBriefVO> {
        val list: MutableList<ServerBriefVO> = mutableListOf()
        for (server in QServer(db).findList()) {
            val obj = serverInstanceHolder.getOrCreate(server.id!!)
            val running = obj.getRunningInfo()
            val result = ServerBriefVO(
                server.id!!,
                server.name,
                server.aliasName,
                server.abbr,
                obj.isRunning(),
                server.portBindings.keys.firstOrNull(),
                server.imageId,
                obj.getRunningInfo().lastCrashDate,
                groupsOfServer(server.id!!),
                running.serverPlayerNumber,
                running.serverMaxPlayer,
                running.serverTps,
            )
            list.add(result)
        }

        return list
    }

    fun getIdByAbbr(abbr: String): Long? {
        return QServer(db).abbr.eq(abbr).findIds<Long>().firstOrNull()
    }

    suspend fun getServerInfo(id: Long): ServerVO {
        val server = QServer(db).id.eq(id).findOne()
            ?: throw ServerException(ServerExceptionMsg.SERVER_NOT_FOUND)
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
            groupsOfServer(server.id!!)
        )
    }

    private fun groupsOfServer(id: Long): List<Long> {
        val result: MutableList<Long> = mutableListOf()
        for (row in QGroupServer(db).serverId.eq(id).findList()) {
            result.add(row.groupId)
        }
        return result
    }

    suspend fun updateServerInfo(id: Long, serverInfoDTO: ServerInfoDTO) {

        val server = QServer(db).id.eq(id).findOne()
            ?: throw ServerException(ServerExceptionMsg.SERVER_NOT_FOUND)
        server.name = serverInfoDTO.name
        server.aliasName = serverInfoDTO.aliasName ?: ""
        server.abbr = serverInfoDTO.abbr
        server.location = serverInfoDTO.location
        server.startCommand = serverInfoDTO.startCommand
        server.imageId = serverInfoDTO.imageId
        server.workingDir = serverInfoDTO.workingDir
        server.portBindings = serverInfoDTO.portBindings
        server.volumeBind = serverInfoDTO.volumeBind
        server.update()
        serverInstanceHolder.getOrCreate(server.id!!).reset()
    }

    suspend fun createServerInfo(serverInfoDTO: ServerInfoDTO) {
        if (!QImage(db).id.eq(serverInfoDTO.imageId).exists())
            throw ImageException(ImageExceptionMsg.IMAGE_NOT_FOUND)
        if (!config.testing) {
            if (imageService.getImageStatus(serverInfoDTO.imageId).status != ImageStatus.Downloaded)
                throw ImageException(ImageExceptionMsg.IMAGE_NOT_DOWNLOADED)
        }
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

        val realTime = ServerRealTime(serverId = server.id!!)
        realTime.insert()

        statisticsService.addServer(server.id!!, server.abbr)

        serverInfoDTO.permissionGroup.forEach {
            GroupServer(
                it,
                server.id!!,
            ).insert()
        }


    }

    suspend fun start(id: Long) {
        if (config.testing) {
            return
        }
        serverInstanceHolder.getOrCreate(id).start()
    }

    suspend fun stop(id: Long) {
        if (config.testing) {
            return
        }
        serverInstanceHolder.getOrCreate(id).stop()
    }

    suspend fun restart(id: Long) {
        stop(id)
        start(id)
    }

    suspend fun terminate(id: Long) {
        if (config.testing) {
            return
        }

        serverInstanceHolder.getOrCreate(id).terminate()

    }

    suspend fun removeServer(id: Long) {
        if (config.testing) {
            return
        }
        if (!QServer().id.eq(id).exists())
            throw ServerException(ServerExceptionMsg.SERVER_NOT_FOUND)

        val server = QServer().id.eq(id).findOne()!!
        val groupServer = QGroupServer().id.eq(id)

        kotlin.runCatching {
            dockerService.removeContainer(id)
        }
        //删除监控信息
        statisticsService.delServer(id)
        serverInstanceHolder.remove(id)
        val serverRealTime = QServerRealTime().id.eq(id).findOne()
        if (!server.delete() || !serverRealTime!!.delete() || groupServer.delete() <= 0)
            throw ServerException(ServerExceptionMsg.SERVER_DATABASE_REMOVE_FAILED)

    }
}