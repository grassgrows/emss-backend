package top.warmthdawn.emss.features.statistics

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.server.ServerService
import top.warmthdawn.emss.features.server.entity.StatisticsType
import top.warmthdawn.emss.features.statistics.StatisticsService
import top.warmthdawn.emss.utils.Code
import top.warmthdawn.emss.utils.R

/**
 *
 * @author WarmthDawn
 * @since 2021-07-20
 */

fun Route.statisticsEndpoint() {
    route("/statistics") {
        val statisticsService by inject<StatisticsService>()
        val serverService by inject<ServerService>()
        get("/{id}/{type}") {
            val id = call.parameters["id"]!!.toLong()
            val typeParam = call.parameters["type"]!!
            val flag = typeParam.toIntOrNull()
            if (flag != null) {

                val types = StatisticsType.containsAll(flag)
                val result = statisticsService.getStatistics(types)
                R.ok(result)
            } else {
                val type = StatisticsType.getByName(typeParam)
                if (type != null) {
                    val result = statisticsService.getStatistics(type)
                    R.ok(result)
                } else {
                    R.error(Code.BadRequest, "无法找到相应的资源", HttpStatusCode.BadRequest)
                }

            }
        }
    }


}