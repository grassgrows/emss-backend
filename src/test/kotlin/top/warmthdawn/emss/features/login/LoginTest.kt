package top.warmthdawn.emss.features.login

import io.ebean.Database
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import org.junit.Test
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.User
import top.warmthdawn.emss.utils.withTestServer
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

internal class LoginTest {
    @Test
    fun loginTest() {
        withTestServer {
            val db by application.inject<Database>()
            db.insert(
                User(
                    "takanashi",
                    "123456789",
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
                assertEquals(HttpStatusCode.Unauthorized, response.status())
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
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }

    }

    @Test
    fun validateTest(){
        withTestServer {
            handleRequest(HttpMethod.Get, "/login/validate") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization,
                    "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJBdXRoZW50aWNhdGlvbiIsImF1ZCI6ImVtc3Mtand0LWF1ZGllbmNlIiwiaXNzIjoiaHR0cHM6Ly9lbXNzLndhcm10aGRhd24udG9wIiwiZXhwIjoxNjI2ODUzNTk1LCJpYXQiOjE2MjY3NjcxOTUsInVzZXJuYW1lIjoidGFrYW5hc2hpIn0.HPu2oSuvHaoqOroqBIErVdhE8HiVLzBzxXT8JrqzqvY")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

}