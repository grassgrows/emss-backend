package top.warmthdawn.emss.features.server.vo

/**
 *
 * @author WarmthDawn
 * @since 2021-07-19
 */
data class ServerStatisticsVO(
    val timestamps: List<Long>,
    val current: Double,
    val values: List<Double>,
)