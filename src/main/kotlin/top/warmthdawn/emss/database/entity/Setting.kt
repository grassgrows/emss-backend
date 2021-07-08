package top.warmthdawn.emss.database.entity

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.enum
import org.ktorm.schema.int
import org.ktorm.schema.varchar

/**
 *
 * @author sunday7994
 * @date 2021/7/8
 */

enum class SettingType {
    Name,
    ServerRootDirectory
}

interface Setting : Entity<Setting> {
    companion object : Entity.Factory<Setting>()

    var type: SettingType
    var value: String
}

object Settings : Table<Setting>("t_settings") {
    val type = enum<SettingType>("type").primaryKey().bindTo { it.type }
    val value = varchar("value").bindTo { it.value }
}