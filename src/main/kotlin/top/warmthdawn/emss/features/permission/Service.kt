package top.warmthdawn.emss.features.permission

import io.ebean.Database
import top.warmthdawn.emss.database.entity.GroupServer
import top.warmthdawn.emss.database.entity.PermissionGroup
import top.warmthdawn.emss.database.entity.User
import top.warmthdawn.emss.database.entity.UserGroup
import top.warmthdawn.emss.database.entity.query.*
import top.warmthdawn.emss.features.login.vo.UserInfoVO
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
        for (row in QUser(db).findList()) {
            val permissionGroupVO = PermissionGroupVO(
                row.id!!,
                row.username,
                row.permissionLevel,
                usersInGroup(row.id!!)
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

    suspend fun addPermissionGS(groupName: String, serverName: String) {
        GroupServer(
            QPermissionGroup(db).groupName.eq(groupName).findOne()!!.id!!,
            QServer(db).name.eq(serverName).findOne()!!.id!!
        ).insert()
    }

    suspend fun removePermissionGS(groupName: String, serverName: String) {
        QGroupServer(db)
            .groupId.eq(QPermissionGroup(db).groupName.eq(groupName).findOne()!!.id!!)
            .serverId.eq(QServer(db).name.eq(serverName).findOne()!!.id!!)
            .delete()
    }

    suspend fun addPermissionUG(userName: String, groupName: String) {
        UserGroup(
            QUser(db).username.eq(userName).findOne()!!.id!!,
            QPermissionGroup(db).groupName.eq(groupName).findOne()!!.id!!
        ).insert()
    }

    suspend fun removePermissionUG(userName: String, groupName: String) {
        QUserGroup(db)
            .userId.eq(QUser(db).username.eq(userName).findOne()!!.id!!)
            .groupId.eq(QPermissionGroup(db).groupName.eq(groupName).findOne()!!.id!!)
            .delete()
    }
}