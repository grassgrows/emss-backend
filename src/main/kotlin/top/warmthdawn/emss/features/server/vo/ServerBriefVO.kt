package top.warmthdawn.emss.features.server.vo

import com.sun.org.apache.xpath.internal.operations.Bool
import top.warmthdawn.emss.features.docker.ContainerStatus
import top.warmthdawn.emss.features.server.ServerStatus
import java.time.LocalDateTime

/**
 *
 * @author WarmthDawn
 * @since 2021-07-16
 */
class ServerBriefVO(
    var id: Long,
    var name: String, //服务器名
    var aliasName: String = "", //服务器别名
    var abbr: String, //服务器缩写
    var running: Boolean,
    var port: Int?,
    var imageId: Long,    //镜像的数据库ID
    var lastCrashDate: LocalDateTime? = null, //最后崩溃时间
)