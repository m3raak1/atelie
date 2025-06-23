package com.example.atelie

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object OffsetDateTimeSerializer : KSerializer<OffsetDateTime> {
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("OffsetDateTime", PrimitiveKind.STRING)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun serialize(encoder: Encoder, value: OffsetDateTime) {
        encoder.encodeString(value.format(formatter))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun deserialize(decoder: Decoder): OffsetDateTime {
        return OffsetDateTime.parse(decoder.decodeString(), formatter)
    }
}