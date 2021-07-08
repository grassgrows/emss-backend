package top.warmthdawn.emss.database.entity

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

interface DockerImage : Entity<DockerImage> {
    val dockerImageName: String
    val dockerfileRootDirectory: String
}

object DockerImages : Table<DockerImage>("t_docker_image") {
    val dockerImageName = varchar("docker_image_name").primaryKey().bindTo { it.dockerImageName }
    val dockerfileRootDirectory = varchar("dockerfile_root_directory").bindTo { it.dockerfileRootDirectory }
}