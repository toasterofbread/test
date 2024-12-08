package dev.toastbits.lifelog.application.dbsource.domain.accessor

import dev.toastbits.composekit.util.roundTo
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.LoadProgress
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.LoadProgress.Absolute
import lifelog.application.dbsource.domain.generated.resources.Res
import lifelog.application.dbsource.domain.generated.resources.`database_accessor_load_progress_generic_$part`
import lifelog.application.dbsource.domain.generated.resources.`database_accessor_load_progress_generic_$part_of_$total`
import lifelog.application.dbsource.domain.generated.resources.`database_accessor_load_progress_generic_$total`
import lifelog.application.dbsource.domain.generated.resources.`database_accessor_load_progress_network_$bytes`
import lifelog.application.dbsource.domain.generated.resources.`database_accessor_load_progress_network_$bytes_of_$total_$percent`
import lifelog.application.dbsource.domain.generated.resources.`database_accessor_load_progress_object_$part_$type_$hash`
import lifelog.application.dbsource.domain.generated.resources.`database_accessor_load_progress_object_$part_of_$total_$type_$hash`
import lifelog.application.dbsource.domain.generated.resources.`database_accessor_load_progress_object_$total_$type_$hash`
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

fun LoadProgress.Type.create(part: Long?, total: Long?, messageResource: StringResource): LoadProgress =
    if (part != null && total != null)
        object : Absolute {
            override val type: LoadProgress.Type = this@create
            override val progressFraction: Float = part.toFloat() / total

            override fun getMessageResource(): StringResource = messageResource

            override suspend fun getProgressMessage(): String = getProgressMessage(part, total)
        }
    else
        object : LoadProgress {
            override val type: LoadProgress.Type = this@create
            override fun getMessageResource(): StringResource = messageResource

            override suspend fun getProgressMessage(): String? =
                part?.let { getProgressMessageWithPart(it) }
                ?: total?.let { getProgressMessageWithTotal(it) }
        }

private suspend fun LoadProgress.Type.getProgressMessageWithPart(part: Long): String =
    when (this) {
        LoadProgress.Type.Generic ->
            getString(Res.string.`database_accessor_load_progress_generic_$part`)
                .replace("\$part", part.toString())
        LoadProgress.Type.Network ->
            getString(Res.string.`database_accessor_load_progress_network_$bytes`)
                .replace("\$bytes", part.toString())

        is LoadProgress.Type.Object ->
            getString(Res.string.`database_accessor_load_progress_object_$part_$type_$hash`)
                .replace("\$part", part.toString())
                .replace("\$type", currentObject.type.toString())
                .replace("\$hash", currentObject.hash)
    }

private suspend fun LoadProgress.Type.getProgressMessageWithTotal(total: Long): String =
    when (this) {
        LoadProgress.Type.Generic,
        LoadProgress.Type.Network ->
            getString(Res.string.`database_accessor_load_progress_generic_$total`)
                .replace("\$total", total.toString())
        is LoadProgress.Type.Object ->
            getString(Res.string.`database_accessor_load_progress_object_$total_$type_$hash`)
                .replace("\$total", total.toString())
                .replace("\$type", currentObject.type.toString())
                .replace("\$hash", currentObject.hash)
    }

private suspend fun LoadProgress.Type.getProgressMessage(part: Long, total: Long): String =
    when (this) {
        LoadProgress.Type.Generic ->
            getString(Res.string.`database_accessor_load_progress_generic_$part_of_$total`)
                .replace("\$part", part.toString())
                .replace("\$total", total.toString())
        LoadProgress.Type.Network ->
            getString(Res.string.`database_accessor_load_progress_network_$bytes_of_$total_$percent`)
                .replace("\$bytes", part.toString())
                .replace("\$total", total.toString())
                .replace(
                    "\$percent",
                    if (total <= 0) "0"
                    else ((part.toFloat() / total) * 100).roundTo(2).toString()
                )
        is LoadProgress.Type.Object ->
            getString(Res.string.`database_accessor_load_progress_object_$part_of_$total_$type_$hash`)
                .replace("\$part", part.toString())
                .replace("\$total", total.toString())
                .replace("\$type", currentObject.type.toString())
                .replace("\$hash", currentObject.hash)
    }
