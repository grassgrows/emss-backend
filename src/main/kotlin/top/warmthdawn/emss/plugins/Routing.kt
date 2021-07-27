package top.warmthdawn.emss.plugins

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.request.*
import top.warmthdawn.emss.features.command.commandEndpoint
import top.warmthdawn.emss.features.compressed.compressedEndpoint
import top.warmthdawn.emss.features.docker.dockerEndpoint
import top.warmthdawn.emss.features.file.fileEndpoint
import top.warmthdawn.emss.features.permission.permissionEndpoint
import top.warmthdawn.emss.features.login.loginEndpoint
import top.warmthdawn.emss.features.server.serverEndpoint
import top.warmthdawn.emss.features.settings.settingEndpoint
import top.warmthdawn.emss.features.statistics.statisticsEndpoint
import top.warmthdawn.emss.features.system.systemEndpoint

fun Application.configureRouting() {
    val routing = routing {

        authenticate("auth-jwt") {
            settingEndpoint()
            serverEndpoint()
            dockerEndpoint()
            fileEndpoint()
            commandEndpoint()
            permissionEndpoint()
            statisticsEndpoint()
            systemEndpoint()
            compressedEndpoint()
        }
        loginEndpoint()
    }
}
