package top.warmthdawn.emss.features.server

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.server.dto.DTOImageInfo
import top.warmthdawn.emss.features.server.dto.DTOServerInfo

/**
 *
 * @author sunday7994
 * @date 2021/7/9
 */

fun Route.settingEndpoint() {

    val serverService by inject<ServerService>()
    route("/server"){
        get("/info") {
            call.respond(serverService.getServerInfo())
        }
        post("/create"){
            val dtoServerInfo = call.receive<DTOServerInfo>()
            val dtoImageInfo = call.receive<DTOImageInfo>()
            serverService.updateServerInfo(dtoServerInfo, dtoImageInfo)

        }
    }


}