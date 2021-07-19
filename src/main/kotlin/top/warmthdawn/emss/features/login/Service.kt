package top.warmthdawn.emss.features.login

import io.ebean.Database
import top.warmthdawn.emss.database.entity.PermissionGroup
import top.warmthdawn.emss.database.entity.query.QPermissionGroup
import top.warmthdawn.emss.database.entity.query.QUser
import top.warmthdawn.emss.database.entity.query.QUserGroup
import top.warmthdawn.emss.features.login.vo.UserInfoVO
import top.warmthdawn.emss.features.permission.vo.PermissionGroupVO


/**
 *
 * @author sunday7994
 * @date 2021/7/19
 */

class LoginService(
    private val db: Database
) {
    suspend fun getUserInfo(): List<UserInfoVO> {
        val result: MutableList<UserInfoVO> = mutableListOf()
        for (row in QUser(db).findList()) {
            val userInfoVO = UserInfoVO(
                row.id!!,
                row.username,
                row.permissionLevel,
                groupsOfUser(row.id!!)
            )
            result.add(userInfoVO)
        }
        return result
    }

    private fun groupsOfUser(id: Long): List<PermissionGroup> {
        val result: MutableList<PermissionGroup> = mutableListOf()
        for (row in QUserGroup(db).userId.eq(id).findList()) {
            result.plus(QPermissionGroup(db).id.eq(row.groupId).findList())
        }
        return result
    }
}

