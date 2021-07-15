package top.warmthdawn.emss.features.file

import io.ebean.Database
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import org.junit.Test
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.utils.json
import top.warmthdawn.emss.utils.withTestServer
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


/**
 *
 * @author sunday7994
 * @date 2021/7/14
 */

internal class FileTest {
    @Test
    fun processPathTest() {
        withTestServer {
            val fileService by application.inject<FileService>()
            val root = QSetting().type.eq(SettingType.SERVER_ROOT_DIRECTORY).findOne()!!.value
            val path = fileService.processPath("/root/1").toFile().invariantSeparatorsPath
            assertEquals(root + "1", path)
        }
    }

    @Test
    fun getfileListTest() {
        withTestServer {
            val db by application.inject<Database>()
            val root = QSetting(db).type.eq(SettingType.SERVER_ROOT_DIRECTORY).findOne()!!
            root.value = "D:\\Test"
            root.update()

            val url = "/file/list?${parametersOf("path", "/root/新建文件夹").formUrlEncode()}"

            handleRequest(HttpMethod.Get, url).apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val apiResult = response.content
                assertNotNull(apiResult)
                val json = Json.parseToJsonElement(apiResult).jsonObject["data"]!!
                assertEquals(7, json.jsonArray.size)
                val path = json.jsonArray[0].jsonObject["filePath"]!!.jsonPrimitive.content
                assertEquals("/root/新建文件夹/1", path)
            }
        }
    }

    @Test
    fun createDirsTest() {
        withTestServer {
            val db by application.inject<Database>()
            val root = QSetting(db).type.eq(SettingType.SERVER_ROOT_DIRECTORY).findOne()!!
            root.value = "D:\\Test"
            root.update()

            val url1 = "/file/create?${parametersOf("path", "/root/新建文件夹/1").formUrlEncode()}"

            handleRequest(HttpMethod.Post, url1).apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            val url2 = "/file/list?${parametersOf("path", "/root/新建文件夹").formUrlEncode()}"

            handleRequest(HttpMethod.Get, url2).apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val apiResult = response.content
                assertNotNull(apiResult)
                val json = Json.parseToJsonElement(apiResult).jsonObject["data"]!!
                assertEquals(7, json.jsonArray.size)
                val path = json.jsonArray[0].jsonObject["filePath"]!!.jsonPrimitive.content
                assertEquals("/root/新建文件夹/1", path)
            }
        }
    }

    @Test
    fun downloadTest() {
        withTestServer {
            val db by application.inject<Database>()
            val root = QSetting(db).type.eq(SettingType.SERVER_ROOT_DIRECTORY).findOne()!!
            root.value = "D:\\Test"
            root.update()

            val url = "/file/download?${parametersOf("path", "/root/新建文件夹/ktor_logo.png").formUrlEncode()}"

            handleRequest(HttpMethod.Get, url).apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val content = response.byteContent ?: ByteArray(4)

                fun assertPNG(array: ByteArray) {
                    assertEquals((0x89).toByte(), array[0])
                    assertEquals((0x50).toByte(), array[1])
                    assertEquals((0x4E).toByte(), array[2])
                    assertEquals((0x47).toByte(), array[3])
                }

                assertPNG(content)
                assertEquals(
                    "attachment; filename=ktor_logo.png",
                    response.headers[HttpHeaders.ContentDisposition]
                )
            }
        }
    }

    @Test
    fun renameFileTest() {
        withTestServer {
            val db by application.inject<Database>()
            val root = QSetting(db).type.eq(SettingType.SERVER_ROOT_DIRECTORY).findOne()!!
            root.value = "D:\\Test"
            root.update()

            val url1 = "/file/rename?${
                parametersOf(
                    "path" to listOf("/root/新建文件夹/1.txt"),
                    "newName" to listOf("2.txt")
                ).formUrlEncode()
            }"

            handleRequest(HttpMethod.Post, url1) {}.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            val url2 = "/file/search?${
                parametersOf(
                    "path" to listOf("/root/新建文件夹"),
                    "keyword" to listOf("2.txt")
                ).formUrlEncode()
            }"

            handleRequest(HttpMethod.Get, url2).apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val apiResult = response.content
                assertNotNull(apiResult)
                val json = Json.parseToJsonElement(apiResult).jsonObject["data"]!!
                assertEquals(1, json.jsonArray.size)
                val path = json.jsonArray[0].jsonObject["filePath"]!!.jsonPrimitive.content
                assertEquals("/root/新建文件夹/2.txt", path)
            }
        }
    }

    @Test
    fun searchFileTest() {
        withTestServer {
            val db by application.inject<Database>()
            val root = QSetting(db).type.eq(SettingType.SERVER_ROOT_DIRECTORY).findOne()!!
            root.value = "D:\\Test"
            root.update()

            val url = "/file/search?${
                parametersOf(
                    "path" to listOf("/root/新建文件夹"),
                    "keyword" to listOf("新")
                ).formUrlEncode()
            }"

            handleRequest(HttpMethod.Get, url).apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val apiResult = response.content
                assertNotNull(apiResult)
                val json = Json.parseToJsonElement(apiResult).jsonObject["data"]!!
                assertEquals(3, json.jsonArray.size)
                val path = json.jsonArray[0].jsonObject["filePath"]!!.jsonPrimitive.content
                assertEquals("/root/新建文件夹/新建文件夹", path)
            }
        }
    }

    @Test
    fun copyFileTest() {
        withTestServer {
            val db by application.inject<Database>()
            val root = QSetting(db).type.eq(SettingType.SERVER_ROOT_DIRECTORY).findOne()!!
            root.value = "D:\\Test"
            root.update()

//            val fileService by application.inject<FileService>()
//            val list = fileService.searchFile("/root/新建文件夹", "新")
//            assertEquals(3, list.size)
//            assertEquals("新建文件夹 (2)", list[1].fileName)

            val url = "/file/copy?${parametersOf("path", "/root/新建文件夹/新建文件夹").formUrlEncode()}"

            handleRequest(HttpMethod.Post, url) {
                val data = listOf("/root/新建文件夹/1.txt", "/root/新建文件夹/2.txt")
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(json.writeValueAsString(data))
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun deleteFileTest() {
        withTestServer {
            val db by application.inject<Database>()
            val root = QSetting(db).type.eq(SettingType.SERVER_ROOT_DIRECTORY).findOne()!!
            root.value = "D:\\Test"
            root.update()

            handleRequest(HttpMethod.Post, "/file/delete") {
                val data = listOf("/root/新建文件夹/新建文件夹/1.txt", "/root/新建文件夹/新建文件夹/2.txt")
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(json.writeValueAsString(data))
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun cutFileTest() {
        withTestServer {
            val db by application.inject<Database>()
            val root = QSetting(db).type.eq(SettingType.SERVER_ROOT_DIRECTORY).findOne()!!
            root.value = "D:\\Test"
            root.update()

            val url = "/file/cut?${parametersOf("path", "/root/新建文件夹/新建文件夹").formUrlEncode()}"

            handleRequest(HttpMethod.Post, url) {
                val data = listOf("/root/新建文件夹/1.txt", "/root/新建文件夹/2.txt")
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(json.writeValueAsString(data))
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
}