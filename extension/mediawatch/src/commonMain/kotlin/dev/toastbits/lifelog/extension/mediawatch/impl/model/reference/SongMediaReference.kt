package dev.toastbits.lifelog.extension.mediawatch.impl.model.reference

import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtension
import dev.toastbits.lifelog.extension.mediawatch.model.reference.MediaReference
import dev.toastbits.lifelog.extension.mediawatch.util.MediaEntityType

data class SongMediaReference(
    override val mediaId: String,
    override val referenceTypeId: ExtensionId
): MediaReference() {
    override val extensionId: ExtensionId = MediaWatchExtension.ID
    override val mediaType: MediaEntityType = MediaEntityType.SONG
}
