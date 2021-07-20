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
}

class JWTException(
    val jwtExceptionMsg: JWTExceptionMsg
) : Exception(jwtExceptionMsg.toString())

enum class JWTExceptionMsg {
    CREDENTIAL_WRONG,
}