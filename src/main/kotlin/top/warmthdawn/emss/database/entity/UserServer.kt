package top.warmthdawn.emss.database.entity

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import top.warmthdawn.emss.database.entity.Servers.bindTo
import top.warmthdawn.emss.database.entity.Servers.primaryKey
import top.warmthdawn.emss.database.entity.Servers.references

interface UserServer : Entity<UserServer> {
    companion object : Entity.Factory<UserServer>() //一个伴随对象，继承 Entity.Factory，可用val server = Server()直接创建实体对象

    val userId: Long
    val serverId: Long
    val user: User
    val server: Server
}

object UserServers : Table<UserServer>("t_user_server") {
    val userId = long("userId").bindTo { it.userId }.references(Users) { it.user }
    val serverId = long("serverId").bindTo { it.serverId }.references(Servers) { it.server }
}