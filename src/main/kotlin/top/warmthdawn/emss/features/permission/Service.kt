package top.warmthdawn.emss.features.permission

import io.ebean.Database
import top.warmthdawn.emss.database.entity.GroupServer
import top.warmthdawn.emss.database.entity.PermissionGroup
import top.warmthdawn.emss.database.entity.UserGroup
import top.warmthdawn.emss.database.entity.query.*
import top.warmthdawn.emss.features.login.LoginException
import top.warmthdawn.emss.features.login.LoginExceptionMsg
import top.warmthdawn.emss.features.login.LoginService
import top.warmthdawn.emss.features.permission.dto.BriefUserInfoDTO
import top.warmthdawn.emss.features.permission.vo.BriefGroupInfoVO
import top.warmthdawn.emss.features.permission.vo.BriefServerInfoVO
import top.warmthdawn.emss.features.permission.vo.BriefUserInfoVO
import top.warmthdawn.emss.features.permission.vo.PermissionGroupVO


/**
 *
 * @author sunday7994
 * @date 2021/7/19
 */

class PermissionService
    (
    private val db: Database,
    val loginService: LoginService
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

    suspend fun getAllUserInfo(): List<BriefUserInfoVO> {
        val result: MutableList<BriefUserInfoVO> = mutableListOf()
        for (row in QUser(db).findList()) {
            val briefUserInfo = BriefUserInfoVO(
                row.id!!,
                row.username,
                row.permissionLevel
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

    private suspend fun usersInGroup(groupId: Long): List<BriefUserInfoVO> {
        val result: MutableList<BriefUserInfoVO> = mutableListOf()
        QUserGroup(db).groupId.eq(groupId).findList().forEach {
            for (row in QUser(db).id.eq(it.userId).findList()) {
                val briefUserInfo = BriefUserInfoVO(
                    row.id!!,
                    row.username,
                    it.groupPermissionLevel
                )
                result.add(briefUserInfo)
            }
        }
        return result
    }

    private suspend fun serversOfGroup(groupId: Long): List<BriefServerInfoVO> {
        val result: MutableList<BriefServerInfoVO> = mutableListOf()
        QGroupServer(db).groupId.eq(groupId).findList().forEach {
            for (row in QServer(db).id.eq(it.serverId).findList()) {
                val briefServerInfo = BriefServerInfoVO(
                    row.id!!,
                    row.name,
                    row.abbr
                )
                result.add(briefServerInfo)
            }
        }
        return result
    }

    suspend fun getBriefGroupInfo(): List<BriefGroupInfoVO> {
        val result: MutableList<BriefGroupInfoVO> = mutableListOf()
        for (row in QPermissionGroup(db).findList()) {
            val briefGroupInfo = BriefGroupInfoVO(
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

    suspend fun getGroupInfo(groupId: Long): PermissionGroupVO {
        val group = QPermissionGroup(db).id.eq(groupId).findOne()!!
        return PermissionGroupVO(
            group.id!!,
            group.groupName,
            group.maxPermissionLevel,
            usersInGroup(group.id!!),
            serversOfGroup(group.id!!),
            getPermittedLocation(group.id!!)
        )
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

    suspend fun updatePermissionUG(groupId: Long, userList: List<Long>) {
        val userGroup = QUserGroup(db).groupId.eq(groupId)

        QUserGroup(db)
            .groupId.eq(groupId)
            .userId.notIn(userList)
            .delete()

        val existing = QUserGroup(db)
            .groupId.eq(groupId)
            .userId.isIn(userList)
            .findIds<Long>()

        val toAdd = userList - existing
        val userGroups = QUser(db)
            .id.isIn(toAdd)
            .findList()
            .map { UserGroup(it.id!!, groupId, it.permissionLevel) }
        db.insertAll(userGroups)

    }


    suspend fun addPermittedLocation(groupId: Long, location: String) {
        val group = QPermissionGroup(db).id.eq(groupId).findOne()!!
        group.permittedLocation += location
        group.update()
    }

    suspend fun updatePermittedLocation(groupId: Long, location: List<String>) {
        val group = QPermissionGroup(db).id.eq(groupId).findOne()!!
        group.permittedLocation = location
        group.update()
    }

    suspend fun removePermittedLocation(groupId: Long, location: String) {
        val group = QPermissionGroup(db).id.eq(groupId).findOne()!!
        group.permittedLocation -= location
        group.update()
    }

    suspend fun modifyUserPermission(groupId: Long, briefUserInfoDTO: BriefUserInfoDTO) {
        val userGroup = QUserGroup(db)
            .userId.eq(briefUserInfoDTO.id)
            .groupId.eq(groupId)
            .findOne()!!
        if (briefUserInfoDTO.groupPermissionLevel != null) {
            userGroup.groupPermissionLevel = briefUserInfoDTO.groupPermissionLevel!!
            userGroup.update()
            if (briefUserInfoDTO.groupPermissionLevel!! < 2) {
                val user = QUser(db)
                    .id.eq(briefUserInfoDTO.id)
                    .findOne()!!
                user.permissionLevel = briefUserInfoDTO.groupPermissionLevel!!
                user.update()
            }
        }
        if (!briefUserInfoDTO.username.isNullOrEmpty()) {
            loginService.modifyUserName(briefUserInfoDTO.id, briefUserInfoDTO.username!!)
        }
        if (!briefUserInfoDTO.password.isNullOrEmpty()) {
            loginService.modifyPasswordAdmin(briefUserInfoDTO.id, briefUserInfoDTO.password!!)
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
                .findOne()
                ?.groupPermissionLevel ?: 4 > requiredLevel
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
            .getInteger("RESULT") ?: 4
        if (permissionLevel > requiredLevel
        ) {
            throw PermissionException(PermissionExceptionMsg.INSUFFICIENT_PERMISSION_LEVEL)
        }
    }

}