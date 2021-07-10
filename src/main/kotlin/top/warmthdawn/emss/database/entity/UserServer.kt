package top.warmthdawn.emss.database.entity

import javax.persistence.Entity

/**
 *
 * @author sunday7994
 * @date 2021/7/9
 */
@Entity
class UserServer(
    var userId: Long,
    var serverId: Long
) : BaseEntity()