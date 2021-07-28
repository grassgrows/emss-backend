package top.warmthdawn.emss.plugins

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.content.*
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

        route("/socket") {
            authenticate("auth-jwt") {
                commandEndpoint()
            }
        }
        route("/api") {
            authenticate("auth-jwt") {
                settingEndpoint()
                serverEndpoint()
                dockerEndpoint()
                fileEndpoint()
                permissionEndpoint()
                statisticsEndpoint()
                systemEndpoint()
                compressedEndpoint()
            }
            loginEndpoint()
        }

        static {
            resources("frontend")
            defaultResource("index.html", "frontend")
        }
    }
}
