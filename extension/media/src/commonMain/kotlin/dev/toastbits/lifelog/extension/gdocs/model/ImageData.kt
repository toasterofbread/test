package dev.toastbits.lifelog.extension.gdocs.model

class ImageData(
    val format: Format,
    val data: ByteArray
) {
    enum class Format {
        PNG
    }
}
