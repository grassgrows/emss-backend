package top.warmthdawn.emss.features.settings

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.settings.dto.ImageDTO
import top.warmthdawn.emss.utils.Code
import top.warmthdawn.emss.utils.R
import top.warmthdawn.emss.utils.checkPermission

/**
 *
 * @author sunday7994
 * @date 2021/7/8
 */


fun Route.settingEndpoint() {

    val settingService by inject<SettingService>()
    val imageService by inject<ImageService>()

    route("/settings") {
        get("/base") {
            R.ok(settingService.getBaseSetting())
        }
        get("/images") {
            R.ok(settingService.getImages())
        }
        post("/base") {
            checkPermission(0)
            val baseSetting = call.receive<BaseSetting>()
            settingService.updateBaseSetting(baseSetting)
            R.ok()
        }

        route("/image") {

            post("/{id}/download") {
                checkPermission(0)
                val id = call.parameters["id"]!!.toLong()
                if (imageService.downloadImage(id)) {
                    R.ok()
                } else {
                    R.error(Code.ImageDownloadFailed, "下载Image失败",
                        HttpStatusCode.InternalServerError)
                }
            }

            post("/{id}/cancelDownload") {
                checkPermission(0)
                val id = call.parameters["id"]!!.toLong()
                imageService.cancelDownloadImage(id)
                R.ok()
            }

            get("/{id}/status") {
                //DockerApi
                val id = call.parameters["id"]!!.toLong()
                val result = imageService.getImageStatus(id)
                R.ok(result)
            }

            get("/{id}") {
                val id = call.parameters["id"]!!.toLong()
                R.ok(settingService.getImage(id))
            }

            post {
                checkPermission(0)
                val imageDTO = call.receive<ImageDTO>()
                settingService.createImage(imageDTO)
                R.ok()
            }

            delete("/{id}") {
                checkPermission(0)
                val id = call.parameters["id"]!!.toLong()
                imageService.removeImage(id)
                R.ok()
            }

        }


    }

}