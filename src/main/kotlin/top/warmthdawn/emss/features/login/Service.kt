package top.warmthdawn.emss.features.login

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ebean.Database
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.database.entity.User
import top.warmthdawn.emss.database.entity.query.QUser
import top.warmthdawn.emss.database.entity.query.QUserGroup
import java.security.MessageDigest
import java.util.*


/**
 *
 * @author sunday7994
 * @date 2021/7/19
 */

class LoginService(
    private val db: Database,
    private val config: AppConfig
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

    // 签发证书
    fun sign(userId: Long): Map<String, String> {
        return AuthProvider.sign(userId, config.secretKeyConfig.authKey)
    }

    // 登陆验证
    fun loginValidate(username: String, password: String): Long
    {
        if(!QUser(db).username.eq(username).exists())
            throw LoginException(LoginExceptionMsg.USER_NOT_FOUND)

        val user = QUser(db).username.eq(username).findOne()!!
        if(sha256Encoder(password) != user.password)
            throw LoginException(LoginExceptionMsg.PASSWORD_WRONG)

        return user.id!!
    }

    // 创建用户
    fun createUser(username: String, password: String, permissionLevel: Int)
    {
        // 用户名只能为大小写字母、数字或下划线，且长度为3~20个字符
        val nameRegex = Regex("^[a-zA-Z0-9_]{3,20}$")
        // 密码只能为大小写字母或数字，且长度为6~20个字符
        val passwordRegex = Regex("^[a-zA-Z0-9]{6,20}$")
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
            permissionLevel
        ).insert()
    }

    // 修改用户名
    fun modifyUserName(userId: Long, newUsername: String)
    {
        if(!QUser(db).id.eq(userId).exists()) {
            throw LoginException(LoginExceptionMsg.USER_NOT_FOUND)
        }
        // 用户名只能为大小写字母、数字或下划线，且长度为3~20个字符
        val nameRegex = Regex("^[a-zA-Z0-9_]{3,20}$")
        if(!nameRegex.matches(newUsername))
        {
            throw LoginException(LoginExceptionMsg.USERNAME_ILLEGAL)
        }
        if(QUser(db).username.eq(newUsername).exists())
        {
            throw LoginException(LoginExceptionMsg.USERNAME_HAVE_BEEN_USED)
        }

        val user = QUser(db).id.eq(userId).findOne()!!
        user.username = newUsername
        user.update()
    }

    // 修改密码
    fun modifyPassword(userId: Long, password: String, newPassword: String)
    {
        if(!QUser(db).id.eq(userId).exists()) {
            throw LoginException(LoginExceptionMsg.USER_NOT_FOUND)
        }
        val user = QUser(db).id.eq(userId).findOne()!!
        if(sha256Encoder(password) != user.password)
        {
            throw LoginException(LoginExceptionMsg.PASSWORD_WRONG)
        }
        // 密码只能为大小写字母或数字，且长度为6~20个字符
        val passwordRegex = Regex("^[a-zA-Z0-9]{6,20}$")
        if(!passwordRegex.matches(newPassword))
        {
            throw LoginException(LoginExceptionMsg.PASSWORD_ILLEGAL)
        }

        user.password = sha256Encoder(newPassword)
        user.update()
    }

    // 加密器
    fun sha256Encoder(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val encodedPassword = config.secretKeyConfig.passwordFrontKey + password + config.secretKeyConfig.passwordBackKey
        val result = digest.digest(encodedPassword.toByteArray())
        return result.joinToString("") { "%02x".format(it) }
    }
}

