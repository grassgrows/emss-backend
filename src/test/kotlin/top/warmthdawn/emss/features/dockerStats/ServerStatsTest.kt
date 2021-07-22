package top.warmthdawn.emss.features.dockerStats

import com.github.dockerjava.api.model.*
import org.junit.Test
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.database.entity.query.QServerRealTime
import top.warmthdawn.emss.features.docker.DockerManager
import top.warmthdawn.emss.features.server.ServerService
import java.lang.Thread.sleep
import java.time.LocalDateTime


fun main() {
/*
    val statsService = StatsService()
    val containerName = "testContainerMC"
    var count = 1

    for(i in 1..10)
    {
        sleep(2000)
        print(count++)
        print(statsService.serverStatsInfoMap)
        print("\n")
    }


    DockerManager.startContainer(containerName!!)
    statsService.startStats(containerName,2000,5)

    for(i in 1..10)
    {
        sleep(2000)
        print(count++)
        print(statsService.serverStatsInfoMap)
        print("\n")
    }

    statsService.serverStatsProxy[1]!!.callback.close()
    DockerManager.stopContainer(containerName!!)

    for(i in 1..10)
    {
        sleep(2000)
        print(count++)
        print(statsService.serverStatsInfoMap)
        print("\n")
    }

*/
}
