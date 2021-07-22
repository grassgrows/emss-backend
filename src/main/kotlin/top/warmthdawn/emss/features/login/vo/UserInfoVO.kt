package top.warmthdawn.emss.features.login.vo

import top.warmthdawn.emss.database.entity.PermissionGroup
import top.warmthdawn.emss.features.permission.vo.PermissionGroupVO


/**
 *
 * @author sunday7994
 * @date 2021/7/19
 */

data class UserInfoVO(
    var id: Long,
    var username: String,
    var groups: List<PermissionGroup>
)

