package top.warmthdawn.emss.features.file

import io.ebean.Database
import io.ktor.http.*
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
    fun fileListTest(){
        withTestServer {
            val db by application.inject<Database>()
            val root = QSetting(db).type.eq(SettingType.ServerRootDirectory).findOne()!!
            root.value = "D:\\Test"
            root.update()




            handleRequest(HttpMethod.Get, "/file/list/root/新建文件夹").apply {

                Json.parseToJsonElement(response.content!!)
                    .jsonObject["data"]!!
                    .jsonObject["xxx"]!!
                    .jsonArray[0]
                    .jsonPrimitive.int
            }
        }
    }
}