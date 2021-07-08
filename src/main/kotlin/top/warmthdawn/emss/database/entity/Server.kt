package top.warmthdawn.emss.database.entity

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/**
 *
 * @author WarmthDawn
 * @since 2021-07-08
 */

interface Server : Entity<Server> {
    companion object : Entity.Factory<Server>() //一个伴随对象，继承 Entity.Factory，可用val server = Server()直接创建实体对象

    val id: Int //服务器ID
    var name: String //服务器名
    var createDate: String //创建时间
    var lastDate: String //最后启动时间
}

object Servers : Table<Server>("t_server") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val createDate = varchar("create_date").bindTo { it.createDate }
    val lastDate = varchar("last_date").bindTo { it.lastDate }
}