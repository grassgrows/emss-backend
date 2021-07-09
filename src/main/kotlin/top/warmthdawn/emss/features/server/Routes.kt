package top.warmthdawn.emss.features.server

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.server.dto.ServerInfoDTO

/**
 *
 * @author sunday7994
 * @date 2021/7/9
 */

fun Route.settingEndpoint() {

    val serverService by inject<ServerService>()
    route("/server"){
        get("/list") {
            call.respond(serverService.getServerInfo())
        }
        post("/create") {
            val dtoServerInfo = call.receive<ServerInfoDTO>()
            serverService.createServerInfo(dtoServerInfo)
        }
    }


}