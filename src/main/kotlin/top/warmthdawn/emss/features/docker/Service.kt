package top.warmthdawn.emss.features.docker

import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import io.ebean.Database
import top.warmthdawn.emss.database.entity.query.QImage
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.features.docker.dto.ContainerInfo
import top.warmthdawn.emss.features.docker.vo.ContainerStatus

/**
 *
 * @author WarmthDawn
 * @since 2021-07-09
 */

class ContainerService(private val db: Database) {

    fun createContainer(
        containerName: String, imageName: String,
        hostPortId: Int, exposedPortId: Int,
        volumeBind: Bind, cmd: List<String>
    ): String? {
        val exposedPort = ExposedPort(exposedPortId)
        val binding = Ports.Binding(null, hostPortId.toString())
        val portBinding = PortBinding(binding, exposedPort)

        return DockerManager.createContainer(containerName, imageName, portBinding, volumeBind, cmd)
    }

    suspend fun getContainerName(containerId: String): String {
        val containerInfo = DockerManager.inspectContainer(containerId)
        return containerInfo?.name ?: ""
    }

    suspend fun getContainerCreateTime(containerId: String): String {
        val containerInfo = DockerManager.inspectContainer(containerId)
        if (containerInfo != null) {
            val time: String = containerInfo.createTime
            // TODO 处理字符串
            return time
        } else {
            return ""
        }
    }

    suspend fun getContainerImageId(containerId: String): String {
        val containerInfo = DockerManager.inspectContainer(containerId)
        return containerInfo?.imageId ?: ""
    }

    fun getContainerStatusEnum(containerId: String): ContainerStatus? {
        val containerInfo = DockerManager.inspectContainer(containerId)
        return containerInfo?.status
    }

    suspend fun getContainerStatusText(containerId: String): String {
        val containerInfo = DockerManager.inspectContainer(containerId)
        return when (containerInfo?.status) {
            ContainerStatus.Created -> "已创建"
            ContainerStatus.Running -> "运行中"
            ContainerStatus.Paused -> "已暂停"
            ContainerStatus.Restarting -> "重启中"
            ContainerStatus.Removing -> "删除中"
            ContainerStatus.Exited -> "已关闭"
            ContainerStatus.Dead -> "已崩溃"
            else -> "状态未知"
        }
    }

}