package top.warmthdawn.emss.features.dockerStats

import com.github.dockerjava.api.model.*
import org.junit.Test
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.docker.DockerManager
import top.warmthdawn.emss.features.server.ServerService


fun main() {
    val containerName = "testContainer008"
    val bind = mutableListOf(Bind("E:\\EMSS\\$containerName", Volume("/data")))
    val cmd = mutableListOf("/bin/sh", "-c", "while true; do echo hello world; sleep 1; done")
    val exposedPort = ExposedPort(8084)
    val binding = Ports.Binding(null, 8083.toString())
    val portBinding = mutableListOf(PortBinding(binding, exposedPort))

    val containerId= DockerManager.createContainer(
        containerName, "openjdk:8", portBinding, bind, "/data/", cmd
    )

    val statsService = StatsService()
    statsService.startStats(containerName,2000,60)

}
