package top.warmthdawn.emss.features.server.vo

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
    var imageId: Long,    //镜像的数据库ID
    var workingDir: String,    //工作目录
    var portBindings: Map<Int,Int>,  //端口映射
    var volumeBind: Map<String,String>, //目录映射
    var permissionGroup: List<Long>

)