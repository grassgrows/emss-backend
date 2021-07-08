package top.warmthdawn.emss.database.entity

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/**
 *
 * @author sunday7994
 * @date 2021/7/8
 */

interface Server : Entity<Server> {
    companion object : Entity.Factory<Server>() //一个伴随对象，继承 Entity.Factory，可用val server = Server()直接创建实体对象

    val containerId: String //服务器Docker容器ID
    var name: String //服务器名
    var createDate: String //创建时间
    var lastDate: String //最后启动时间
    var administrator: User //服务器管理员
    var image: Image //Docker镜像
    var containerPort: Int //Docker容器端口
    var hostPort: Int //主机端口

}

object Servers : Table<Server>("t_server") {
    val containerId = varchar("container_id").primaryKey().bindTo { it.containerId }
    val name = varchar("name").bindTo { it.name }
    val createDate = varchar("create_date").bindTo { it.createDate }
    val lastDate = varchar("last_date").bindTo { it.lastDate }
    val administratorName = varchar("administrator_name").references(Users) { it.administrator }
    val imageName = varchar("image_name").references(Images) { it.image }
    val containerPort = int("docker_ports").bindTo { it.containerPort }
    val hostPort = int("host_ports").bindTo { it.hostPort }
}