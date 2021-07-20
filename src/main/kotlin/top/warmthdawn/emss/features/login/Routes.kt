package top.warmthdawn.emss.features.login

import com.auth0.jwk.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.docker.ContainerService
import top.warmthdawn.emss.features.login.dto.UserLoginDTO
import top.warmthdawn.emss.utils.Code
import top.warmthdawn.emss.utils.R
import java.util.concurrent.TimeUnit

/**
 * @author takanashi
 * @since 2021-07-19
 */

fun Route.loginEndpoint() {

    val loginService by inject<LoginService>()

    route("/login") {

        post {
            val user = call.receive<UserLoginDTO>()
            if (loginService.loginValidate(user.username, user.password)) {
                R.ok(AuthProvider.sign(user.username))
            } else {
                R.error(Code.UserNameOrPasswordWrong, "用户名或密码错误!", HttpStatusCode.Unauthorized)
            }
        }

        authenticate("auth-jwk") {
            // TODO 测试，不留
            get("/validate") {
                val name = (call.authentication.principal as JWTPrincipal).payload.getClaim("username")
                val issuedTime = (call.authentication.principal as JWTPrincipal).payload.issuedAt

                print("---------------------------- $name\n")
                print("---------------------------- $issuedTime\n")
                R.ok()
            }
        }

    }


}
