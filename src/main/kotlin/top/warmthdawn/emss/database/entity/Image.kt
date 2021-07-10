package top.warmthdawn.emss.database.entity

import javax.persistence.Entity

/**
 *
 * @author sunday7994
 * @date 2021/7/8
 */

@Entity
class Image(
    var imageId: String? = null,
    var name: String,
    var repository: String,
    var tag: String = "latest",
    var canRemove: Boolean = true,
) : BaseEntity()
