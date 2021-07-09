package top.warmthdawn.emss.database.entity

import kotlinx.serialization.Serializable
import top.warmthdawn.emss.utils.EbeanEntitySerializer
import javax.management.loading.ClassLoaderRepository
import javax.persistence.Entity


/**
 *
 * @author sunday7994
 * @date 2021/7/8
 */

//interface Image : Entity<Image> {
//}
//
//object Images : Table<Image>("t_docker_image") {
//    val name = varchar("name").primaryKey().bindTo { it.name }
//    val dockerfileRootDirectory = varchar("dockerfile_root_directory").bindTo { it.dockerfileRootDirectory }
//}
@Entity
class Image(
    var imageId: String? = null,
    var name: String,
    var repository: String,
    var tag: String = "latest",
    var canRemove: Boolean = true,
) : BaseEntity()
