package top.warmthdawn.emss.features.server.entity

/**
 *
 * @author WarmthDawn
 * @since 2021-07-17
 */
class StatisticsInfo(
    val type: StatisticsType,
    val value: Double,
)

class StatisticsList(
    val type: StatisticsType,
    val timestamps: List<Long>,
    val values: List<Double>
)


enum class StatisticsType(
    val flag: Int,
) {
    CPU(1),
    MEMORY(2),
    DISK_WRITE(4),
    DISK_READ(8),
    NETWORK_UPLOAD(16),
    NETWORK_DOWNLOAD(32),
    ONLINE_PLAYER(64),
    TPS(128);

    companion object {
        val types = values()
        fun join(vararg types: StatisticsType): Int {
            return types.map { it.flag }.reduce { a, b -> a and b }
        }

        fun containsAll(types: Int): List<StatisticsType> {
            return this.types.filter { (it.flag and types) == it.flag }
        }
        fun getByName(type: String): StatisticsType? {
            return types.firstOrNull() { it.name == type.uppercase() }
        }
    }

}

operator fun Int.contains(type: StatisticsType): Boolean {
    return (type.flag and this) == type.flag
}
