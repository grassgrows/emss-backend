package top.warmthdawn.emss.features.file


/**
 *
 * @author sunday7994
 * @date 2021/7/13
 */

class PathException(
    val pathExceptionMsg: PathExceptionMsg
):Exception(pathExceptionMsg.toString()){
}

enum class PathExceptionMsg{
    INSUFFICIENT_PERMISSION_LEVEL,
    WRONG_PATH_FORMAT
}