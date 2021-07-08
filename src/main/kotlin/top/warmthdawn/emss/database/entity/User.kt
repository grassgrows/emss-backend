package top.warmthdawn.emss.database.entity

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

/**
 *
 * @author sunday7994
 * @date 2021/7/8
 */

interface User : Entity<User> {
    val username: String //用户名
    var password: String //密码
    var permissionLevel: String //用户权限等级
    var server: Server //用户管理的服务器
}

object Users : Table<User>("t_user") {
    val username = varchar("username").primaryKey().bindTo { it.username }
    val password = varchar("password").bindTo { it.password}
    val permissionLevel = varchar("permission_level").bindTo { it.permissionLevel }
    val managedServer = varchar("managed_server").references(Servers) { it.server }
}