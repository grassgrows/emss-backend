package top.warmthdawn.emss.features.server.vo

import top.warmthdawn.emss.features.docker.ContainerStatus
import top.warmthdawn.emss.features.server.ServerStatus
import java.lang.management.MemoryUsage
import java.time.LocalDateTime

data class CpuUsageVO(
    var timestamps: List<Long>,  //时间戳表
    var values: List<Double>,    //cpu利用率表，1分钟刷新一次
    var current: Double,         //当前cpu利用率
)
data class MemoryUsageVO(
    var timestamps: List<Long>,  //时间戳表
    var usedMemory: Long,       //已使用内存（单位Bytes）
    var availableMemory: Long,  //总可用内存（单位Bytes）
)
data class NetworkVO(
    var networkRxBytes: Long,   //接收字节（单位Bytes）
    var networkTxBytes: Long,   //发送字节（单位Bytes）
)




