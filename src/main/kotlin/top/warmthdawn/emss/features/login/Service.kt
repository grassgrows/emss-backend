package top.warmthdawn.emss.features.login

import io.ebean.Database
import top.warmthdawn.emss.database.entity.User
import top.warmthdawn.emss.database.entity.query.QUser
import java.security.MessageDigest


/**
 *
 * @author sunday7994
 * @date 2021/7/19
 */

class LoginService(
    private val db: Database
) {
    /*
    suspend fun getUserInfo(): List<UserInfoVO> {
        val result: MutableList<UserInfoVO> = mutableListOf()
        for (row in QUser(db).findList()) {
            val userInfoVO = UserInfoVO(
                row.id!!,
                row.username,
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
    */

    // 登陆验证
    fun loginValidate(username: String, password: String): Boolean
    {
        if(!QUser(db).username.eq(username).exists())
            return false

        val user = QUser(db).username.eq(username).findOne()!!
        return sha256Encoder(password) == user.password
    }

    // 创建用户
    fun createUser(username: String, password: String)
    {
        // 用户名只能为大小写字母、数字或下划线，且长度为3~20个字符
        val nameRegex = Regex("^[a-zA-Z0-9_]{3,20}$")
        // 密码只能为大小写字母或数字，且长度为8~20个字符
        val passwordRegex = Regex("^[a-zA-Z0-9]{8,20}$")
        if(!nameRegex.matches(username))
        {
            throw LoginException(LoginExceptionMsg.USERNAME_ILLEGAL)
        }
        if(QUser(db).username.eq(username).exists())
        {
            throw LoginException(LoginExceptionMsg.USERNAME_HAVE_BEEN_USED)
        }
        if(!passwordRegex.matches(password))
        {
            throw LoginException(LoginExceptionMsg.PASSWORD_ILLEGAL)
        }

        User(
            username,
            sha256Encoder(password),
            1
        ).insert()
    }

    fun sha256Encoder(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val result = digest.digest(password.toByteArray())
        return result.joinToString("") { "%02x".format(it) }
    }
}

