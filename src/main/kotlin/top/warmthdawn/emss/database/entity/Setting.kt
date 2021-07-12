package top.warmthdawn.emss.database.entity

import io.ebean.Model
import io.ebean.annotation.Identity
import io.ebean.annotation.WhenCreated
import io.ebean.annotation.WhenModified
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.Id


/**
 *
 * @author sunday7994
 * @date 2021/7/8
 */
enum class SettingType {
    Name,
    ServerRootDirectory,
    MAX_FILE_SIZE,
    TEMPORARY_FOLDER
}

@Entity
class Setting(
    @Identity
    @Id
    var type: SettingType,
    var value: String,

) : Model() {
    @WhenCreated
    lateinit var whenCreated: LocalDateTime
    @WhenModified
    lateinit var whenModified: LocalDateTime
}
