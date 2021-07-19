package top.warmthdawn.emss.features.server

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.server.dto.ServerInfoDTO
import top.warmthdawn.emss.features.server.impl.StatisticsService
import top.warmthdawn.emss.features.server.vo.ServerStatisticsVO
import top.warmthdawn.emss.utils.R
import top.warmthdawn.emss.utils.checkPermission
import top.warmthdawn.emss.utils.checkServerPermission

/**
 *
 * @author sunday7994
 * @date 2021/7/9
 */

fun Route.serverEndpoint() {

    val serverService by inject<ServerService>()
    val statisticsService by inject<StatisticsService>()
    route("/servers") {
        get {
            R.ok(serverService.getServersBriefInfo())
        }

        post {
            checkPermission(0)
            val dtoServerInfo = call.receive<ServerInfoDTO>()
            serverService.createServerInfo(dtoServerInfo)
            R.ok()
        }

        delete("/{id}"){
            checkPermission(0)
            val id = call.parameters["id"]!!.toLong()
            serverService.removeServer(id)
            R.ok()
        }
        get("/{id}") {
            val id = call.parameters["id"]!!.toLong()
            R.ok(serverService.getServerInfo(id))
        }
        post("/{id}") {
            val id = call.parameters["id"]!!.toLong()
            checkServerPermission(id, 3)
            val dto = call.receive<ServerInfoDTO>()
            serverService.updateServerInfo(id, dto)
            R.ok()
        }
        get("/{id}/stats") {
            val id = call.parameters["id"]!!.toLong()
            val flag = call.parameters["type"]!!.toInt()
            val type = statisticsService.getProvider(id).getByFlag(flag)
                .map {
                    val history = it.getHistory()
                    ServerStatisticsVO(history.timestamps, it.getCurrent().value, history.values)
                }
            R.ok(type)
        }
        post("/{id}/start") {
            val id = call.parameters["id"]!!.toLong()
            checkServerPermission(id, 3)
            serverService.start(id)
            R.ok()
        }
        post("/{id}/stop") {
            val id = call.parameters["id"]!!.toLong()
            checkServerPermission(id, 3)
            serverService.stop(id)
            R.ok()
        }
        post("/{id}/restart") {
            val id = call.parameters["id"]!!.toLong()
            checkServerPermission(id, 3)
            serverService.restart(id)
            R.ok()
        }
        post("/{id}/terminate") {
            val id = call.parameters["id"]!!.toLong()
            checkServerPermission(id, 3)
            serverService.terminate(id)
            R.ok()
        }


    }

}