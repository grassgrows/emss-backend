package top.warmthdawn.emss.database.entity

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import top.warmthdawn.emss.database.entity.DockerImages.bindTo
import top.warmthdawn.emss.database.entity.DockerImages.primaryKey

/**
 *
 * @author sunday7994
 * @date 2021/7/8
 */
interface Server : Entity<Server> {
    companion object : Entity.Factory<Server>() //一个伴随对象，继承 Entity.Factory，可用val server = Server()直接创建实体对象

    val id: Int //服务器ID
    var name: String //服务器名
    var createDate: String //创建时间
    var lastDate: String //最后启动时间
    val dockerImageName: DockerImage //Docker镜像名称
    val dockerPorts: String //docker端口

}

object Servers : Table<Server>("t_server") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val createDate = varchar("create_date").bindTo { it.createDate }
    val lastDate = varchar("last_date").bindTo { it.lastDate }
    val dockerImageName = varchar("docker_image_name").references(DockerImages) { it.dockerImageName }
    val dockerPorts = varchar("docker_ports").bindTo { it.dockerPorts }
}