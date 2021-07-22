package top.warmthdawn.emss.features.permission.dto


/**
 *
 * @author sunday7994
 * @date 2021/7/22
 */

data class BriefUserInfoDTO(
    var id: Long,
    var username: String?,
    var password: String?,
    var groupPermissionLevel: Int?
)