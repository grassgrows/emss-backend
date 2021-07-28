package top.warmthdawn.emss.plugins

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun Application.configureSerialization() {
    install(ContentNegotiation) {
//        json()
        jackson {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
            registerModule(JavaTimeModule().apply {
                addDeserializer(
                    LocalDateTime::class.java,
                    LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
                addSerializer(
                    LocalDateTime::class.java,
                    LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
            })
        }
    }
}
