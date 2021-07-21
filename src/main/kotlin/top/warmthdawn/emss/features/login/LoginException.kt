package top.warmthdawn.emss.features.login

/**
 * @author takanashi
 * @since 2021-07-20
 */

class LoginException(
    val loginExceptionMsg: LoginExceptionMsg
) : Exception(loginExceptionMsg.toString())

enum class LoginExceptionMsg {
    USERNAME_OR_PASSWORD_WRONG,
    USER_NOT_FOUND,
    PASSWORD_WRONG,
    USERNAME_ILLEGAL,
    PASSWORD_ILLEGAL,
    USERNAME_HAVE_BEEN_USED,
}

class JWTException(
    val jwtExceptionMsg: JWTExceptionMsg
) : Exception(jwtExceptionMsg.toString())

enum class JWTExceptionMsg {
    CREDENTIAL_WRONG,
}