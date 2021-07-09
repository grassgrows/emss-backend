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
import top.warmthdawn.emss.database.entity.Image
import top.warmthdawn.emss.features.settings.dto.ImageDTO

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
        get("/images") {
            call.respond(settingService.getImages())
        }
        post("/base/update") {
            val baseSetting = call.receive<BaseSetting>()
            settingService.updateBaseSetting(baseSetting)
        }

        route("/image") {

            get("/{id}") {
                val id = call.parameters["id"]!!.toLong()
                call.respond(settingService.getImage(id))
            }
            post {
                val imageDTO = call.receive<ImageDTO>()
                settingService.createImage(imageDTO)
            }

        }


    }

}