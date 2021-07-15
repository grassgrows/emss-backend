package top.warmthdawn.emss.database

import io.ebean.DB
import top.warmthdawn.emss.database.entity.Setting
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.utils.withTestServer
import kotlin.test.Test
import kotlin.test.assertEquals

class DatabaseTest {

    @Test
    fun testRoot() {
        withTestServer {
            if(!QSetting().type.eq(SettingType.NAME).exists()){
                DB.insert(Setting(SettingType.NAME, "EMSS"))
            }else{
                DB.update(Setting(SettingType.NAME, "EMSS"))
            }
            assertEquals(QSetting().type.eq(SettingType.NAME).findOne()!!.value, "EMSS")
        }
    }
}