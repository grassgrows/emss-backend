package top.warmthdawn.emss.features.statistics

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.server.ServerService
import top.warmthdawn.emss.features.server.entity.StatisticsType
import top.warmthdawn.emss.features.server.vo.ServerStatisticsVO
import top.warmthdawn.emss.features.statistics.impl.ServerNotFoundException
import top.warmthdawn.emss.features.statistics.impl.StatisticsService
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
                val result = statisticsService.getProvider(id).getByFlag(flag)
                    .map { provider ->
                        val history = provider.getHistory()
                        ServerStatisticsVO(provider.type ,history.timestamps, provider.getCurrent().value, history.values)
                    }
                R.ok(result)
            } else {
                val provider = try {
                    val type = StatisticsType.valueOf(typeParam.uppercase())
                    statisticsService.getProvider(id).getByType(type)
                } catch (e: Exception) {
                    R.error(Code.BadRequest, "无法找到相应的资源", HttpStatusCode.BadRequest)
                    return@get
                }
                val history = provider.getHistory()
                val result = ServerStatisticsVO(provider.type ,history.timestamps, provider.getCurrent().value, history.values)
                R.ok(result)

            }
        }
    }


}