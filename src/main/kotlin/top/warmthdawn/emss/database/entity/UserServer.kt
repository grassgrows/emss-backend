package top.warmthdawn.emss.database.entity

import javax.persistence.Entity


@Entity
class UserServer(
    var userId: Long,
    var serverId: Long
) : BaseEntity()

//interface UserServer : Entity<UserServer> {
//    companion object : Entity.Factory<UserServer>() //一个伴随对象，继承 Entity.Factory，可用val server = Server()直接创建实体对象
//
//    val userId: Long
//    val serverId: Long
//    val user: User
//    val server: Server
//}
//
//object UserServers : Table<UserServer>("t_user_server") {
//    val userId = long("userId").bindTo { it.userId }.references(Users) { it.user }
//    val serverId = long("serverId").bindTo { it.serverId }.references(Servers) { it.server }
//}