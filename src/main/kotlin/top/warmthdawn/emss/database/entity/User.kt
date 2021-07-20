package top.warmthdawn.emss.database.entity

import javax.persistence.Entity

/**
 *
 * @author sunday7994
 * @date 2021/7/8
 */
@Entity
class User(
    var username: String, //用户名
    var password: String,//密码
    var permissionLevel: Int,
) : BaseEntity()
