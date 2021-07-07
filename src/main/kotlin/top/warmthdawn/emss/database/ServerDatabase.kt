package top.warmthdawn.emss.database

import org.ktorm.entity.*
import org.ktorm.schema.*
import top.warmthdawn.emss.database.Servers.bindTo

/**
 * @author
 */

interface Server : Entity<Server> {
    companion object : Entity.Factory<Server>() //一个伴随对象，继承 Entity.Factory，可用val server = Server()直接创建实体对象

    val id: Int //服务器ID
    var name: String //服务器名
    var createDate: String //创建时间
    var lastDate: String //最后启动时间
}

interface User : Entity<User> {
    companion object : Entity.Factory<User>()

    var username: String //用户名
    var password: String //密码
    var createDate: String //注册时间
    var lastDate: String //最后登录时间
}

object Servers : Table<Server>("t_server") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val createDate = varchar("create_date").bindTo { it.createDate }
    val lastDate = varchar("last_date").bindTo { it.lastDate }
}

object Users : Table<User>("t_user") {
    val username = varchar("username").primaryKey().bindTo { it.username }
    val password = varchar("password").bindTo { it.password }
    val createDate = varchar("create_date").bindTo { it.createDate }
    val lastDate = varchar("last_date").bindTo { it.lastDate }
}