package top.warmthdawn.emss.features.statistics

/**
 *
 * @author WarmthDawn
 * @since 2021-07-26
 */
data class ContainerStatistics(
    val cpuPercent: Double,
    val totalMemory: Long,
    val currentMemory: Long,
    val uploadBytes: Long,
    val downloadBytes: Long,
    val diskRead: Long,
    val diskWrite: Long,
)