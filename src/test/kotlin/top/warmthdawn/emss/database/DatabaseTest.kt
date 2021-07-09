package top.warmthdawn.emss.database

import io.ebean.DB
import io.ktor.http.*
import io.ktor.server.testing.*
import top.warmthdawn.emss.database.entity.Setting
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.utils.withTestServer
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseTest {

    @Test
    fun testRoot() {
        withTestServer {
            if(!QSetting().type.eq(SettingType.Name).exists()){
                DB.insert(Setting(SettingType.Name, "EMSS"))
            }else{
                DB.update(Setting(SettingType.Name, "EMSS"))
            }
            assertEquals(QSetting().type.eq(SettingType.Name).findOne()!!.value, "EMSS")
        }
    }
}