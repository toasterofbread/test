package dev.toastbits.lifelog.extension.mediawatch.impl.model.mapper

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

fun MediaEntityType.createReference(mediaId: String, referenceTypeId: String): MediaReference =
    when (this) {
        MediaEntityType.MOVIE_OR_SHOW -> MovieOrShowMediaReference(mediaId, referenceTypeId)
        MediaEntityType.BOOK -> BookMediaReference(mediaId, referenceTypeId)
        MediaEntityType.GAME -> GameMediaReference(mediaId, referenceTypeId)
        MediaEntityType.SONG -> SongMediaReference(mediaId, referenceTypeId)
    }

fun MediaEntityType.createConsumeEvent(mediaReference: MediaReference): MediaConsumeEvent =
    when (this) {
        MediaEntityType.MOVIE_OR_SHOW -> MovieOrShowMediaConsumeEvent(mediaReference)
        MediaEntityType.BOOK -> BookMediaConsumeEvent(mediaReference)
        MediaEntityType.GAME -> GameMediaConsumeEvent(mediaReference)
        MediaEntityType.SONG -> SongMediaConsumeEvent(mediaReference)
    }
