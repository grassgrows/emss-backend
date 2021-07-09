package top.warmthdawn.emss.features.docker

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.settings.BaseSetting
import top.warmthdawn.emss.features.settings.SettingService

fun Route.dockerEndpoint() {

    val settingService by inject<SettingService>()

    route("/docker") {
    }

}