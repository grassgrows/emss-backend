package top.warmthdawn.emss.features.permission

import io.ebean.Database
import top.warmthdawn.emss.database.entity.GroupServer
import top.warmthdawn.emss.database.entity.PermissionGroup
import top.warmthdawn.emss.database.entity.User
import top.warmthdawn.emss.database.entity.UserGroup
import top.warmthdawn.emss.database.entity.query.*
import top.warmthdawn.emss.features.permission.vo.PermissionGroupVO


/**
 *
 * @author sunday7994
 * @date 2021/7/19
 */

class PermissionService
    (
    private val db: Database
) {
    suspend fun createPermissionGroup(groupName: String, maxPermissionLevel: Int) {
        PermissionGroup(
            groupName,
            maxPermissionLevel
        ).insert()
    }

    suspend fun getGroupInfo(): List<PermissionGroupVO> {
        val result: MutableList<PermissionGroupVO> = mutableListOf()
        for (row in QPermissionGroup(db).findList()) {
            val permissionGroupVO = PermissionGroupVO(
                row.id!!,
                row.groupName,
                row.maxPermissionLevel,
                usersInGroup(row.id!!),
                serversOfGroup(row.id!!)
            )
            result.add(permissionGroupVO)
        }
        return result
    }

    private fun usersInGroup(id: Long): List<User> {
        val result: MutableList<User> = mutableListOf()
        for (row in QUserGroup(db).groupId.eq(id).findList()) {
            result.plus(QUser(db).id.eq(row.userId).findList())
        }
        return result
    }

    private fun serversOfGroup(id: Long): List<String> {
        val result: MutableList<String> = mutableListOf()
        for (row in QGroupServer(db).groupId.eq(id).findList()) {
            result.add(QServer(db).id.eq(row.serverId).findOne()!!.abbr)
        }
        return result
    }

    suspend fun addPermissionGS(groupId: Long, serverId: Long) {
        GroupServer(
            groupId,
            serverId
        ).insert()
    }

    suspend fun removePermissionGS(groupId: Long, serverId: Long) {
        QGroupServer(db)
            .groupId.eq(groupId)
            .serverId.eq(serverId)
            .delete()
    }

    suspend fun addPermissionUG(groupId: Long, userId: Long) {
        UserGroup(
            userId,
            groupId
        ).insert()
    }

    suspend fun removePermissionUG(groupId: Long, userId: Long) {
        QUserGroup(db)
            .userId.eq(userId)
            .groupId.eq(groupId)
            .delete()
    }

    suspend fun modifyUserPermission(userId: Long, newLevel: Int) {
        val user = QUser(db).id.eq(userId).findOne()!!
        user.permissionLevel = newLevel
        user.update()
    }

    suspend fun checkUserPermission(username: String, requiredLevel: Int) {
        if (QUser(db).username.eq(username).findOne()!!.permissionLevel > requiredLevel) {
            throw PermissionException(PermissionExceptionMsg.INSUFFICIENT_PERMISSION_LEVEL)
        }
    }
}