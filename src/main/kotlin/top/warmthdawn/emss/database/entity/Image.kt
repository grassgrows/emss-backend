package top.warmthdawn.emss.database.entity

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

/**
 *
 * @author sunday7994
 * @date 2021/7/8
 */

interface Image : Entity<Image> {
    val name: String
    var dockerfileRootDirectory: String
}

object Images : Table<Image>("t_docker_image") {
    val name = varchar("name").primaryKey().bindTo { it.name }
    val dockerfileRootDirectory = varchar("dockerfile_root_directory").bindTo { it.dockerfileRootDirectory }
}
