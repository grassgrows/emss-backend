package top.warmthdawn.emss.features.permission

import com.sun.jna.Function
import io.ktor.application.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.server.ServerService
import top.warmthdawn.emss.utils.R
import java.security.Permissions


/**
 *
 * @author sunday7994
 * @date 2021/7/19
 */

fun Route.permissionEndpoint() {

    val permissionService by inject<PermissionService>()

    route("/permission"){
        route("/group"){
            get{
                R.ok(permissionService.getGroupInfo())
            }
            post("/create") {
                val name = call.request.queryParameters["name"]!!
                val maxPermissionLevel = call.request.queryParameters["maxLevel"]!!.toInt()
                permissionService.createPermissionGroup(name, maxPermissionLevel)
                R.ok()
            }
        }
    }
}