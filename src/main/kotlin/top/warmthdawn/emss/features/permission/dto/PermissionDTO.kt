package top.warmthdawn.emss.features.permission.dto

import top.warmthdawn.emss.database.entity.User


/**
 *
 * @author sunday7994
 * @date 2021/7/20
 */

data class PermissionDTO(
    val addedGS: List<String>,
    val removedGS: List<String>,
    val addedUG: List<User>,
    val removedUG: List<User>
)