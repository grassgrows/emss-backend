package top.warmthdawn.emss.features.server.dto

import kotlinx.serialization.Serializable
import top.warmthdawn.emss.database.entity.BaseEntity
import top.warmthdawn.emss.database.entity.Image
import top.warmthdawn.emss.database.entity.Server
import java.time.LocalDateTime
import javax.persistence.Entity

/**
 *
 * @author sunday7994
 * @date 2021/7/9
 */

@Serializable
data class ServerInfoDTO(
    var name: String, //服务器名
    var aliasName: String? = null, //服务器别名
    var abbr: String, //服务器缩写
    var location: String, //服务器位置
    var startCommand: String, //服务器启动命令
    var containerPort: Int, //Docker容器端口
    var hostPort: Int, //主机端口
    var imageId: Long, //DockerImageId
)

class ImageVO(
    var imageId: String? = null,
    var name: String,
    var repository: String,
    var tag: String = "latest",
    var canRemove: Boolean = true,
)

@Entity
class ServerVO(
    var containerId: String? = null, //服务器Docker容器ID
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
)