package top.warmthdawn.emss.features.server.dto

import kotlinx.serialization.Serializable
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