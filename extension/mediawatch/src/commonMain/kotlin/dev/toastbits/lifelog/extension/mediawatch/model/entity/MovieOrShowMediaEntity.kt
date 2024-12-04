package dev.toastbits.lifelog.extension.mediawatch.model.entity

import dev.toastbits.lifelog.core.specification.model.entity.LogEntity
import dev.toastbits.lifelog.core.specification.model.entity.LogEntityCompanion
import dev.toastbits.lifelog.extension.mediawatch.localisation.MediaStringId
import kotlin.time.Duration

interface MovieOrShowMediaEntity: MediaEntity {
    val runtime: Duration?
    val partCount: Int?

    companion object: LogEntityCompanion<MovieOrShowMediaEntity>(MediaEntity) {
        override fun getAllProperties(): List<LogEntity.Property<*, *>> =
            listOf(
                MediaStringId.Property.MediaEntityMovieOrShow.RUNTIME.property { runtime },
                MediaStringId.Property.MediaEntityMovieOrShow.PART_COUNT.property { partCount }
            )
    }
}
