package top.warmthdawn.emss.features.permission


/**
 *
 * @author sunday7994
 * @date 2021/7/20
 */

class PermissionException(
    val permissionExceptionMsg: PermissionExceptionMsg
) : Exception(permissionExceptionMsg.toString())

enum class PermissionExceptionMsg {
    INSUFFICIENT_PERMISSION_LEVEL,
    GROUP_DATABASE_REMOVE_FAILED,
    USER_DATABASE_REMOVE_FAILED
}