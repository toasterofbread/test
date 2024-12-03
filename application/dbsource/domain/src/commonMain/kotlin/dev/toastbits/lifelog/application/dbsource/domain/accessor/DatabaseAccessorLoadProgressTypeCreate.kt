package dev.toastbits.lifelog.application.dbsource.domain.accessor

import androidx.compose.runtime.Composable
import dev.toastbits.composekit.util.roundTo
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.LoadProgress
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.LoadProgress.Absolute
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.LoadProgress.Type.GENERIC
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.LoadProgress.Type.NETWORK
import lifelog.application.dbsource.domain.generated.resources.Res
import lifelog.application.dbsource.domain.generated.resources.`database_accessor_load_progress_generic_$part`
import lifelog.application.dbsource.domain.generated.resources.`database_accessor_load_progress_generic_$part_of_$total`
import lifelog.application.dbsource.domain.generated.resources.`database_accessor_load_progress_generic_$total`
import lifelog.application.dbsource.domain.generated.resources.`database_accessor_load_progress_network_$bytes`
import lifelog.application.dbsource.domain.generated.resources.`database_accessor_load_progress_network_$bytes_of_$total_$percent`
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

fun LoadProgress.Type.create(part: Long?, total: Long?, messageResource: StringResource): LoadProgress =
    if (part != null && total != null)
        object : Absolute {
            override val progressFraction: Float = part.toFloat() / total

            override fun getMessageResource(): StringResource = messageResource

            @Composable
            override fun getProgressMessage(): String = getProgressMessage(part, total)
        }
    else
        object : LoadProgress {
            override fun getMessageResource(): StringResource = messageResource

            @Composable
            override fun getProgressMessage(): String? =
                part?.let { getProgressMessageWithPart(it) }
                ?: total?.let { getProgressMessageWithTotal(it) }
        }

@Composable
private fun LoadProgress.Type.getProgressMessageWithPart(part: Long): String =
    when (this) {
        GENERIC ->
            stringResource(Res.string.`database_accessor_load_progress_generic_$part`)
                .replace("\$part", part.toString())
        NETWORK ->
            stringResource(Res.string.`database_accessor_load_progress_network_$bytes`)
                .replace("\$bytes", part.toString())
    }

@Composable
private fun LoadProgress.Type.getProgressMessageWithTotal(total: Long): String =
    stringResource(Res.string.`database_accessor_load_progress_generic_$total`)
        .replace("\$total", total.toString())

@Composable
private fun LoadProgress.Type.getProgressMessage(part: Long, total: Long): String =
    when (this) {
        GENERIC ->
            stringResource(Res.string.`database_accessor_load_progress_generic_$part_of_$total`)
                .replace("\$part", part.toString())
                .replace("\$total", total.toString())
        NETWORK ->
            stringResource(Res.string.`database_accessor_load_progress_network_$bytes_of_$total_$percent`)
                .replace("\$bytes", part.toString())
                .replace("\$total", total.toString())
                .replace(
                    "\$percent",
                    if (total <= 0) "0"
                    else ((part.toFloat() / total) * 100).roundTo(2).toString()
                )
    }
