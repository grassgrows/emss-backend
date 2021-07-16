package top.warmthdawn.emss.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*

/**
 *
 * @author WarmthDawn
 * @since 2021-07-10
 */

fun Application.configureCors() {
    install(CORS)
    {
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Delete)
        header(HttpHeaders.XForwardedProto)
        header(HttpHeaders.XForwardedFor)
        header(HttpHeaders.XForwardedHost)
        header(HttpHeaders.XHttpMethodOverride)
        header(HttpHeaders.XForwardedServer)
        anyHost()
        allowCredentials = true
        allowNonSimpleContentTypes = true
    }
}