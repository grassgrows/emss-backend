package top.warmthdawn.emss.database.entity

import javax.persistence.Entity


/**
 *
 * @author sunday7994
 * @date 2021/7/19
 */

@Entity
class UserGroup(
    var userId: Long,
    var groupId: Long,
    var groupPermissionLevel: Int, //用户权限等级
) : BaseEntity()