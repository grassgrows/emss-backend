package top.warmthdawn.emss.features.server.vo

import top.warmthdawn.emss.features.docker.ContainerStatus
import top.warmthdawn.emss.features.server.ServerStatus
import java.lang.management.MemoryUsage
import java.time.LocalDateTime


data class ServerStatsVO(
    var cpuUsageVO: CpuUsageVO,
    var memoryUsageVO: MemoryUsageVO,
    var diskVO: DiskVO,
    var networkVO: NetworkVO
)



data class CpuUsageVO(
    var timestamps: MutableList<Long>,  //时间戳表（单位秒）
    var values: MutableList<Double>,    //cpu利用率队列，一段时间刷新一次（单位%）
    var current: Double,         //当前cpu利用率
)
data class MemoryUsageVO(
    var timestamps: MutableList<Long>,  //时间戳表（单位秒）
    var values: MutableList<Long>,      //已使用内存队列，一段时间刷新一次（单位Bytes）
    var available: Long,   //总可用内存（单位Bytes）
    var current: Long,           //当前已使用内存
)
data class DiskVO(
    //TODO 磁盘读写监控
    var diskRead: Long,
    var diskWrite: Long
)
data class NetworkVO(
    var timestamps: MutableList<Long>,     //时间戳表（单位秒）
    //map第一个参数为网络名
    var network: MutableMap<String,EachNetwork>
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




