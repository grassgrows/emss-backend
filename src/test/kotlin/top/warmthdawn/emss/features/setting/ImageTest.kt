package top.warmthdawn.emss.features.setting

import io.ebean.Database
import io.ebean.annotation.Transactional
import io.ebean.annotation.TxIsolation
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.database.entity.query.QImage
import top.warmthdawn.emss.features.settings.dto.ImageDTO
import top.warmthdawn.emss.utils.withTestServer
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 *
 * @author WarmthDawn
 * @since 2021-07-09
 */

internal class ImageTest {
    @Test
    fun downloadTest() {
        withTestServer {
            handleRequest(HttpMethod.Post, "/settings/image/1/download").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun statusTest() {
        withTestServer {
            handleRequest(HttpMethod.Get, "/settings/image/1/status").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun getTest() {
        withTestServer {
            handleRequest(HttpMethod.Get, "/settings/image/1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val image = response.content
                assertNotNull(image)
                val json = Json.parseToJsonElement(image)
                assertEquals("OpenJDK 8", json.jsonObject["name"]!!.jsonPrimitive.content)
                assertEquals("openjdk", json.jsonObject["repository"]!!.jsonPrimitive.content)
            }
        }
    }


}

internal class ImageUpdateTest {

    @Test
    fun createImageTest() {
        withTestServer {

            val db by application.inject<Database>()
            db.beginTransaction(TxIsolation.SERIALIZABLE)
            try {
                handleRequest(HttpMethod.Post, "/settings/image") {
                    val data = ImageDTO(name = "Ubuntu", repository = "ubuntu")
                    addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                    setBody(Json.encodeToString(data))
                }.apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                    assertTrue {
                        QImage(db).name.eq("Ubuntu").exists()
                    }
                }
            } finally {
                db.rollbackTransaction()
            }
        }
    }
}