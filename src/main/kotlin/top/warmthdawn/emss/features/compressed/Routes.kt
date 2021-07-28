package top.warmthdawn.emss.features.compressed

import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.utils.R

/**
 *
 * @author WarmthDawn
 * @since 2021-07-27
 */

fun Route.compressedEndpoint() {
    route("/compressed") {
        val compressService by inject<CompressService>()

        get("progress") {
            R.ok(compressService.getWorkingProgress())
        }
    }

    //其他的路由已经写在file里面了

}