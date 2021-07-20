package top.warmthdawn.emss.utils

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.database.entity.query.QUser
import top.warmthdawn.emss.features.permission.PermissionException
import top.warmthdawn.emss.features.permission.PermissionExceptionMsg
import top.warmthdawn.emss.features.permission.PermissionService


/**
 *
 * @author sunday7994
 * @date 2021/7/20
 */

val PipelineContext<*, ApplicationCall>.username
    get() =
        (this.call.authentication.principal as JWTPrincipal).payload.getClaim("username").asString()

suspend fun PipelineContext<*, ApplicationCall>.checkPermission(requiredLevel: Int) {
    val permissionService by application.inject<PermissionService>()
    permissionService.checkUserPermission(username = username, requiredLevel)
}

suspend fun PipelineContext<*, ApplicationCall>.checkGroupPermission(groupId: Long, requiredLevel: Int) {
    val permissionService by application.inject<PermissionService>()
    permissionService.checkGroupPermission(username = username, requiredLevel, groupId)
}

suspend fun PipelineContext<*, ApplicationCall>.checkServerPermission(serverId: Long, requiredLevel: Int) {
    val permissionService by application.inject<PermissionService>()
    permissionService.checkServerPermission(username = username, requiredLevel, serverId)
}
