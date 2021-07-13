package top.warmthdawn.emss.database.entity

import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.VolumeBind
import io.ebean.annotation.DbDefault
import io.ebean.annotation.DbJsonB
import java.time.LocalDateTime
import javax.persistence.Entity

/**
 *
 * @author sunday7994
 * @date 2021/7/8
 */
@Entity
class Server(
    var containerId: String? = null, //服务器Docker容器ID
    var name: String, //服务器名
    var aliasName: String = "", //服务器别名
    var abbr: String, //服务器缩写
    var location: String, //服务器位置
    var startCommand: String, //服务器启动命令
    var imageId: Long,   //镜像的数据库ID
    var workingDir: String,  //工作目录
    @DbJsonB
    var portBindings: Map<Int, Int> = emptyMap() ,  //额外端口映射
    @DbJsonB
    var volumeBind: Map<String,String> = emptyMap() //目录映射

//    var containerPort: Int,
//    var hostPort: Int,

): BaseEntity()
