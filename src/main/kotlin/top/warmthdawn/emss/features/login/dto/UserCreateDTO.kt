package top.warmthdawn.emss.features.login.dto

/**
 * @author takanashi
 * @since 2021-07-22
 */

data class UserCreateDTO(
    var username: String,
    var password: String,
    var permissionLevel: Int
)