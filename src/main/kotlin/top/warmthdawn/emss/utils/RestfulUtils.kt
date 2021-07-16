package top.warmthdawn.emss.utils

import com.fasterxml.jackson.annotation.JsonValue
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

/**
 *
 * @author WarmthDawn
 * @since 2021-07-11
 */

val PipelineContext<*, ApplicationCall>.R: RestApplication get() = RestApplication(this.context)

class RestApplication(val call: ApplicationCall) {
    suspend fun ok() {
        call.respond(ApiResult(Code.Success, Unit, "请求成功"))
    }
    suspend inline fun <reified T : Any> ok(data: T, msg: String = "请求成功") {
        call.respond(ApiResult(Code.Success, data, "请求成功"))
    }

    suspend fun error(code: Code, message: String,
                      statusCode: HttpStatusCode = HttpStatusCode.InternalServerError) {
        call.respond(statusCode, ApiResult(code, Unit, message))
    }

    suspend fun error(e: Throwable, statusCode: HttpStatusCode = HttpStatusCode.InternalServerError) {
        call.respond(statusCode, ApiResult(Code.UnknownError, Unit, e.message))
    }

    suspend fun unexpected(message: String) {
        call.respond(HttpStatusCode.InternalServerError, ApiResult(Code.UnexpectedError, Unit, message))
    }
}

data class ApiResult<T>(
    val code: Code,
    val data: T,
    val msg: String?,
)
/**
 * 错误码
 */
enum class Code(
    @JsonValue
    val code: String
) {
    /**
     * 成功
     */
    Success("0000"),
    //用户错误
    /**
     * 请求参数不对
     */
    BadRequest("A110"),

    /**
     * 请求404
     */
    NotFound("A120"),

    //已知错误
    //Docker相关
    //Image
    /**
     * Image下载失败
     */
    ImageDownloadFailed("B112"),
    /**
     * Image未找到
     */
    ImageNotFound("B120"),
    /**
     * Image未下载
     */
    ImageNotDownloaded("B122"),
    /**
     * Image删除失败
     */
    ImageRemoveFailed("B140"),
    /**
     * Image的数据库信息删除失败
     */
    ImageDatabaseRemoveFailed("B142"),
    /**
     * Image删除时正在被使用
     */
    ImageRemoveWhenUsed("B144"),
    //服务器（容器）
    /**
     * 服务器未找到
     */
    ServerNotFound("B220"),
    /**
     *  服务器未启动
     */
    ServerNotRunning("B230"),
    /**
     * 服务器删除失败
     */
    ServerRemoveFailed("B240"),
    /**
     * 服务器的数据库信息删除失败
     */
    ServerDatabaseRemoveFailed("B242"),


    //系统错误
    /**
     * 未知系统错误
     */
    UnknownError("C910"),

    /**
     * 明明不可能发生的错误它却发生了
     */
    UnexpectedError("C920"),

    //文件相关
    /**
     * 当前请求用户权限等级不足
     */
    InsufficientPermissionLevel("D111"),

    /**
     * 请求文件路径格式不对
     */
    WrongPathFormat("D112"),

    /**
     * 请求文件未找到
     */
    FileNotFound("D113"),

    /**
     * 请求文件更名为空
     */
    InvalidFileName("D114"),

    /**
     * 请求文件目录未找到
     */
    DirectoryNotFound("D115"),

    /**
     * 请求文件路径出错(可能是请求了一个文件的地址)
     */
    InvalidDirectoryName("D116"),

    /**
     * 请求文件已存在
     */
    FileAlreadyExist("D117"),

    /**
     * 上传请求出错
     */
    NonUploaderRequest("D120"),
    /**
     * 上传文件出错：文件块数量出错
     */
    InvalidUploaderRequest1("D121"),
    /**
     * 上传文件出错：文件块大小超出限制
     */
    InvalidUploaderRequest2("D122"),
    /**
     * 上传文件出错：文件块大小出错
     */
    InvalidUploaderRequest3("D123"),
    /**
     * 上传文件出错：最后一个文件块出错
     */
    InvalidUploaderRequest4("D124"),
    /**
     * 上传文件出错：单文件块文件大小出错
     */
    InvalidUploaderRequest5("D125"),

}

