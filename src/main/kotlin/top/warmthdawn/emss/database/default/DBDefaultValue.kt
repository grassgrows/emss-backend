package top.warmthdawn.emss.database.default

import io.ebean.Database
import io.ktor.application.*
import org.koin.ktor.ext.inject
import top.warmthdawn.emss.database.entity.Image
import top.warmthdawn.emss.database.entity.Setting
import top.warmthdawn.emss.database.entity.SettingType

/**
 *
 * @author WarmthDawn
 * @since 2021-07-09
 */

fun Application.tryInitDefault() {
    val db by inject<Database>()

    //Image初始值
    if(!db.find(Image::class.java).exists()) {
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

    if(!db.find(Setting::class.java).exists()) {
        Setting(SettingType.NAME, "EMSS").save()
        Setting(SettingType.SERVER_ROOT_DIRECTORY, "~/emss/").save()
        Setting(SettingType.TEMPORARY_FOLDER, "~/emss/temp").save()
    }
}