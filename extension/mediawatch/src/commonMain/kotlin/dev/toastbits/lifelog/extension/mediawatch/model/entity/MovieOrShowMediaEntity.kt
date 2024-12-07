package dev.toastbits.lifelog.extension.mediawatch.model.entity

import dev.toastbits.lifelog.core.specification.model.entity.LogEntityCompanion
import dev.toastbits.lifelog.core.specification.model.entity.property.LogEntityProperty
import dev.toastbits.lifelog.extension.mediawatch.localisation.MediaStringId
import kotlin.time.Duration

interface MovieOrShowMediaEntity: MediaEntity {
    val runtime: Duration?
    val partCount: Int?

    fun copy(
        runtime: Duration?,
        partCount: Int?
    ): MovieOrShowMediaEntity

    override fun getCompanion(): LogEntityCompanion<*> = Companion

    companion object: LogEntityCompanion<MovieOrShowMediaEntity>(MediaEntity) {
        override fun getProperties(): List<LogEntityProperty<MovieOrShowMediaEntity, *>> =
            listOf(
                MediaStringId.Property.MediaEntityMovieOrShow.RUNTIME.durationProperty({ runtime }, { copy(it, partCount) }),
                MediaStringId.Property.MediaEntityMovieOrShow.PART_COUNT.intProperty({ partCount }, { copy(runtime, it) })
            )
    }
}
