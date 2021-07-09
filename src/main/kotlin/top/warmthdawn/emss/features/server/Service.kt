package top.warmthdawn.emss.features.server

import io.ebean.Database
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.features.server.dto.ServerInfoDTO

/**
 *
 * @author sunday7994
 * @date 2021/7/9
 */

class ServerService(
    private val db: Database
) {
    suspend fun getServerInfo(): List<Server> {
        return QServer(db).findList()
    }

    suspend fun createServerInfo(serverInfoDTO: ServerInfoDTO) {
        val server = Server(
            name = serverInfoDTO.name,
            aliasName = serverInfoDTO.aliasName ?: "",
            abbr = serverInfoDTO.abbr,
            location = serverInfoDTO.location,
            startCommand = serverInfoDTO.startCommand,
            imageId = serverInfoDTO.imageId,
            containerPort = serverInfoDTO.containerPort,
            hostPort = serverInfoDTO.hostPort,
        )
        server.save()
    }

}