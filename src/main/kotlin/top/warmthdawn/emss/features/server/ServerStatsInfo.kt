package top.warmthdawn.emss.features.server

/**
 * @author takanashi
 * @since 2021-07-15
 */

data class ServerStatsInfo(
    val containerId: String,
    var cpuUsage: CpuUsage,
    var memoryUsage: MemoryUsage,
    var disk: Disk,
    var network: Network
)



data class CpuUsage(
    var timestamps: MutableList<Long>,  //时间戳表（单位秒）
    var values: MutableList<Double>,    //cpu利用率队列，一段时间刷新一次（单位%）
    var current: Double,         //当前cpu利用率
)
data class MemoryUsage(
    var timestamps: MutableList<Long>,  //时间戳表（单位秒）
    var values: MutableList<Long>,      //已使用内存队列，一段时间刷新一次（单位Bytes）
    var available: Long,   //总可用内存（单位Bytes）
    var current: Long,           //当前已使用内存
)
data class Disk(
    //TODO 磁盘读写监控
    var diskRead: Long,
    var diskWrite: Long
)
data class Network(
    var timestamps: MutableList<Long>,     //时间戳表（单位秒）
    //map第一个参数为网络名
    var network: MutableMap<String, EachNetwork>
)
data class EachNetwork(
    var receiveValues: MutableList<Long>,  //接收字节队列，一段时间刷新一次（单位Bytes）
    var sendValues: MutableList<Long>,     //发送字节队列，一段时间刷新一次（单位Bytes）
    var currentRxBytes: Long,   //当前接收字节（单位Bytes）
    var currentTxBytes: Long,   //当前发送字节（单位Bytes）
)
data class EachNetworkForSecond(
    var receiveValues: MutableList<Long>,
    var sendValues: MutableList<Long>,
)




