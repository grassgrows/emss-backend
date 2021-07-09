package top.warmthdawn.emss.features.server

import io.ebean.Database
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.query.QImage
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.features.docker.DockerManager
import top.warmthdawn.emss.features.server.dto.ServerInfoDTO
import top.warmthdawn.emss.features.server.dto.ServerVO

/**
 *
 * @author sunday7994
 * @date 2021/7/9
 */

class ServerService(
    private val db: Database,
    private val config: AppConfig,
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

    suspend fun start(id: Long) {
        if(config.testing){
            return
        }
        val imageId = QImage().id.eq(db.find(Server::class.java, id)!!.imageId).findOne()!!.imageId!!
        DockerManager.startContainer(imageId)
    }

    suspend fun stop(id: Long) {
        if(config.testing){
            return
        }

        val imageId = QImage().id.eq(db.find(Server::class.java, id)!!.imageId).findOne()!!.imageId!!
        DockerManager.stopContainer(imageId)
    }

    suspend fun restart(id: Long) {
        stop(id)
        start(id)
    }

    suspend fun terminate(id: Long) {
        if(config.testing){
            return
        }
        val imageId = QImage().id.eq(db.find(Server::class.java, id)!!.imageId).findOne()!!.imageId!!
        DockerManager.stopContainer(imageId)

    }

}