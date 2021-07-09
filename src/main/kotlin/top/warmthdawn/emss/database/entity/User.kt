package top.warmthdawn.emss.database.entity

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import top.warmthdawn.emss.database.entity.Servers.bindTo
import top.warmthdawn.emss.database.entity.Servers.primaryKey

/**
 *
 * @author sunday7994
 * @date 2021/7/8
 */

interface User : Entity<User> {
    /**
     * 主键
     */
    val id: Long
    val username: String //用户名
    var password: String //密码
    var permissionLevel: String //用户权限等级
}

object Users : Table<User>("t_user") {
    val id = long("id").primaryKey().bindTo { it.id }
    val username = varchar("username").bindTo { it.username }
    val password = varchar("password").bindTo { it.password}
    val permissionLevel = varchar("permission_level").bindTo { it.permissionLevel }
}