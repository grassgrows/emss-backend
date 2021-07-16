package top.warmthdawn.emss.features.docker

import com.github.dockerjava.api.model.*
import io.ebean.Database
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 *
 * @author WarmthDawn,takanashi
 * @since 2021-07-09
 */

class ContainerService(private val db: Database) {

    fun createContainer(
        containerName: String, imageName: String,
        portBindings: Map<Int, Int>,
        volumeBind: Map<String, String>,
        workingDir: String, command: String,
    ): String? {

        val portBindingList: MutableList<PortBinding> = mutableListOf()
        for (hostPortId in portBindings.keys) {
            if (portBindings[hostPortId] != null) {
                portBindingList.add(
                    PortBinding(
                        Ports.Binding(
                            null, hostPortId.toString()
                        ),
                        ExposedPort(portBindings[hostPortId]!!)
                    )
                )
            }
        }

        val volumeBindList: MutableList<Bind> = mutableListOf()
        for (hostVolume in volumeBind.keys) {
            volumeBindList.add(Bind(hostVolume, Volume(volumeBind[hostVolume])))
        }
        val cmd = mutableListOf("/bin/sh", "-c", command)
        return DockerManager.createContainer(containerName, imageName, portBindingList, volumeBindList, workingDir, cmd)
    }
    /*

    suspend fun getContainerName(containerId: String?): String {
        return if (containerId != null) DockerManager.inspectContainer(containerId)?.name
            ?: "NotFindContainer" else "NotFindId"
    }

    suspend fun getContainerCreateTime(containerId: String?): LocalDateTime? {
        if (containerId == null) return null
        val containerInfo = DockerManager.inspectContainer(containerId)
        if (containerInfo != null) {
            // 时间格式处理
            val myDateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
            return LocalDateTime.parse(containerInfo.createTime, myDateTimeFormatter)
        } else {
            return null
        }
    }

    suspend fun getContainerImageId(containerId: String?): String {
        return if (containerId != null) DockerManager.inspectContainer(containerId)?.imageId
            ?: "NotFindContainer" else "NotFindId"
    }

    suspend fun getContainerStatus(containerId: String?): ContainerStatus {
        return if (containerId != null) DockerManager.inspectContainer(containerId)?.status
            ?: ContainerStatus.Unknown else ContainerStatus.Unknown
    }
    */

}