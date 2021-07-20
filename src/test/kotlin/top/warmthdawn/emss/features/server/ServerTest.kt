package top.warmthdawn.emss.features.server

import io.ebean.Database
import io.ebean.annotation.DbJsonB
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.Test
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.query.QServer
import top.warmthdawn.emss.features.server.dto.ServerInfoDTO
import top.warmthdawn.emss.utils.json
import top.warmthdawn.emss.utils.withTestServer
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 *
 * @author WarmthDawn
 * @since 2021-07-09
 */
internal class ServerTest {


    @Test
    fun serverListTest() {
        withTestServer {
            val db by application.inject<Database>()
            db.insert(
                Server(
                    name = "Guilong",
                    aliasName = "贵龙",
                    abbr = "gl",
                    location = "guilong",
                    imageId = 1,
                    workingDir = "/data/",
                    portBindings = mapOf(522 to 522),
                    volumeBind = mapOf(),
                    startCommand = "java -jar forge-2847-universal.jar"
                )
            )
            db.insert(
                Server(
                    name = "Sunzhongyv",
                    aliasName = "孙",
                    abbr = "szy",
                    location = "sun",
                    imageId = 2,
                    workingDir = "/data/",
                    portBindings = mapOf(544 to 544),
                    volumeBind = mapOf(),
                    startCommand = "bash start.sh"
                )
            )

            handleRequest(HttpMethod.Get, "/server/list").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val setting = response.content
                assertNotNull(setting)
                val json = Json.parseToJsonElement(setting).jsonObject["data"]!!
                assertEquals(2, json.jsonArray.size)
            }

        }
    }


    @Test
    fun serverOperationTest() {
        withTestServer {
            handleRequest(HttpMethod.Post, "/server/1/start").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
            handleRequest(HttpMethod.Post, "/server/1/restart").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
            handleRequest(HttpMethod.Post, "/server/1/stop").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
            handleRequest(HttpMethod.Post, "/server/1/terminate").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
}
/*
internal class ServerUpdateTest {

    @Test
    fun createServiceTest() {
        withTestServer {
            val serverService by application.inject<ServerService>()
            val data = ServerInfoDTO(
                name = "Qwq",
                abbr = "qwq",
                location = "qwq",
                startCommand = "echo hello world",
                workingDir = "/data/",
                portBindings = mapOf(666 to 666),
                volumeBind = mapOf(),
                imageId = 1
            )
            serverService.createServerInfo(data)
        }
    }
    @Test
    fun serverTest() {
        withTestServer {
            val db by application.inject<Database>()
            handleRequest(HttpMethod.Post, "/server/create") {
                val data = ServerInfoDTO(
                    name = "Qwq",
                    abbr = "qwq",
                    location = "qwq",
                    startCommand = "echo hello world",
                    workingDir = "/data/",
                    portBindings = mapOf(666 to 666),
                    volumeBind = mapOf(),
                    imageId = 1
                )
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(json.writeValueAsString(data))
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(1, QServer(db).findList().size)
            }

        }
    }
}
 */