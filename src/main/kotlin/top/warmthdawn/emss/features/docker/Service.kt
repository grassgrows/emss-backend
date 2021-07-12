package top.warmthdawn.emss.features.docker

import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import io.ebean.Database
import top.warmthdawn.emss.features.docker.vo.ContainerStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    suspend fun getContainerName(containerId: String?): String {
        //val containerInfo = DockerManager.inspectContainer(containerId)
        //return containerInfo?.name ?: "Error"
        return if (containerId != null) DockerManager.inspectContainer(containerId)?.name ?: "NotFindContainer" else "NotFindId"
    }

    suspend fun getContainerCreateTime(containerId: String?): LocalDateTime? {
        if (containerId == null) return null
        val containerInfo = DockerManager.inspectContainer(containerId)
        if (containerInfo != null) {
            // 时间格式处理
            print("**********"+containerInfo.createTime)
            val myDateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME
            print("**********"+myDateTimeFormatter.toString()+"****************/n")
            print("**********"+LocalDateTime.parse(containerInfo.createTime, myDateTimeFormatter)+"****************/n")
            return LocalDateTime.parse(containerInfo.createTime, myDateTimeFormatter)
        } else {
            return null
        }
    }

    suspend fun getContainerImageId(containerId: String?): String {
        //val containerInfo = DockerManager.inspectContainer(containerId)
        //return containerInfo?.imageId ?: ""
        return if (containerId != null) DockerManager.inspectContainer(containerId)?.imageId ?: "NotFindContainer" else "NotFindId"

    }

    suspend fun getContainerStatusEnum(containerId: String?): ContainerStatus {
        //val containerInfo = DockerManager.inspectContainer(containerId)
        //return containerInfo?.status
        return if (containerId != null) DockerManager.inspectContainer(containerId)?.status ?: ContainerStatus.Unknown else ContainerStatus.Unknown

    }

    /*
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
    */
}