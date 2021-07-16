package top.warmthdawn.emss.features.server

/**
 * @author takanashi
 * @since 2021-07-16
 */

class ServerException(
    val serverExceptionMsg: ServerExceptionMsg
) : Exception(serverExceptionMsg.toString())

enum class ServerExceptionMsg {
    SERVER_NOT_FOUND,
    SERVER_NOT_RUNNING,
    SERVER_REMOVE_FAILED,
    SERVER_DATABASE_REMOVE_FAILED

}