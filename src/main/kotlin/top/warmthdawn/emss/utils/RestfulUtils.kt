package top.warmthdawn.emss.utils

import com.fasterxml.jackson.annotation.JsonValue
import io.ktor.application.*
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

    suspend fun error(code: Code, message: String) {
        call.respond(ApiResult(code, Unit, message))
    }

    suspend fun error(e: Throwable) {
        call.respond(ApiResult(Code.UnknownError, Unit, e.message))
    }

    suspend fun expected(message: String) {
        call.respond(ApiResult(Code.UnexpectedError, Unit, message))
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
    /**
     * Image下载失败
     */
    ImageDownloadFailed("B112"),
    //系统错误
    /**
     * 未知系统错误
     */
    UnknownError("C910"),

    /**
     * 明明不可能发生的错误它却发生了
     */
    UnexpectedError("C920"),

}

