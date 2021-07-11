package top.warmthdawn.emss.features.server.vo

import top.warmthdawn.emss.features.docker.vo.ContainerStatus
import java.time.LocalDateTime
import javax.persistence.Entity

/**
 *
 * @author sunday7994
 * @date 2021/7/10
 */

data class ServerVO(
    var id: Long,
    var name: String, //服务器名
    var aliasName: String = "", //服务器别名
    var abbr: String, //服务器缩写
    var location: String, //服务器位置
    var startCommand: String, //服务器启动命令
    var lastCrashDate: LocalDateTime? = null, //最后崩溃时间
    var lastStartDate: LocalDateTime? = null, //最后启动时间
    var imageId: Long,
    var containerPort: Int, //Docker容器端口
    var hostPort: Int, //主机端口
    var containerId: String?, //Docker容器ID
    var containerName: String, //Docker容器名
    var containerCreateTime: LocalDateTime?, //Docker容器创建时间
    var containerStatus: ContainerStatus, //Docker容器状态
)