package top.warmthdawn.emss.features.docker.timerTask

import top.warmthdawn.emss.features.server.vo.*
import java.util.*

/**
 * @author takanashi
 * @since 2021-07-15
 */

data class TimerTaskInfo(
    val cpuUsageVO: CpuUsageVO,
    val memoryUsageVO: MemoryUsageVO,
    val diskVO: DiskVO, //TODO 磁盘监控
    val networkVO: NetworkVO,
    val cpuUsageList: MutableList<Double>,
    val memoryUsageList: MutableList<Long>,
    var availableMemory: Long,
    val networkNew: MutableMap<String, EachNetworkForSecond>
)


class StatsTimerTask(
    val timerTaskInfo: TimerTaskInfo,
    val timestampsMax: Int
) : TimerTask() {

    override fun run() {

        with(timerTaskInfo) {

            with(cpuUsageVO) {
                // CPU使用率
                if(timestamps.count()>=timestampsMax)
                {
                    timestamps.removeFirst()
                    values.removeFirst()
                }
                timestamps.add(System.currentTimeMillis() / 1000)
                if (cpuUsageList.isNotEmpty()) {
                    values.add(cpuUsageList.average())
                    current = cpuUsageList.last()
                } else {
                    values.add(0.0)
                    current = 0.0
                }

                cpuUsageList.clear()
            }
            with(memoryUsageVO) {
                // 内存使用
                if(timestamps.count()>=timestampsMax) {
                    timestamps.removeFirst()
                    values.removeFirst()
                }
                timestamps.add(System.currentTimeMillis() / 1000)
                if (memoryUsageList.isNotEmpty()) {
                    values.add(memoryUsageList.average().toLong())
                    available = availableMemory
                    current = memoryUsageList.last()
                } else {
                    values.add(0)
                    current = 0
                }

                memoryUsageList.clear()
            }

            //TODO 磁盘


            with(networkVO) {
                // 网络IO
                if(timestamps.count()>=timestampsMax)
                {
                    timestamps.removeFirst()
                    timestamps.add(System.currentTimeMillis() / 1000)
                }
                with(network)
                {
                    for (key in networkNew.keys) {
                        if (!(keys.contains(key))) {
                            put(key, EachNetwork(
                                mutableListOf(), mutableListOf(), 0, 0
                            ))
                        }
                        with(getValue(key))
                        {
                            if (receiveValues.count() >= timestampsMax) {
                                receiveValues.removeFirst()
                                sendValues.removeFirst()
                            }
                            if (networkNew[key]!!.receiveValues.isNotEmpty()) {
                                receiveValues.add(networkNew[key]!!.receiveValues.average().toLong())
                                currentRxBytes = networkNew[key]!!.receiveValues.last()
                            } else {
                                receiveValues.add(0)
                                currentRxBytes = 0
                            }
                            if (networkNew[key]!!.sendValues.isNotEmpty()) {
                                sendValues.add(networkNew[key]!!.sendValues.average().toLong())
                                currentTxBytes = networkNew[key]!!.sendValues.last()
                            } else {
                                sendValues.add(0)
                                currentTxBytes = 0
                            }
                        }
                    }

                    networkNew.clear()
                }


            }
        }

    }
}

//测试
fun main() {
    val cpuUsageVO = CpuUsageVO(mutableListOf(0), mutableListOf(0.0), 0.0)
    val memoryUsageVO = MemoryUsageVO(mutableListOf(0), mutableListOf(0), 0, 0)
    val diskVO = DiskVO(0, 0)
    //val networkVO = NetworkVO(mutableListOf(0), mutableListOf(0), mutableListOf(0), 0, 0)
    //Timer().schedule(StatsTimerTask(cpuUsageVO,memoryUsageVO,diskVO,networkVO), Date(), 1000)

    print(System.currentTimeMillis() / 1000)
//    while(true)
//        cpuUsageVO.current++
}
