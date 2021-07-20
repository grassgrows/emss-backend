package top.warmthdawn.emss.features.permission

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.database.entity.User
import top.warmthdawn.emss.features.permission.dto.PermissionDTO
import top.warmthdawn.emss.utils.R
import top.warmthdawn.emss.utils.checkGroupPermission
import top.warmthdawn.emss.utils.checkPermission


/**
 *
 * @author sunday7994
 * @date 2021/7/19
 */

fun Route.permissionEndpoint() {

    val permissionService by inject<PermissionService>()

    route("/permission") {
        route("/group") {
            get {
                R.ok(permissionService.getGroupInfo())
            }
            post("/create") {
                checkPermission(0)
                val name = call.request.queryParameters["name"]!!
                val maxPermissionLevel = call.request.queryParameters["maxLevel"]!!.toInt()
                permissionService.createPermissionGroup(name, maxPermissionLevel)
                R.ok()
            }
        }

        route("/user") {
            get{
                R.ok(permissionService.getUserInfo())
            }
            post("/modify") {
                checkPermission(1)
                val groupId = call.request.queryParameters["groupId"]!!
                val userId = call.request.queryParameters["userId"]!!
                val level = call.request.queryParameters["newLevel"]!!
                checkGroupPermission(groupId.toLong(), 2)
                permissionService.modifyUserPermission(groupId.toLong(), userId.toLong(), level.toInt())
            }
        }

        route("/GS") {
            post("/add") {
                checkPermission(1)
                val groupId = call.request.queryParameters["groupId"]!!
                val serverId = call.request.queryParameters["serverId"]!!
                checkGroupPermission(groupId.toLong(), 2)
                permissionService.addPermissionGS(groupId.toLong(), serverId.toLong())
            }
            post("/remove") {
                checkPermission(1)
                val groupId = call.request.queryParameters["groupId"]!!
                val serverId = call.request.queryParameters["serverId"]!!
                checkGroupPermission(groupId.toLong(), 2)
                permissionService.removePermissionGS(groupId.toLong(), serverId.toLong())
            }
        }
    }

    route("/UG") {
        post("/add") {
            checkPermission(1)
            val groupId = call.request.queryParameters["groupId"]!!
            checkGroupPermission(groupId.toLong(), 2)
            val permissionDTO = call.receive<PermissionDTO>()
            permissionDTO.addedUG.forEach {
                permissionService.addPermissionUG(groupId.toLong(), it)
            }
        }
        post("/remove") {
            checkPermission(1)
            val groupId = call.request.queryParameters["groupId"]!!
            checkGroupPermission(groupId.toLong(), 2)
            val permissionDTO = call.receive<PermissionDTO>()
            permissionDTO.removedUG.forEach {
                permissionService.removePermissionUG(groupId.toLong(), it)
            }
        }
    }
}
