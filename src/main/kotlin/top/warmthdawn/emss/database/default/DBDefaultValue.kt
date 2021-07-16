package top.warmthdawn.emss.database.default

import io.ebean.Database
import io.ktor.application.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.database.entity.Image
import top.warmthdawn.emss.database.entity.Setting
import top.warmthdawn.emss.database.entity.SettingType
import top.warmthdawn.emss.database.entity.query.QSetting
import top.warmthdawn.emss.features.file.FileService

/**
 *
 * @author WarmthDawn
 * @since 2021-07-09
 */

fun Application.tryInitDefault() {
    val db by inject<Database>()
    val fileService by inject<FileService>()

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
}