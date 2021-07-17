package top.warmthdawn.emss.features.server.entity

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
class StatisticsInfo(
    val name: String,
    val value: Double,
)

/**
 *
 * [[{name: ]]
 */
class StatisticsList(
    val timestamps: List<Long>,
    val values: List<StatisticsInfo>
)


enum class StatisticsType(
    val flag: Int,
) {
    CPU(1),
    Memory(2),
    DISK_WRITE(4),
    DISK_READ(8),
    NETWORK_UPLOAD(16),
    NETWORK_DOWNLOAD(32),
    ONLINE_PLAYER(64),
    TPS(128);

    fun join(vararg types: StatisticsType): Int {
        return types.map { it.flag }.reduce { a, b -> a and b }
    }
}
