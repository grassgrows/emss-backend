package top.warmthdawn.emss.database.entity

import javax.persistence.Entity


/**
 *
 * @author sunday7994
 * @date 2021/7/8
 */
val Database.settings get() = this.sequenceOf(Settings)

enum class SettingType {
    Name,
    ServerRootDirectory
}

@Entity
class Setting(
    var type: SettingType,
    var value: String
) : BaseEntity()

//interface Setting : Entity<Setting> {
//    companion object : Entity.Factory<Setting>()
//
//    var type: SettingType
//    var value: String
//}
//
//object Settings : Table<Setting>("t_settings") {
//    val type = enum<SettingType>("type").primaryKey().bindTo { it.type }
//    val value = varchar("value").bindTo { it.value }
//}