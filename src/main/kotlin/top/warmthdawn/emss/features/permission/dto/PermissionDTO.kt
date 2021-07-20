package top.warmthdawn.emss.features.permission.dto

import top.warmthdawn.emss.database.entity.User


/**
 *
 * @author sunday7994
 * @date 2021/7/20
 */

data class PermissionDTO(
    val addedUG: List<Long>,
    val removedUG: List<Long>
)