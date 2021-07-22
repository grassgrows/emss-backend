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

val PipelineContext<*, ApplicationCall>.userId
    get() =
        (this.call.authentication.principal as JWTPrincipal).payload.getClaim("userId").asLong()

suspend fun PipelineContext<*, ApplicationCall>.checkPermission(requiredLevel: Int) {
    val permissionService by application.inject<PermissionService>()
    permissionService.checkUserPermission(userId = userId, requiredLevel)
}

suspend fun PipelineContext<*, ApplicationCall>.checkGroupPermission(groupId: Long, requiredLevel: Int) {
    val permissionService by application.inject<PermissionService>()
    permissionService.checkGroupPermission(userId = userId, requiredLevel, groupId)
}

suspend fun PipelineContext<*, ApplicationCall>.checkServerPermission(serverId: Long, requiredLevel: Int) {
    val permissionService by application.inject<PermissionService>()
    permissionService.checkServerPermission(userId = userId, requiredLevel, serverId)
}
