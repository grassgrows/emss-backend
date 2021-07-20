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
            post("/modify") {
                val users = call.receive<List<User>>()
                users.forEach {
                    permissionService.modifyUserPermission(it)
                }
            }
        }

        route("/GS") {
            post("/add") {
                val groupName = call.request.queryParameters["groupName"]!!
                val permissionDTO = call.receive<PermissionDTO>()
                permissionDTO.addedGS.forEach {
                    permissionService.addPermissionGS(groupName, it)
                }
            }
            post("/remove") {
                val groupName = call.request.queryParameters["groupName"]!!
                val permissionDTO = call.receive<PermissionDTO>()
                permissionDTO.removedGS.forEach {
                    permissionService.removePermissionGS(groupName, it)
                }
            }
        }

        route("/UG") {
            post("/add") {
                val groupName = call.request.queryParameters["groupName"]!!
                val permissionDTO = call.receive<PermissionDTO>()
                permissionDTO.addedUG.forEach {
                    permissionService.addPermissionUG(groupName, it)
                }
            }
            post("/remove") {
                val groupName = call.request.queryParameters["groupName"]!!
                val permissionDTO = call.receive<PermissionDTO>()
                permissionDTO.removedUG.forEach {
                    permissionService.removePermissionUG(groupName, it)
                }
            }
        }
    }
}