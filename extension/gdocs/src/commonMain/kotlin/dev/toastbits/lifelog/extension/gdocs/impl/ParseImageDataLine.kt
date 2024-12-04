package dev.toastbits.lifelog.extension.gdocs.impl

import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.core.specification.converter.alert.SpecificationLogParseAlert
import dev.toastbits.lifelog.extension.gdocs.model.ImageData
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

// [image01]: <data:image/png;base64,XYZ>
@OptIn(ExperimentalEncodingApi::class)
internal fun parseImageDataLine(
    line: String,
    onAlert: (LogParseAlert) -> Unit
): Pair<UInt, ImageData>? {
    if (!line.startsWith("[image")) {
        return null
    }

    val imageIndexEnd: Int = line.indexOf(']')
    if (imageIndexEnd == -1) {
        return null
    }

    val imageIndex: UInt? = line.substring(6, imageIndexEnd).toUIntOrNull()
    if (imageIndex == null) {
        return null
    }

    var head: Int = imageIndexEnd + 3

    if (!line.regionMatches(head, "<data:image/", 0, 12) || !line.endsWith('>')) {
        return null
    }

    head += 12

    val imageFormatEnd: Int = line.indexOf(';', head)
    if (imageFormatEnd == -1) {
        return null
    }

    val imageFormatName: String = line.substring(head, imageFormatEnd)
    val imageFormat: ImageData.Format? =
        ImageData.Format.entries.firstOrNull { it.name.equals(imageFormatName, ignoreCase = true) }

    if (imageFormat == null) {
        onAlert(SpecificationLogParseAlert.UnknownImageFormat(imageFormatName))
        return null
    }

    val dataEncodingEnd: Int = line.indexOf(',', imageFormatEnd + imageFormatName.length)
    if (dataEncodingEnd == -1) {
        return null
    }

    val encodedData: String = line.substring(dataEncodingEnd + 1, line.length - 1)

    val dataEncoding: String = line.substring(imageFormatEnd + 1, dataEncodingEnd)
    val decodedData: ByteArray =
        when (dataEncoding.lowercase()) {
            "base64" -> Base64.decode(encodedData)
            else -> {
                onAlert(SpecificationLogParseAlert.UnknownDataEncoding(dataEncoding))
                return null
            }
        }

    return Pair(imageIndex, ImageData(imageFormat, decodedData))
}
