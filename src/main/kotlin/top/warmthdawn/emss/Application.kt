package top.warmthdawn.emss

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.content.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.cio.*
import io.ktor.util.*
import top.warmthdawn.emss.plugins.*
import top.warmthdawn.emss.config.setupConfig
import top.warmthdawn.emss.di.appModule
import org.koin.core.module.Module
import top.warmthdawn.emss.features.login.AuthProvider
import top.warmthdawn.emss.features.login.JWTException
import top.warmthdawn.emss.features.login.JWTExceptionMsg
import top.warmthdawn.emss.features.login.dto.UserLoginDTO
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) = EngineMain.main(args)

@Suppress("unused")
fun Application.module(testing: Boolean = false, koinModules: List<Module> = listOf(appModule)) {
    configureKoin(koinModules)
    setupConfig(testing)
    configureDatabase()
    configureCors()
    configureLogging()
    configureSecurity()
    configureSerialization()
    configureStatusPages()
    install(Locations)
    install(ForwardedHeaderSupport)
    configureSockets()
    configureRouting()
}
