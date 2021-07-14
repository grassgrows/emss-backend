package top.warmthdawn.emss.features.file

import io.ebean.Database
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.*
import org.junit.Test
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QSetting
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
    fun getfileListTest(){
        withTestServer {
            val db by application.inject<Database>()
            val root = QSetting(db).type.eq(SettingType.ServerRootDirectory).findOne()!!
            root.value = "D:\\Test"
            root.update()

            val url =  "/file/list?${parametersOf("path" , "/root/新建文件夹").formUrlEncode()}"


            handleRequest(HttpMethod.Get, url).apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val setting = response.content
                assertNotNull(setting)
                val json = Json.parseToJsonElement(setting).jsonObject["data"]!!
                assertEquals(3, json.jsonArray.size)
                val path = json.jsonArray[0].jsonObject["filePath"]!!.jsonPrimitive.content
                assertEquals("/root/新建文件夹/新建文件夹", path)
            }



        }
    }

    @Test
    fun processPathTest(){
        withTestServer{
            val fileService by application.inject<FileService>()
            val root = QSetting().type.eq(SettingType.ServerRootDirectory).findOne()!!.value
            val path = fileService.processPath("/root/1").toFile().invariantSeparatorsPath
            assertEquals("$root/1", path)
        }
    }
}