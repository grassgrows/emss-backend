package top.warmthdawn.emss.database.entity

import javax.persistence.Entity


/**
 *
 * @author sunday7994
 * @date 2021/7/19
 */

@Entity
class Group(
    groupName: String,
    maxPermissionLevel: Int
) : BaseEntity()