package top.warmthdawn.emss.features.permission

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.permission.dto.BriefUserInfoDTO
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
                if (call.request.queryParameters["brief"]?.lowercase() == "true") {
                    R.ok(permissionService.getBriefGroupInfo())
                } else {
                    R.ok(permissionService.getGroupInfo())
                }
            }

            post {
                checkPermission(0)
                val name = call.request.queryParameters["name"]!!
                val maxPermissionLevel = call.request.queryParameters["maxLevel"]!!.toInt()
                permissionService.createPermissionGroup(name, maxPermissionLevel)
                R.ok()
            }

            get("/{id}") {
                val groupId = call.parameters["id"]!!.toLong()
                R.ok(permissionService.getGroupInfo(groupId))
            }
            post("/{id}") {
                checkPermission(0)
                val groupId = call.parameters["id"]!!.toLong()
                val name = call.request.queryParameters["name"]!!
                val maxPermissionLevel = call.request.queryParameters["maxLevel"]!!.toInt()
                permissionService.updatePermissionGroup(groupId, name, maxPermissionLevel)
                R.ok()
            }
            delete("/{id}") {
                checkPermission(0)
                val groupId = call.parameters["id"]!!.toLong()
                permissionService.removePermissionGroup(groupId)
                R.ok()
            }

            route("/{groupId}") {
                route("/server") {
                    post("/{serverId}") {
                        checkPermission(1)
                        val groupId = call.parameters["groupId"]!!.toLong()
                        val serverId = call.parameters["serverId"]!!.toLong()
                        checkGroupPermission(groupId, 2)
                        permissionService.addPermissionGS(groupId, serverId)
                        R.ok()
                    }
                    delete("/{serverId}") {
                        checkPermission(1)
                        val groupId = call.parameters["groupId"]!!.toLong()
                        val serverId = call.parameters["serverId"]!!.toLong()
                        checkGroupPermission(groupId, 2)
                        permissionService.removePermissionGS(groupId, serverId)
                        R.ok()
                    }
                }

                route("/location") {
                    delete {
                        val groupId = call.parameters["groupId"]!!.toLong()
                        val location = call.request.queryParameters["location"]!!
                        checkGroupPermission(groupId, 2)
                        permissionService.removePermittedLocation(groupId, location)
                        R.ok()
                    }
                    post {
                        checkPermission(1)
                        val groupId = call.parameters["groupId"]!!.toLong()
                        val location = call.receive<List<String>>()
                        checkGroupPermission(groupId, 2)
                        permissionService.updatePermittedLocation(groupId, location)
                        R.ok()
                    }
                }
            }
        }

        route("/user") {
            get {
                R.ok(permissionService.getAllUserInfo())
            }
            post {
                checkPermission(1)
                val groupId = call.request.queryParameters["groupId"]!!.toLong()
//                val userId = call.request.queryParameters["userId"]!!.toLong()
//                val level = call.request.queryParameters["newLevel"]!!.toInt()
                val userDTO = call.receive<BriefUserInfoDTO>()
                checkGroupPermission(groupId, 2)
                permissionService.modifyUserPermission(groupId, userDTO)
                R.ok()
            }
            delete("/{id}") {
                checkPermission(0)
                val userId = call.parameters["id"]!!.toLong()
                permissionService.removeUser(userId)
                R.ok()
            }
        }



        route("/user-group") {
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
            delete {
                checkPermission(1)
                val groupId = call.request.queryParameters["groupId"]!!.toLong()
                val userId = call.request.queryParameters["userId"]!!.toLong()
                checkGroupPermission(groupId, 2)
                permissionService.removePermissionUG(groupId, userId)
                R.ok()
            }
            post("/update") {
                checkPermission(1)
                val groupId = call.request.queryParameters["groupId"]!!.toLong()
                val userList = call.receive<List<Long>>()
                checkGroupPermission(groupId, 2)
                permissionService.updatePermissionUG(groupId, userList)
                R.ok()
            }
        }
    }

}
