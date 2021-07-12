package top.warmthdawn.emss.features.setting

import io.ebean.Database
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.features.settings.BaseSetting
import top.warmthdawn.emss.utils.json
import top.warmthdawn.emss.utils.withTestServer
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 *
 * @author WarmthDawn
 * @since 2021-07-09
 */
internal class BaseSettingTest {

    @Test
    fun getImagesTest() {
        withTestServer {
            handleRequest(HttpMethod.Get, "/settings/images").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val setting = response.content
                assertNotNull(setting)
                val json = Json.parseToJsonElement(setting).jsonObject["data"]!!
                assertEquals(2, json.jsonArray.size)
            }
        }
    }

    @Test
    fun getSettingTest() {
        withTestServer {
            handleRequest(HttpMethod.Get, "/settings/base").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val setting = response.content
                assertNotNull(setting)
                val json = Json.parseToJsonElement(setting).jsonObject["data"]!!
                assertEquals("EMSS", json.jsonObject["name"]!!.jsonPrimitive.content)
                assertEquals("~/emss/", json.jsonObject["serverRootDirectory"]!!.jsonPrimitive.content)
            }
        }
    }


}

internal class BaseSettingUpdateTest {
    @Test
    fun updateSettingTest() {
        withTestServer {
            val db by application.inject<Database>()
            handleRequest(HttpMethod.Post, "/settings/base") {
                val data = BaseSetting(name = "EMSS2")
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(json.writeValueAsString(data))
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val name = QSetting(db).type.eq(SettingType.Name).findOne()
                assertNotNull(name)
                assertEquals("EMSS2", name.value)
            }
        }
    }
}