package top.warmthdawn.emss.database.default

import io.ebean.Database
import io.ktor.application.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.config.AppConfig
import top.warmthdawn.emss.database.entity.*
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.features.file.FileService
import top.warmthdawn.emss.features.login.LoginService
import java.awt.desktop.AppEvent
import java.security.AuthProvider

/**
 *
 * @author WarmthDawn
 * @since 2021-07-09
 */

fun Application.tryInitDefault() {
    val db by inject<Database>()
    val loginService by inject<LoginService>()
    
    //Image初始值
    if (!db.find(Image::class.java).exists()) {
        Image(
            name = "OpenJDK 8",
            repository = "openjdk",
            tag = "8",
            canRemove = false
        ).save()

        Image(
            name = "OpenJDK Latest",
            repository = "openjdk",
            tag = "latest",
            canRemove = false
        ).save()
    }

    if (!QSetting().type.eq(SettingType.NAME).exists()) {
        Setting(SettingType.NAME, "EMSS").save()
    }
    if (!QSetting().type.eq(SettingType.SERVER_ROOT_DIRECTORY).exists()) {
        Setting(SettingType.SERVER_ROOT_DIRECTORY, "~/emss/").save()
    }
    if (!QSetting().type.eq(SettingType.SERVER_BACKUP_DIRECTORY).exists()) {
        Setting(SettingType.SERVER_BACKUP_DIRECTORY, "~/emss/backup/").save()
    }
    if (!QSetting().type.eq(SettingType.TEMPORARY_FOLDER).exists()) {
        Setting(SettingType.TEMPORARY_FOLDER, "~/emss/temp/").save()
    }

    if (!db.find(PermissionGroup::class.java).exists()) {
        PermissionGroup(
            groupName = "默认分组",
            maxPermissionLevel = -1,
        ).insert()
    }

    if (!db.find(User::class.java).exists()) {
        User(
            username = "admin",
            password = loginService.sha256Encoder("123456"),
            permissionLevel = 0
        ).insert()
    }

    if (!db.find(UserGroup::class.java).exists()) {
        UserGroup(
            userId = 1L,
            groupId = 1L,
            groupPermissionLevel = 0
        ).insert()
    }
}