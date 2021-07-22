package top.warmthdawn.emss.features.permission.vo

import top.warmthdawn.emss.database.entity.User
import top.warmthdawn.emss.features.login.vo.UserInfoVO
import top.warmthdawn.emss.features.server.vo.ServerBriefVO


/**
 *
 * @author sunday7994
 * @date 2021/7/19
 */

data class PermissionGroupVO(
    var id: Long,
    var groupName: String,
    var maxPermissionLevel: Int,
    var members: List<BriefUserInfo>,
    var serverName: List<BriefServerInfo>,
    var permittedLocation: List<String>
)