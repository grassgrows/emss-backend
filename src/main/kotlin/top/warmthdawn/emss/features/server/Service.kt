package top.warmthdawn.emss.features.server

import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.Volume
import io.ebean.Database
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.query.QImage
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.features.docker.ContainerService
import top.warmthdawn.emss.features.docker.DockerManager
import top.warmthdawn.emss.features.server.dto.ServerInfoDTO

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

        val image = QImage(db).id.eq(serverInfoDTO.imageId).findOne()

        if (image == null) {
            // TODO 返回不存在本地镜像
            return
        }




        val hostIp = "主机IP地址"
        val bind = Bind("/data/$serverInfoDTO.name)", Volume("/data"))
        val cmd = listOf(serverInfoDTO.startCommand)
        ContainerService(db).createContainer(serverInfoDTO.name, ((image.repository) +":"+image.tag),
            hostIp,serverInfoDTO.hostPort,serverInfoDTO.containerPort,bind,cmd)



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