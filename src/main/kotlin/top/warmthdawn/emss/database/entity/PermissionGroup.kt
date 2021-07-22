package top.warmthdawn.emss.database.entity

import io.ebean.annotation.DbDefault
import io.ebean.annotation.DbJsonB
import javax.persistence.Entity


/**
 *
 * @author sunday7994
 * @date 2021/7/19
 */

@Entity
class PermissionGroup(
    var groupName: String,
    var maxPermissionLevel: Int,
    @DbDefault("[]")
    @DbJsonB
    var permittedLocation: List<String> = emptyList()
) : BaseEntity()