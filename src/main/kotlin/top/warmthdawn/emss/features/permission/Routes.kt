package top.warmthdawn.emss.features.permission

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
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
            get("/brief") {
                R.ok(permissionService.getBriefGroupInfo())
            }
            post("/create") {
                checkPermission(0)
                val name = call.request.queryParameters["name"]!!
                val maxPermissionLevel = call.request.queryParameters["maxLevel"]!!.toInt()
                permissionService.createPermissionGroup(name, maxPermissionLevel)
                R.ok()
            }
            post("/update") {
                checkPermission(0)
                val groupId = call.request.queryParameters["groupId"]!!.toLong()
                val name = call.request.queryParameters["name"]!!
                val maxPermissionLevel = call.request.queryParameters["maxLevel"]!!.toInt()
                permissionService.updatePermissionGroup(groupId, name, maxPermissionLevel)
                R.ok()
            }
            post("/remove") {
                checkPermission(0)
                val groupId = call.request.queryParameters["groupId"]!!.toLong()
                permissionService.removePermissionGroup(groupId)
                R.ok()
            }
        }

        route("/user") {
            get {
                R.ok(permissionService.getAllUserInfo())
            }
            post("/modify") {
                checkPermission(1)
                val groupId = call.request.queryParameters["groupId"]!!.toLong()
                val userId = call.request.queryParameters["userId"]!!.toLong()
                val level = call.request.queryParameters["newLevel"]!!.toInt()
                checkGroupPermission(groupId, 2)
                permissionService.modifyUserPermission(groupId, userId, level)
                R.ok()
            }
            post("/remove") {
                checkPermission(0)
                val userId = call.request.queryParameters["userId"]!!.toLong()
                permissionService.removeUser(userId)
                R.ok()
            }
        }

        route("/GS") {
            post("/add") {
                checkPermission(1)
                val groupId = call.request.queryParameters["groupId"]!!.toLong()
                val serverId = call.request.queryParameters["serverId"]!!.toLong()
                checkGroupPermission(groupId, 2)
                permissionService.addPermissionGS(groupId, serverId)
                R.ok()
            }
            post("/remove") {
                checkPermission(1)
                val groupId = call.request.queryParameters["groupId"]!!.toLong()
                val serverId = call.request.queryParameters["serverId"]!!.toLong()
                checkGroupPermission(groupId, 2)
                permissionService.removePermissionGS(groupId, serverId)
                R.ok()
            }
        }
    }

    route("/UG") {
        post("/add") {
            checkPermission(1)
            val groupId = call.request.queryParameters["groupId"]!!.toLong()
            checkGroupPermission(groupId, 2)
            val permissionDTO = call.receive<PermissionDTO>()
            permissionDTO.addedUG.forEach {
                permissionService.addPermissionUG(groupId, it)
            }
            R.ok()
        }
        post("/remove") {
            checkPermission(1)
            val groupId = call.request.queryParameters["groupId"]!!.toLong()
            val userId = call.request.queryParameters["userId"]!!.toLong()
            checkGroupPermission(groupId, 2)
            permissionService.removePermissionUG(groupId, userId)
            R.ok()
        }
    }

    route("/location"){
        post("/add"){
            val groupId = call.request.queryParameters["groupId"]!!.toLong()
            val location = call.request.queryParameters["location"]!!
            checkGroupPermission(groupId, 2)
            permissionService.addPermittedLocation(groupId, location)
        }
        post("/remove"){
            val groupId = call.request.queryParameters["groupId"]!!.toLong()
            val location = call.request.queryParameters["location"]!!
            checkGroupPermission(groupId, 2)
            permissionService.removePermittedLocation(groupId, location)
        }
    }

}
