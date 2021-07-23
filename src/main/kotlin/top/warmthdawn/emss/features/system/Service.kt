package top.warmthdawn.emss.features.system

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import oshi.SystemInfo
import java.time.LocalDateTime
import java.util.*


/**
 *
 * @author WarmthDawn
 * @since 2021-07-22
 */
class NotificationService {
    private val notifications = LinkedList<NotificationObj>()
    private val maxNotification = 20

    fun getNotifications(): List<NotificationObj> {
        return notifications.toList()
    }

    fun notify(msg: String, level: NotificationObj.Level) {
        notifications.offer(NotificationObj(LocalDateTime.now(), msg, level))
        if(notifications.size > maxNotification) {
            notifications.pop()
        }
    }
}

class NotificationObj(
    val time: LocalDateTime,
    val msg: String,
    val level: Level,
) {
    enum class Level {
        INFO,
        WARN,
        SUCCESS,
        ERROR,
    }
}

class SystemService {
    val systemInfo = SystemInfo()
    val processor = systemInfo.hardware.processor
    val memory = systemInfo.hardware.memory

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val timestamps = LinkedList<Long>()
    private val cpus = LinkedList<Double>()
    private val memorys = LinkedList<Long>()

    lateinit var lastCpu: LongArray
    var totalMemory: Long = 0
    var currentMemory: Long = 0
    var currentCpu: Double = 0.0

    fun init() {
        lastCpu = processor.systemCpuLoadTicks
        totalMemory = memory.total
        val cpuPeriod = DoubleArray(10)
        val memoryPeriod = LongArray(10)
        coroutineScope.launch {
            while(true) {
                for(i in 0..9) {
                    currentCpu = processor.getSystemCpuLoadBetweenTicks(lastCpu)
                    lastCpu = processor.systemCpuLoadTicks
                    currentMemory = totalMemory - memory.available
                    cpuPeriod[i] = currentCpu
                    memoryPeriod[i] = currentMemory
                    if (timestamps.size > 120) {
                        timestamps.poll()
                        cpus.poll()
                        memorys.poll()
                    }
                    delay(1000)
                }
                timestamps.offer(System.currentTimeMillis() / 1000)
                cpus.offer(cpuPeriod.average())
                memorys.offer(memoryPeriod.average().toLong())
            }
        }
    }

    fun getSystemInfo(): SystemInfoVO {
        return SystemInfoVO(
            timestamps.toLongArray(),
            cpus.toDoubleArray(),
            memorys.toLongArray(),
            currentCpu,
            currentMemory,
            totalMemory,
            )
    }

    val startingTime = LocalDateTime.now()
}

data class SystemInfoVO(
    val timestamps: LongArray,
    val cpus: DoubleArray,
    val memorys: LongArray,
    val currentCpu: Double,
    val currentMemory: Long,
    val maxMemory: Long,
)
