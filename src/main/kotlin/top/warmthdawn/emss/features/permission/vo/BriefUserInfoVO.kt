package top.warmthdawn.emss.features.permission.vo

/**
 *
 * @author sunday7994
 * @date 2021/7/21
 */

data class BriefUserInfoVO(
    var id: Long,
    var username: String,
    var groupPermissionLevel: Int? = null
)