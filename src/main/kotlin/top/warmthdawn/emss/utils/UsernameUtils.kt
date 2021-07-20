package top.warmthdawn.emss.utils

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.util.pipeline.*


/**
 *
 * @author sunday7994
 * @date 2021/7/20
 */

val PipelineContext<*, ApplicationCall>.username get() =
    (this.call.authentication.principal as JWTPrincipal).payload.getClaim("username")
