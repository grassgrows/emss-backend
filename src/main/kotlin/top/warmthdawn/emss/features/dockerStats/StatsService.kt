package top.warmthdawn.emss.features.dockerStats

import com.github.dockerjava.api.model.Statistics
import com.github.dockerjava.core.InvocationBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.database.entity.query.QServerRealTime
import top.warmthdawn.emss.features.docker.DockerManager
import top.warmthdawn.emss.features.dockerStats.timerTask.StatsTimerTask
import top.warmthdawn.emss.features.dockerStats.timerTask.TimerTaskInfo
import top.warmthdawn.emss.features.server.ServerException
import top.warmthdawn.emss.features.server.ServerExceptionMsg
import java.util.*
import kotlin.coroutines.CoroutineContext

class StatsService {
    val serverStatsInfoMap = mutableMapOf<Long, ServerStatsInfo>()
    val serverStatsProxy = mutableMapOf<Long, StatsProxy>()

    fun startStats(containerId: String, period: Long, timestampMax: Int) {
        //val server = QServer().id.eq(serverId).findOne()
        //val serverRealTime = QServerRealTime().id.eq(serverId).findOne()
        //if (server == null || serverRealTime == null)
        //    throw ServerException(ServerExceptionMsg.SERVER_NOT_FOUND)
        //TODO

        val serverId:Long = 1   //TODO

        if (!serverStatsInfoMap.keys.contains(serverId)) {
            serverStatsInfoMap[serverId] = ServerStatsInfo(
                CpuUsage(mutableListOf(), mutableListOf(), 0.0),
                MemoryUsage(mutableListOf(), mutableListOf(), 0, 0),
                Disk(0, 0),
                Network(mutableListOf(), mutableMapOf())
            )
        }

        val timerTaskInfo = TimerTaskInfo(
            serverStatsInfoMap[serverId]!!,
            mutableListOf(), mutableListOf(),
            0, mutableMapOf()
        )

        Timer().schedule(StatsTimerTask(timerTaskInfo, timestampMax), Date(), period)

        val statsProxy = StatsProxy()
        statsProxy.stats(containerId, timerTaskInfo)  //TODO

        serverStatsProxy[serverId] = statsProxy

    }
}

class StatsProxy(
    context: CoroutineContext? = null
): CoroutineScope {
    var callback = InvocationBuilder.AsyncResultCallback<Statistics>()

    override val coroutineContext: CoroutineContext = if(context == null) Dispatchers.IO else context + Dispatchers.IO

    fun stats(containerId: String, timerTaskInfo: TimerTaskInfo){
        launch {
            callback = DockerManager.statsContainer(containerId, timerTaskInfo)
        }
    }
}