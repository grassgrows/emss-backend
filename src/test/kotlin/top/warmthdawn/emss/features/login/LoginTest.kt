package top.warmthdawn.emss.features.login

import io.ebean.Database
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import org.bouncycastle.util.encoders.Hex
import org.junit.Ignore
import org.junit.Test
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.User
import top.warmthdawn.emss.utils.withTestServer
import java.security.MessageDigest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class LoginTest {
    @Test
    fun loginTest() {
        withTestServer {
            val db by application.inject<Database>()
            val loginService by application.inject<LoginService>()
            db.insert(
                User(
                    "takanashi",
                    loginService.sha256Encoder("123456789"),
                    1
                )
            )

            handleRequest(HttpMethod.Post, "/login") {
                setBody(buildJsonObject {
                    put("username", "takanashi")
                    put("password", "123456789")
                }.toString())
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())

            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val content = response.content
                assertNotNull(content)
                val json = Json.parseToJsonElement(content).jsonObject["data"]!!
                //assertEquals("xcvb", json.jsonObject["token"].toString())
            }

            handleRequest(HttpMethod.Post, "/login") {
                setBody(buildJsonObject {
                    put("username", "taka")
                    put("password", "123456789")
                }.toString())
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())

            }.apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }

            handleRequest(HttpMethod.Post, "/login") {
                setBody(buildJsonObject {
                    put("username", "takanashi")
                    put("password", "1234")
                }.toString())
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())

            }.apply {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }

            handleRequest(HttpMethod.Post, "/login") {
                setBody(buildJsonObject {
                    put("username", "takhi")
                    put("password", "1234")
                }.toString())
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())

            }.apply {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }

    }

    @Ignore
    @Test
    fun validateTest(){
        withTestServer {
            val db by application.inject<Database>()
            val loginService by application.inject<LoginService>()
            db.insert(
                User(
                    "takanashi",
                    loginService.sha256Encoder("123456789"),
                    1
                )
            )
            handleRequest(HttpMethod.Get, "/login/validate") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization,
                    "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJBdXRoZW50aWNhdGlvbiIsImF1ZCI6ImVtc3Mtand0LWF1ZGllbmNlIiwiaXNzIjoiaHR0cHM6Ly9lbXNzLndhcm10aGRhd24udG9wIiwiZXhwIjoxNjI3MDE1ODE0LCJ1c2VySWQiOjIsImlhdCI6MTYyNjkyOTQxNH0.596eDcBICDrxt4AJ8xN2MaXMJh6BW_zN1QCEa-ZQgFc")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

}

fun main()
{
    val strs = arrayOf("4sj293jd","Asjkfdskfj_sdfjdkfj239", "AsdjAdj五d","Bsdfd?dj","Asdj203_dj","aaa","aa","_dsffez")


    val regex = Regex("^[a-zA-Z0-9_]{3,20}$")
    for(item in strs)
    {
        if (regex.matches(item))
            print("*****************$item\n")
    }

    val password = "12345678"
    val digest = MessageDigest.getInstance("SHA-256")
    val result = digest.digest(password.toByteArray())
    print(result.joinToString("") { "%02x".format(it) })
}