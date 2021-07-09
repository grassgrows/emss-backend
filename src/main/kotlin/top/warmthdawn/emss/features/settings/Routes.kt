package top.warmthdawn.emss.features.settings

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.ktor.ext.inject

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */

fun Route.settingEndpoint() {

    val settingService by inject<SettingService>()

    route("/settings") {
        get("/base") {
            call.respond(settingService.getBaseSetting())
        }
        get("/image") {
            call.respond(settingService.getDockerImage())
        }
        post("/base/update"){
            val baseSetting = call.receive<BaseSetting>()
            settingService.updateBaseSetting(baseSetting)
        }
    }

}