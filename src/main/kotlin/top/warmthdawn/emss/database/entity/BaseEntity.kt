package top.warmthdawn.emss.database.entity

import io.ebean.Model
import io.ebean.annotation.Identity
import io.ebean.annotation.WhenCreated
import io.ebean.annotation.WhenModified
import java.time.Instant
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.MappedSuperclass

/**
 *
 * @author WarmthDawn
 * @since 2021-07-09
 */
@MappedSuperclass
abstract class BaseEntity : Model() {
    @Identity
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    @WhenCreated
    lateinit var whenCreated: Instant

    @WhenModified
    lateinit var whenModified: Instant
}