package top.warmthdawn.emss.features.login

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.features.login.dto.UserCreateDTO
import top.warmthdawn.emss.features.login.dto.UserDTO
import top.warmthdawn.emss.features.login.dto.UserModifyPasswordDTO
import top.warmthdawn.emss.utils.Code
import top.warmthdawn.emss.utils.R
import top.warmthdawn.emss.utils.checkPermission

/**
 * @author takanashi
 * @since 2021-07-19
 */

fun Route.loginEndpoint() {

    val loginService by inject<LoginService>()

    route("/login") {

        post {
            val user = call.receive<UserDTO>()
            if (loginService.loginValidate(user.username, user.password)) {
                R.ok(loginService.sign(user.username))
            } else {
                R.error(Code.UserNameOrPasswordWrong, "用户名或密码错误!", HttpStatusCode.Unauthorized)
            }
        }

        // TODO 测试，不留
        authenticate("auth-jwt") {
            get("/validate") {
                val name = (call.authentication.principal as JWTPrincipal).payload.getClaim("username")
                val issuedTime = (call.authentication.principal as JWTPrincipal).payload.issuedAt

                print("---------------------------- $name\n")
                val nameS = name.toString()
                print("---------------------------- $nameS\n")
                print("---------------------------- $issuedTime\n")
                R.ok()
            }
        }

    }

    route("/user") {
        authenticate("auth-jwt") {
            post("/create") {
                val user = call.receive<UserCreateDTO>()
                checkPermission(0)
                loginService.createUser(user.username, user.password, user.permissionLevel)
                R.ok()
            }
            route("/modify") {
                post("/username") {
                    val newName = call.receive<String>()
                    val name = (call.authentication.principal as JWTPrincipal).payload.getClaim("username").toString()
                    loginService.modifyUserName(name, newName)
                    R.ok()
                }
                post("/password") {
                    val user = call.receive<UserModifyPasswordDTO>()
                    val name = (call.authentication.principal as JWTPrincipal).payload.getClaim("username").toString()
                    loginService.modifyPassword(name, user.password, user.newPassword)
                    R.ok()
                }
            }
        }
    }
}

