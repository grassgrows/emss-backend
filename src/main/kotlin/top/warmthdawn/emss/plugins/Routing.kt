package top.warmthdawn.emss.plugins

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import top.warmthdawn.emss.features.docker.dockerEndpoint
import top.warmthdawn.emss.features.server.serverEndpoint
import top.warmthdawn.emss.features.settings.settingEndpoint

fun Application.configureRouting() {
    routing {
        settingEndpoint()
        serverEndpoint()
        dockerEndpoint()
    }
}
