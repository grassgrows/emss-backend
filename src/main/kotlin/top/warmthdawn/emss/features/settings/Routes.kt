package top.warmthdawn.emss.features.settings

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.settings.dto.ImageDTO

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */

fun Route.settingEndpoint() {

    val settingService by inject<SettingService>()
    val imageService by inject<ImageService>()

    route("/settings") {
        get("/base") {
            call.respond(settingService.getBaseSetting())
        }
        get("/images") {
            call.respond(settingService.getImages())
        }
        post("/base") {
            val baseSetting = call.receive<BaseSetting>()
            settingService.updateBaseSetting(baseSetting)
        }

        route("/image") {

            post("/{id}/download") {
                val id = call.parameters["id"]!!.toLong()
                imageService.downloadImage(id);
            }

            get("/{id}/status") {
                //DockerApi
                val id = call.parameters["id"]!!.toLong()
                val result = imageService.getImageStatus(id)
                call.respond(result)
            }

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