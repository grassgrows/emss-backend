package top.warmthdawn.emss.utils;

import io.ebean.DB
import io.ebean.Model
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bouncycastle.math.raw.Mod
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * @author WarmthDawn
 * @since 2021-07-09
 */
object JsonDateSerializer : KSerializer<LocalDateTime> {
    private val df: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), df)
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(df.format(value))
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DateSerializer", PrimitiveKind.STRING)
}

object EbeanEntitySerializer : KSerializer<Model> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("EbeanEntitySerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Model) {
        encoder.encodeString(DB.json().toJson(value))
    }

    override fun deserialize(decoder: Decoder): Model {
        throw UnsupportedOperationException()
    }

}
