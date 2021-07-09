package top.warmthdawn.emss.features.server

import org.ktorm.database.Database
import org.ktorm.dsl.from
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import top.warmthdawn.emss.database.entity.Image
import top.warmthdawn.emss.database.entity.Images.tag
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.Servers
import top.warmthdawn.emss.features.server.dto.DTOImageInfo
import top.warmthdawn.emss.features.server.dto.DTOServerInfo
import java.time.LocalDate
import java.time.LocalDateTime

/**
 *
 * @author sunday7994
 * @date 2021/7/9
 */

class ServerService(
    val db: Database
) {
    suspend fun getServerInfo(): List<Server> {
        return db.from(Servers)
            .select()
            .map { Servers.createEntity(it) }
    }

    suspend fun updateServerInfo(dtoServerInfo: DTOServerInfo, dtoImageInfo: DTOImageInfo){
        val server = Server{
            name = dtoServerInfo.name
            aliasName = if(dtoServerInfo.aliasName != null) dtoServerInfo.aliasName else aliasName
            abbr = dtoServerInfo.abbr
            location = dtoServerInfo.location
            startCommand = dtoServerInfo.startCommand
            createDate = LocalDateTime.now()
            lastCrashDate = null
            lastStartDate = null
            image = Image{
                name = dtoImageInfo.name
                tag = dtoImageInfo.tag
            }
            containerPort = dtoServerInfo.containerPort
            hostPort = dtoServerInfo.hostPort



        }
    }

    suspend fun create
}