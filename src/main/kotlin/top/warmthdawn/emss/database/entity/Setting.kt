package top.warmthdawn.emss.database.entity

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

interface Setting : Entity<Setting> {
    companion object : Entity.Factory<Setting>()

    var name: String //系统名称
    var serverRootDirectory: String //系统服务端路径

}

object Settings : Table<Setting>("t_settings") {
    val name = varchar("name").bindTo { it.name }
    val serverRootDirectory = varchar(("server_root_directory")).bindTo { it.serverRootDirectory }
}