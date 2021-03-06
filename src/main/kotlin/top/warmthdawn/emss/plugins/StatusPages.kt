package top.warmthdawn.emss.plugins

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import top.warmthdawn.emss.features.docker.ContainerException
import top.warmthdawn.emss.features.docker.ContainerExceptionMsg
import top.warmthdawn.emss.features.docker.ImageException
import top.warmthdawn.emss.features.docker.ImageExceptionMsg
import top.warmthdawn.emss.features.file.*
import top.warmthdawn.emss.features.permission.PermissionException
import top.warmthdawn.emss.features.permission.PermissionExceptionMsg
import top.warmthdawn.emss.features.login.JWTException
import top.warmthdawn.emss.features.login.JWTExceptionMsg
import top.warmthdawn.emss.features.login.LoginException
import top.warmthdawn.emss.features.login.LoginExceptionMsg
import top.warmthdawn.emss.features.server.ServerException
import top.warmthdawn.emss.features.server.ServerExceptionMsg
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

        exception<MismatchedInputException> {
            R.error(it, HttpStatusCode.BadRequest)
        }

        exception<FileException> {
            when (it.fileExceptionMsg) {
                FileExceptionMsg.FILE_NOT_FOUND -> {
                    R.error(Code.FileNotFound, "您请求的文件未找到", HttpStatusCode.NotFound)
                }
                FileExceptionMsg.INVALID_FILE_NAME -> {
                    R.error(Code.InvalidFileName, "您请求的文件名为空", HttpStatusCode.BadRequest)
                }
                FileExceptionMsg.DIRECTORY_NOT_FOUND -> {
                    R.error(Code.DirectoryNotFound, "您请求的文件目录未找到", HttpStatusCode.NotFound)
                }
                FileExceptionMsg.INVALID_DIRECTORY_NAME -> {
                    R.error(
                        Code.InvalidDirectoryName, "您请求的文件路径出错(可能是请求了一个文件的地址)",
                        HttpStatusCode.BadRequest
                    )
                }
                FileExceptionMsg.FILE_ALREADY_EXIST -> {
                    R.error(Code.FileAlreadyExist, "您请求的文件已存在", HttpStatusCode.BadRequest)
                }
                FileExceptionMsg.FILE_SIZE_TOO_LARGE -> {
                    R.error(Code.FileSizeTooLarge, "文件体积过大", HttpStatusCode.BadRequest)
                }
                FileExceptionMsg.WRONG_PATH_FORMAT -> {
                    R.error(Code.WrongPathFormat, "您输入的目录格式出错", HttpStatusCode.NotFound)
                }
            }
        }

        exception<FileChunkException> {
            when (it.fileChunkExceptionMsg) {
                FileChunkExceptionMsg.NON_UPLOADER_REQUEST -> {
                    R.error(Code.NonUploaderRequest, "您的上传请求出错", HttpStatusCode.BadRequest)
                }
                FileChunkExceptionMsg.INVALID_UPLOADER_REQUEST1 -> {
                    R.error(
                        Code.InvalidUploaderRequest1, "上传文件出错：文件块数量出错",
                        HttpStatusCode.InternalServerError
                    )
                }
                FileChunkExceptionMsg.INVALID_UPLOADER_REQUEST2 -> {
                    R.error(
                        Code.InvalidUploaderRequest2, "上传文件出错：文件块大小超出限制",
                        HttpStatusCode.PayloadTooLarge
                    )
                }
                FileChunkExceptionMsg.INVALID_UPLOADER_REQUEST3 -> {
                    R.error(
                        Code.InvalidUploaderRequest3, "上传文件出错：文件块大小出错",
                        HttpStatusCode.InternalServerError
                    )
                }
                FileChunkExceptionMsg.INVALID_UPLOADER_REQUEST4 -> {
                    R.error(
                        Code.InvalidUploaderRequest4, "上传文件出错：最后一个文件块出错",
                        HttpStatusCode.InternalServerError
                    )
                }
                FileChunkExceptionMsg.INVALID_UPLOADER_REQUEST5 -> {
                    R.error(
                        Code.InvalidUploaderRequest5, "上传文件出错：单文件块文件大小出错",
                        HttpStatusCode.InternalServerError
                    )
                }
            }
        }

        exception<ImageException> {
            when (it.imageExceptionMsg) {
                ImageExceptionMsg.IMAGE_NOT_FOUND -> {
                    R.error(Code.ImageNotFound, "查无对应镜像！", HttpStatusCode.NotFound)
                }
                ImageExceptionMsg.IMAGE_NOT_DOWNLOADED -> {
                    R.error(Code.ImageNotDownloaded, "镜像未下载！", HttpStatusCode.NotFound)
                }
                ImageExceptionMsg.IMAGE_REMOVE_FAILED -> {
                    R.error(Code.ImageRemoveFailed, "镜像删除失败！", HttpStatusCode.InternalServerError)
                }
                ImageExceptionMsg.IMAGE_DATABASE_REMOVE_FAILED -> {
                    R.error(Code.ImageDatabaseRemoveFailed, "镜像数据库信息删除失败！", HttpStatusCode.InternalServerError)
                }
                ImageExceptionMsg.IMAGE_REMOVE_WHEN_USED -> {
                    R.error(Code.ImageRemoveWhenUsed, "镜像正在被使用中！请删除使用该镜像的服务器！", HttpStatusCode.Forbidden)
                }
            }
        }
        exception<ContainerException> {
            when (it.containerExceptionMsg) {
                ContainerExceptionMsg.CONTAINER_GET_INFO_FAILED -> {
                    R.error(Code.ContainerGetInfoFailed, "获取容器信息失败！", HttpStatusCode.InternalServerError)
                }
            }
        }

        exception<ServerException> {
            when (it.serverExceptionMsg) {
                ServerExceptionMsg.SERVER_NOT_FOUND -> {
                    R.error(Code.ServerNotFound, "查无对应服务器！", HttpStatusCode.NotFound)
                }
                ServerExceptionMsg.SERVER_NOT_RUNNING -> {
                    R.error(Code.ServerNotRunning, "服务器未启动！", HttpStatusCode.Forbidden)
                }
                ServerExceptionMsg.SERVER_REMOVE_FAILED -> {
                    R.error(Code.ServerRemoveFailed, "服务器删除失败！", HttpStatusCode.InternalServerError)
                }
                ServerExceptionMsg.SERVER_DATABASE_REMOVE_FAILED -> {
                    R.error(Code.ServerDatabaseRemoveFailed, "服务器数据库信息删除失败！", HttpStatusCode.InternalServerError)
                }
                ServerExceptionMsg.SERVER_NOT_STOPPED -> {
                    R.error(Code.ServerNotStopped,"服务器正在运行！", HttpStatusCode.Forbidden)
                }
            }
        }

        exception<JWTException> {
            when (it.jwtExceptionMsg) {
                JWTExceptionMsg.CREDENTIAL_WRONG -> {
                    R.error(Code.CredentialWrong, "JWT证书验证错误！", HttpStatusCode.Unauthorized)
                }
            }
        }

        exception<LoginException> {
            when (it.loginExceptionMsg) {
                LoginExceptionMsg.USERNAME_OR_PASSWORD_WRONG -> {
                    R.error(Code.UserNameOrPasswordWrong, "用户名或密码错误！", HttpStatusCode.Forbidden)
                }
                LoginExceptionMsg.USER_NOT_FOUND -> {
                    R.error(Code.UserNotFound, "用户未找到！", HttpStatusCode.NotFound)
                }
                LoginExceptionMsg.PASSWORD_WRONG -> {
                    R.error(Code.PasswordWrong, "密码错误！", HttpStatusCode.Unauthorized)
                }
                LoginExceptionMsg.USERNAME_ILLEGAL -> {
                    R.error(Code.UserNameIllegal, "用户名不合法！用户名只能为大小写字母、数字或下划线，且长度为3~20个字符！", HttpStatusCode.Forbidden)
                }
                LoginExceptionMsg.PASSWORD_ILLEGAL -> {
                    R.error(Code.PasswordIllegal, "密码不合法！密码只能为大小写字母或数字，且长度为6~20个字符！", HttpStatusCode.Forbidden)
                }
                LoginExceptionMsg.USERNAME_HAVE_BEEN_USED -> {
                    R.error(Code.UserNameHaveBeenUsed, "用户名已被使用！", HttpStatusCode.Forbidden)
                }
            }
        }

        status(HttpStatusCode.NotFound) {
            R.error(Code.NotFound, "您请求的页面不存在", HttpStatusCode.NotFound)
        }
        status(HttpStatusCode.Unauthorized) {
            R.error(Code.CredentialWrong, "JWT证书验证错误！", HttpStatusCode.Unauthorized)
        }


        exception<PermissionException> {
            when (it.permissionExceptionMsg) {
                PermissionExceptionMsg.INSUFFICIENT_PERMISSION_LEVEL -> {
                    R.error(Code.InsufficientPermissionLevel, "您的权限不足", HttpStatusCode.Forbidden)
                }
                PermissionExceptionMsg.GROUP_DATABASE_REMOVE_FAILED -> {
                    R.error(Code.GroupDatabaseRemoveFailed, "用户权限组删除失败", HttpStatusCode.InternalServerError)
                }
                PermissionExceptionMsg.USER_DATABASE_REMOVE_FAILED -> {
                    R.error(Code.UserDatabaseRemoveFailed, "用户删除失败", HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}