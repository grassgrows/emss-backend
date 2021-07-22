package top.warmthdawn.emss.features.server.vo

import top.warmthdawn.emss.features.server.entity.StatisticsType

/**
 *
 * @author WarmthDawn
 * @since 2021-07-19
 */
data class ServerStatisticsVO(
    val type: StatisticsType,
    val timestamps: List<Long>,
    val current: Double,
    val values: List<Double>,
)