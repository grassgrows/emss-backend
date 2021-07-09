package top.warmthdawn.emss.database.entity

import io.ebean.Database
import io.ebean.Model
import io.ebean.annotation.Identity
import io.ebean.annotation.WhenCreated
import io.ebean.annotation.WhenModified
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.Id


/**
 *
 * @author sunday7994
 * @date 2021/7/8
 */
enum class SettingType {
    Name,
    ServerRootDirectory
}

@Entity
class Setting(
    @Identity
    @Id
    var type: SettingType,
    var value: String,

) : Model() {
    @WhenCreated
    lateinit var whenCreated: Instant
    @WhenModified
    lateinit var whenModified: Instant
}
