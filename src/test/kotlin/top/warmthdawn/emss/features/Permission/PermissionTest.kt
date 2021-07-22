package top.warmthdawn.emss.features

import io.ebean.DB
import org.junit.Test
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.database.entity.Server
import top.warmthdawn.emss.database.entity.User
import top.warmthdawn.emss.database.entity.UserGroup
import top.warmthdawn.emss.database.entity.query.QPermissionGroup
import top.warmthdawn.emss.database.entity.query.QUserGroup
import top.warmthdawn.emss.features.permission.PermissionService
import top.warmthdawn.emss.utils.withTestServer
import kotlin.test.assertContentEquals
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

    @Test
    fun updatePermissionUGTest() {
        withTestServer {
            UserGroup(2,1, 2).insert()
            UserGroup(3,1, 2).insert()
            UserGroup(4,1, 2).insert()
            UserGroup(5,1, 2).insert()

            User("1111111", "11111111", 0).insert()
            User("2222222", "11111111", 1).insert()
            User("33333333", "11111111", 0).insert()
            User("44444444", "11111111", 1).insert()
            User("55555555", "11111111", 0).insert()

            val list = listOf<Long>(4,5,6)
            val permissionService by application.inject<PermissionService>()
            permissionService.updatePermissionUG(1,list)
            print(QUserGroup().findList().map { it.userId })
            assertContentEquals(
                list,
                QUserGroup().findList().map { it.userId }
            )
//            assertEquals(3, QUserGroup().findCount())
        }
    }
}