package top.warmthdawn.emss.features.settings

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */

fun Route.settingEndpoint() {

    val settingService by inject<SettingService>()
    get("/settings/base") {
        call.respond(settingService.getBaseSetting())
    }

}