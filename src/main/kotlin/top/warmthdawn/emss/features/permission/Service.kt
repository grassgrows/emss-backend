package top.warmthdawn.emss.features.permission

import io.ebean.Database
import top.warmthdawn.emss.database.entity.GroupServer
import top.warmthdawn.emss.database.entity.PermissionGroup
import top.warmthdawn.emss.database.entity.User
import top.warmthdawn.emss.database.entity.UserGroup
import top.warmthdawn.emss.database.entity.query.*
import top.warmthdawn.emss.features.permission.vo.BriefGroupInfo
import top.warmthdawn.emss.features.permission.vo.BriefServerInfo
import top.warmthdawn.emss.features.permission.vo.BriefUserInfo
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
            maxPermissionLevel,
        ).insert()
    }

    suspend fun updatePermissionGroup(groupId: Long, groupName: String?, maxPermissionLevel: Int?) {
        val group = QPermissionGroup(db).id.eq(groupId).findOne()!!
        if (groupName != null)
            group.groupName = groupName
        if (maxPermissionLevel != null)
            group.maxPermissionLevel = maxPermissionLevel
        group.update()
    }

    suspend fun removePermissionGroup(groupId: Long) {
        if (!QPermissionGroup(db).id.eq(groupId).findOne()!!.delete() ||
            QGroupServer(db).groupId.eq(groupId).delete() <= 0 ||
            QUserGroup(db).groupId.eq(groupId).delete() <= 0
        ) {
            throw PermissionException(PermissionExceptionMsg.GROUP_DATABASE_REMOVE_FAILED)
        }
    }

    suspend fun getAllUserInfo(): List<BriefUserInfo> {
        val result: MutableList<BriefUserInfo> = mutableListOf()
        for (row in QUser(db).findList()) {
            val briefUserInfo = BriefUserInfo(
                row.id!!,
                row.username,
            )
            result.add(briefUserInfo)
        }
        return result
    }

    suspend fun removeUser(userId: Long) {
        if (!QUser(db).id.eq(userId).findOne()!!.delete() ||
            QUserGroup(db).userId.eq(userId).delete() <= 0
        ) {
            throw PermissionException(PermissionExceptionMsg.USER_DATABASE_REMOVE_FAILED)
        }
    }

    private suspend fun usersInGroup(groupId: Long): List<BriefUserInfo> {
        val result: MutableList<BriefUserInfo> = mutableListOf()
        QUserGroup(db).groupId.eq(groupId).findList().forEach {
            for (row in QUser(db).id.eq(it.userId).findList()) {
                val briefUserInfo = BriefUserInfo(
                    row.id!!,
                    row.username,
                    it.groupPermissionLevel
                )
                result.add(briefUserInfo)
            }
        }
        return result
    }

    private suspend fun serversOfGroup(groupId: Long): List<BriefServerInfo> {
        val result: MutableList<BriefServerInfo> = mutableListOf()
        QGroupServer(db).groupId.eq(groupId).findList().forEach {
            for (row in QServer(db).id.eq(it.serverId).findList()) {
                val briefServerInfo = BriefServerInfo(
                    row.id!!,
                    row.name,
                    row.abbr
                )
                result.add(briefServerInfo)
            }
        }
        return result
    }

    suspend fun getBriefGroupInfo(): List<BriefGroupInfo> {
        val result: MutableList<BriefGroupInfo> = mutableListOf()
        for (row in QPermissionGroup(db).findList()) {
            val briefGroupInfo = BriefGroupInfo(
                row.id!!,
                row.groupName,
            )
            result.add(briefGroupInfo)
        }
        return result
    }

    private suspend fun getPermittedLocation(groupId: Long): List<String> {
        return QPermissionGroup(db).id.eq(groupId).findOne()!!.permittedLocation
    }


    suspend fun getGroupInfo(): List<PermissionGroupVO> {
        val result: MutableList<PermissionGroupVO> = mutableListOf()
        for (row in QPermissionGroup(db).findList()) {
            val permissionGroupVO = PermissionGroupVO(
                row.id!!,
                row.groupName,
                row.maxPermissionLevel,
                usersInGroup(row.id!!),
                serversOfGroup(row.id!!),
                getPermittedLocation(row.id!!)
            )
            result.add(permissionGroupVO)
        }
        return result
    }

    suspend fun addPermissionGS(groupId: Long, serverId: Long) {
        GroupServer(
            groupId,
            serverId,
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
            groupId,
            4
        ).insert()
    }

    suspend fun removePermissionUG(groupId: Long, userId: Long) {
        QUserGroup(db)
            .userId.eq(userId)
            .groupId.eq(groupId)
            .delete()
    }

    suspend fun addPermittedLocation(groupId: Long, location: String) {
        val group = QPermissionGroup(db).id.eq(groupId).findOne()!!
        group.permittedLocation += location
        group.update()
    }

    suspend fun removePermittedLocation(groupId: Long, location: String) {
        val group = QPermissionGroup(db).id.eq(groupId).findOne()!!
        group.permittedLocation -= location
        group.update()
    }

    suspend fun modifyUserPermission(groupId: Long, userId: Long, newLevel: Int) {
        val userGroup = QUserGroup(db)
            .userId.eq(userId)
            .groupId.eq(groupId)
            .findOne()!!
        userGroup.groupPermissionLevel = newLevel
        userGroup.update()
        if (newLevel == 0) {
            val user = QUser(db)
                .id.eq(userId)
                .findOne()!!
            user.permissionLevel = 0
            user.update()
        }

    }

    suspend fun checkUserPermission(userId: Long, requiredLevel: Int) {
        if (QUser(db).id.eq(userId).findOne()!!.permissionLevel > requiredLevel) {
            throw PermissionException(PermissionExceptionMsg.INSUFFICIENT_PERMISSION_LEVEL)
        }
    }

    suspend fun checkGroupPermission(userId: Long, requiredLevel: Int, groupId: Long) {
        if (QUserGroup(db)
                .userId.eq(userId)
                .groupId.eq(groupId)
                .findOne()!!
                .groupPermissionLevel > requiredLevel
        ) {
            throw PermissionException(PermissionExceptionMsg.INSUFFICIENT_PERMISSION_LEVEL)
        }
    }

    suspend fun checkServerPermission(userId: Long, requiredLevel: Int, serverId: Long) {
        val sql =
            "SELECT MIN(GROUP_PERMISSION_LEVEL) as RESULT FROM USER_GROUP\n" +
                    "LEFT JOIN GROUP_SERVER on USER_GROUP.GROUP_ID = GROUP_SERVER.GROUP_ID\n" +
                    "WHERE GROUP_SERVER.SERVER_ID = :server_id AND USER_GROUP.USER_ID = :user_id"

        val permissionLevel = db.sqlQuery(sql)
            .setParameter("server_id", serverId)
            .setParameter("user_id", userId)
            .findOne()!!
            .getInteger("RESULT")
        if (permissionLevel > requiredLevel
        ) {
            throw PermissionException(PermissionExceptionMsg.INSUFFICIENT_PERMISSION_LEVEL)
        }
    }

}