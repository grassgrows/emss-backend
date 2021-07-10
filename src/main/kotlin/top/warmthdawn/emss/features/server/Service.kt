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
import top.warmthdawn.emss.features.server.vo.ServerVO

/**
 *
 * @author sunday7994
 * @date 2021/7/9
 */

class ServerService(
    private val db: Database,
    private val config: AppConfig,
) {
    suspend fun getServerInfo(): List<ServerVO> {
        val list: MutableList<ServerVO> = mutableListOf()

        for(row in QServer(db).findList()){
            val serverVO = ServerVO(
                row.name,
                row.aliasName,
                row.abbr,
                row.location,
                row.startCommand,
                row.lastCrashDate,
                row.lastStartDate,
                row.imageId,
                row.containerPort,
                row.hostPort,
                row.containerId!!,
                ContainerService(db).getContainerName(row.containerId!!),
                ContainerService(db).getContainerCreateTime(row.containerId!!)!!,
                ContainerService(db).getContainerStatusEnum(row.containerId!!)!!,
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
            containerPort = serverInfoDTO.containerPort,
            hostPort = serverInfoDTO.hostPort,
        )

        val image = QImage(db).id.eq(serverInfoDTO.imageId).findOne()

        if (image == null) {
            return
        }

        val containerName = "emss_container_"+serverInfoDTO.abbr

        val bind = Bind("/data/$containerName)", Volume("/data"))
        val cmd = listOf(serverInfoDTO.startCommand)
        val id = ContainerService(db).createContainer(containerName, ((image.repository) +":"+image.tag),
            serverInfoDTO.hostPort,serverInfoDTO.containerPort,bind,cmd)


        server.containerId = id
        server.insert()

    }

    suspend fun start(id: Long) {
        if(config.testing){
            return
        }
        val containerId = QServer().id.eq(db.find(Server::class.java, id)!!.id).findOne()!!.containerId!!
        DockerManager.startContainer(containerId)
    }

    suspend fun stop(id: Long) {
        if(config.testing){
            return
        }

        val containerId = QServer().id.eq(db.find(Server::class.java, id)!!.id).findOne()!!.containerId!!
        DockerManager.stopContainer(containerId)
    }

    suspend fun restart(id: Long) {
        stop(id)
        start(id)
    }

    suspend fun terminate(id: Long) {
        if(config.testing){
            return
        }
        val containerId = QServer().id.eq(db.find(Server::class.java, id)!!.id).findOne()!!.containerId!!
        DockerManager.stopContainer(containerId)

    }

}