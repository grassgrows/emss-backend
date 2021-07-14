package top.warmthdawn.emss.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import top.warmthdawn.emss.features.file.FileException
import top.warmthdawn.emss.features.file.FileExceptionMsg
import top.warmthdawn.emss.features.file.PathException
import top.warmthdawn.emss.features.file.PathExceptionMsg
import top.warmthdawn.emss.utils.Code
import top.warmthdawn.emss.utils.R

/**
 *
 * @author WarmthDawn
 * @since 2021-07-11
 */

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> {
            R.error(it)
            throw it
        }

        exception<PathException> {
            when(it.pathExceptionMsg) {
                PathExceptionMsg.INSUFFICIENT_PERMISSION_LEVEL->{
                    R.error(Code.InsufficientPermissionLevel, "您没有权限访问该目录")
                    call.response.status(HttpStatusCode.NonAuthoritativeInformation)
                }
                PathExceptionMsg.WRONG_PATH_FORMAT->{
                    R.error(Code.WrongPathFormat, "您输入的目录格式出错")
                    call.response.status(HttpStatusCode.NotFound)
                }


            }
        }

        exception<FileException> {
            when(it.fileExceptionMsg) {
                FileExceptionMsg.FILE_NOT_FOUND->{
                    R.error(Code.FileNotFound, "您访问的文件未找到")
                    call.response.status(HttpStatusCode.NotFound)
                }
                FileExceptionMsg.INVALID_FILE_NAME->{
                    R.error(Code.InvalidFileName, "您输入的文件名为空")
                    call.response.status(HttpStatusCode.NotFound)
                }
            }
        }


        status(HttpStatusCode.NotFound) {
            R.error(Code.NotFound, "您请求的页面不存在")
            call.response.status(HttpStatusCode.NotFound)
        }
    }
}