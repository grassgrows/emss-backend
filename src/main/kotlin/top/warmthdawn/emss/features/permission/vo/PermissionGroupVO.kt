package top.warmthdawn.emss.features.permission.vo


/**
 *
 * @author sunday7994
 * @date 2021/7/19
 */

data class PermissionGroupVO(
    var id: Long,
    var groupName: String,
    var maxPermissionLevel: Int,
    var members: List<BriefUserInfoVO>,
    var servers: List<BriefServerInfoVO>,
    var permittedLocation: List<String>
)