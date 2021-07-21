package top.warmthdawn.emss.features

import io.ebean.DB
import io.ebean.Database
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QPermissionGroup
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.utils.json
import top.warmthdawn.emss.utils.withTestServer
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 *
 * @author sunday7994
 * @date 2021/7/21
 */
internal class PermissionTest {
    @Test
    fun deleteFileTest() {
        withTestServer(useMemoryDB = false) {
            Server(
                "test",
                "test",
                "test",
                "/test",
                "",
                0L,
                "/working",
            ).insert()
            val group = QPermissionGroup().id.eq(1).findOne()!!
            group.permittedLocation = listOf("/root", "/backup")
            group.update()

            val result = DB.sqlQuery(
                "SELECT DISTINCT(LOCATION) FROM SERVER\n" +
                        "INNER JOIN GROUP_SERVER GS ON SERVER.ID = GS.SERVER_ID\n" +
                        "INNER JOIN USER_GROUP UG ON GS.GROUP_ID = UG.GROUP_ID\n" +
                        "INNER JOIN USER ON UG.USER_ID = USER.ID\n" +
                        "WHERE USERNAME=:username"
            )
                .setParameter("username", "admin OR 1=1")
//                .setParameter("username", "admin")
                .findList()
            assertTrue {
                result.size == 0
            }
        }
    }
}