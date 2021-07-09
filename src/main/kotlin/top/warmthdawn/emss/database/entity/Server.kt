package top.warmthdawn.emss.database.entity

//import org.ktorm.entity.Entity
//import org.ktorm.schema.*
import io.ebean.annotation.Length
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne

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
    var lastCrashDate: LocalDateTime? = null, //最后崩溃时间
    var lastStartDate: LocalDateTime? = null, //最后启动时间
    var imageId: Long,
    var containerPort: Int, //Docker容器端口
    var hostPort: Int, //主机端口
): BaseEntity() {
    @OneToOne
    lateinit var image: Image //Docker镜像
}


//interface Server : Entity<Server> {
//    companion object : Entity.Factory<Server>() //一个伴随对象，继承 Entity.Factory，可用val server = Server()直接创建实体对象
//
//    /**
//     * 主键
//     */
//    val id: Long
//    val containerId: String //服务器Docker容器ID
//    var name: String //服务器名
//    var aliasName: String? //服务器别名
//    var abbr: String //服务器缩写
//    var location: String //服务器位置
//    var startCommand: String //服务器启动命令
//    var createDate: LocalDateTime //创建时间
//    var lastCrashDate: LocalDateTime? //最后崩溃时间
//    var lastStartDate: LocalDateTime? //最后启动时间
//    var image: Image //Docker镜像
//    var containerPort: Int //Docker容器端口
//    var hostPort: Int //主机端口
//
//}
//
//object Servers : Table<Server>("t_server") {
//    val id = long("id").primaryKey().bindTo { it.id }
//    val containerId = varchar("container_id").bindTo { it.containerId }
//    val name = varchar("name").bindTo { it.name }
//    var aliasName = varchar("alias_name").bindTo { it.aliasName }
//    var abbr = varchar("abbr").bindTo { it.abbr }
//    var location = varchar("location").bindTo { it.location }
//    var startCommand = varchar("start_command").bindTo { it.startCommand }
//    val createDate = datetime("create_date").bindTo { it.createDate }
//    val lastStartDate = datetime("last_start_date").bindTo { it.lastStartDate }
//    val lastCrashDate = datetime("last_crash_date").bindTo { it.lastCrashDate }
//    val imageName = varchar("image_name").references(Images) { it.image }
//    val containerPort = int("docker_ports").bindTo { it.containerPort }
//    val hostPort = int("host_ports").bindTo { it.hostPort }
//}