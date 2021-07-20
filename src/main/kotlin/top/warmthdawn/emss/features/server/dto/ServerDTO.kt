package top.warmthdawn.emss.features.server.dto

import top.warmthdawn.emss.database.entity.PermissionGroup
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDateTime
import javax.persistence.Entity

/**
 *
 * @author sunday7994
 * @date 2021/7/9
 */

data class ServerInfoDTO(
    var name: String, //服务器名
    var aliasName: String? = null, //服务器别名
    var abbr: String, //服务器缩写
    var location: String, //服务器位置
    var startCommand: String, //服务器启动命令
    var workingDir: String,  //工作目录
    var portBindings: Map<Int,Int> = emptyMap(),  //端口映射
    var volumeBind: Map<String,String> = emptyMap(), //目录映射
    var imageId: Long, //镜像的数据库Id
    var permissionGroup: List<String>

//    var containerPort: Int,
//    var hostPort: Int,
)

//data class ServerAttachDTO(
//    var inputStream: InputStream, //输入流
//    var outputStream: OutputStream //输出流
//)
