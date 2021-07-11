package top.warmthdawn.emss.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import top.warmthdawn.emss.utils.Code
import top.warmthdawn.emss.utils.R

/**
 *
 * @author WarmthDawn
 * @since 2021-07-11
 */

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> {
            R.error(it)
            throw it
        }

        status(HttpStatusCode.NotFound) {
            R.error(Code.NotFound, "您请求的页面不存在")
            call.response.status(HttpStatusCode.NotFound)
        }
    }
}