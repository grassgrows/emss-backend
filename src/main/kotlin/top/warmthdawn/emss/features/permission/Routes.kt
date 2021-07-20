package top.warmthdawn.emss.features.permission

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.database.entity.User
import top.warmthdawn.emss.features.permission.dto.PermissionDTO
import top.warmthdawn.emss.utils.R


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
                val id = call.request.queryParameters["userId"]!!
                val level = call.request.queryParameters["newLevel"]!!
                permissionService.modifyUserPermission(id.toLong(), level.toInt())
            }
        }

        route("/GS") {
            post("/add") {
                val groupId = call.request.queryParameters["groupId"]!!
                val serverId = call.request.queryParameters["serverId"]!!
                permissionService.addPermissionGS(groupId.toLong(), serverId.toLong())
            }
            post("/remove") {
                val groupId = call.request.queryParameters["groupId"]!!
                val serverId = call.request.queryParameters["serverId"]!!
                permissionService.removePermissionGS(groupId.toLong(), serverId.toLong())
            }
        }
    }

    route("/UG") {
        post("/add") {
            val groupId = call.request.queryParameters["groupId"]!!
            val permissionDTO = call.receive<PermissionDTO>()
            permissionDTO.addedUG.forEach {
                permissionService.addPermissionUG(groupId.toLong(), it)
            }
        }
        post("/remove") {
            val groupId = call.request.queryParameters["groupId"]!!
            val permissionDTO = call.receive<PermissionDTO>()
            permissionDTO.removedUG.forEach {
                permissionService.removePermissionUG(groupId.toLong(), it)
            }
        }
    }
}
