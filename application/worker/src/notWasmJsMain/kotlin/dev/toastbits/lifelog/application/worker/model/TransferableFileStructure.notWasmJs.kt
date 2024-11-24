package dev.toastbits.lifelog.application.worker.model

import dev.toastbits.kogit.core.filestructure.FileStructure
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
actual class TransferableFileStructure(
    @Serializable(with = DummySerializer::class)
    val structure: FileStructure
): FileStructure by structure

private object DummySerializer: KSerializer<FileStructure> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(
            "DummySerializer",
            PrimitiveKind.STRING
        )

    override fun serialize(encoder: Encoder, value: FileStructure) =
        throw IllegalStateException()

    override fun deserialize(decoder: Decoder): FileStructure =
        throw IllegalStateException()
}
