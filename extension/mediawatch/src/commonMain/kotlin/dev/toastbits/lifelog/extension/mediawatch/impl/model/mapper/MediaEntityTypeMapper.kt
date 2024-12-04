package dev.toastbits.lifelog.extension.mediawatch.impl.model.mapper

import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.extension.mediawatch.impl.model.reference.BookMediaReference
import dev.toastbits.lifelog.extension.mediawatch.impl.model.reference.GameMediaReference
import dev.toastbits.lifelog.extension.mediawatch.impl.model.reference.MovieOrShowMediaReference
import dev.toastbits.lifelog.extension.mediawatch.impl.model.reference.SongMediaReference
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.BookMediaConsumeEvent
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.GameMediaConsumeEvent
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.MediaConsumeEvent
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.MovieOrShowMediaConsumeEvent
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.SongMediaConsumeEvent
import dev.toastbits.lifelog.extension.mediawatch.model.reference.MediaReference
import dev.toastbits.lifelog.extension.mediawatch.util.MediaEntityType

fun MediaEntityType.createReference(mediaId: String, extensionId: ExtensionId, referenceTypeId: ExtensionId): MediaReference =
    when (this) {
        MediaEntityType.MOVIE_OR_SHOW -> MovieOrShowMediaReference(mediaId, extensionId, referenceTypeId)
        MediaEntityType.BOOK -> BookMediaReference(mediaId, extensionId, referenceTypeId)
        MediaEntityType.GAME -> GameMediaReference(mediaId, extensionId, referenceTypeId)
        MediaEntityType.SONG -> SongMediaReference(mediaId, extensionId, referenceTypeId)
    }

fun MediaEntityType.createConsumeEvent(mediaReference: MediaReference, extensionId: ExtensionId?): MediaConsumeEvent =
    when (this) {
        MediaEntityType.MOVIE_OR_SHOW -> MovieOrShowMediaConsumeEvent(extensionId, mediaReference)
        MediaEntityType.BOOK -> BookMediaConsumeEvent(extensionId, mediaReference)
        MediaEntityType.GAME -> GameMediaConsumeEvent(extensionId, mediaReference)
        MediaEntityType.SONG -> SongMediaConsumeEvent(extensionId, mediaReference)
    }
