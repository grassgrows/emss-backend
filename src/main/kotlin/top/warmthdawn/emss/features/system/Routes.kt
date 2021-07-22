package top.warmthdawn.emss.features.system

import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.utils.R

/**
 *
 * @author WarmthDawn
 * @since 2021-07-22
 */
fun Route.systemEndpoint() {
    route("/system") {
        val systemService by inject<SystemService>()
        val notificationService by inject<NotificationService>()
        get("/info") {
            R.ok(systemService.getSystemInfo())
        }
        get("/notifications") {
            R.ok(notificationService.getNotifications())
        }
        get("/startingTime") {
            R.ok(systemService.startingTime)
        }
    }
}