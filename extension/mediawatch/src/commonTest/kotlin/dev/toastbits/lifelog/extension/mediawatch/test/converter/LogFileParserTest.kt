package dev.toastbits.lifelog.extension.mediawatch.test.converter

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import dev.toastbits.lifelog.core.specification.converter.LogFileConverter
import dev.toastbits.lifelog.core.specification.impl.model.entity.date.LogDateImpl
import dev.toastbits.lifelog.core.specification.impl.model.entity.event.LogCommentEventImpl
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.extension.mediawatch.impl.model.reference.BookMediaReference
import dev.toastbits.lifelog.extension.mediawatch.impl.model.reference.GameMediaReference
import dev.toastbits.lifelog.extension.mediawatch.impl.model.reference.MovieOrShowMediaReference
import dev.toastbits.lifelog.extension.mediawatch.impl.model.reference.SongMediaReference
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.BookMediaConsumeEvent
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.GameMediaConsumeEvent
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.MediaConsumeEvent
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.MovieOrShowMediaConsumeEvent
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.SongMediaConsumeEvent
import dev.toastbits.lifelog.extension.mediawatch.testutil.parser.ParserTest
import dev.toastbits.lifelog.extension.mediawatch.util.MediaEntityType
import kotlinx.datetime.LocalDate.Formats.ISO
import kotlinx.datetime.format
import kotlin.test.Test

class LogFileParserTest: ParserTest() {

    @Test
    fun testMediaConsumeEventIterations() {
        val numberTypes: Map<String, Int> =
            mapOf(
                "first" to 1, "1st" to 1,
                "second" to 2, "2nd" to 2,
                "third" to 3, "3rd" to 3,
                "fourth" to 4,
                "fifth" to 5,
                "sixth" to 6,
                "seventh" to 7,
                "eighth" to 8,
                "ninth" to 9,
                "tenth" to 10,
            ) + (4..1000).associateBy { "${it}th" }

        for ((iterationText, iterationNumber) in numberTypes) {
            for (entityType in MediaEntityType.entries) {
                for (prefix in mediaWatchExtension.strings.getMediaEntityTypeConsumeEventPrefixes(entityType)) {
                    for (suffix in mediaWatchExtension.strings.getMediaEntityTypeIterationSuffixes(entityType)) {
                        val text: String = """
                        ----- ${templateDate.format(ISO)}
                        ${prefix.uppercase()} Test Test Test ($iterationText $suffix)
                        """.trimIndent()

                        val result: LogFileConverter.ParseResult = converter.parseLogFile(text.splitToSequence('\n'))
                        assertThat(result.alerts).isEmpty()

                        val event: LogEvent = result.days[LogDateImpl(templateDate, ambiguous = false)]!!.single()
                        assertThat(event).isInstanceOf<MediaConsumeEvent>()

                        assertThat((event as MediaConsumeEvent).iteration).isEqualTo(iterationNumber)
                    }
                }
            }
        }
    }

    @Test
    fun testComments() {
        val blockCommentContent: String = """
            Block comment

            Multiple lines
            Very cool
            Such comment
        """.trimIndent()

        val text: String = """
// Date comment
----- ${templateDate.format(ISO)} // Inline date comment

// Standalone comment

// Event comment
Watched Test Test Test (first watch, eps 1-5) { // Inline event comment

}

/*$blockCommentContent*/
Watched Test Test Test (first watch, eps 1-5) {

}

/*$blockCommentContent*/

// Standalone comment
        """

        val result: LogFileConverter.ParseResult = converter.parseLogFile(text.splitToSequence('\n'))
        assertThat(result.alerts).isEmpty()

        val (date: LogDate, day: List<LogEvent>) = result.days.entries.single()
        assertThat(date.date).isEqualTo(templateDate)
        assertThat(date.inlineComment).isEqualTo(UserContent.single("Inline date comment"))
        assertThat(date.aboveComment).isEqualTo(UserContent.single("Date comment"))

        assertThat(day).hasSize(5)

        assertThat(day[0]).isEqualTo(
            LogCommentEventImpl(UserContent.single("Standalone comment"))
        )
        assertThat(day[1]).isEqualTo(
            MovieOrShowMediaConsumeEvent(
                MovieOrShowMediaReference("Test Test Test", mediaWatchExtension.id, mediaWatchExtension.strings.mediaReferenceTypeId),
                inlineComment = UserContent.single("Inline event comment"),
                aboveComment = UserContent.single("Event comment"),
                iteration = 1
            )
        )
        assertThat(day[2]).isEqualTo(
            MovieOrShowMediaConsumeEvent(
                MovieOrShowMediaReference("Test Test Test", mediaWatchExtension.id, mediaWatchExtension.strings.mediaReferenceTypeId),
                inlineComment = null,
                aboveComment = UserContent.single(blockCommentContent),
                iteration = 1
            )
        )
        assertThat(day[3]).isEqualTo(
            LogCommentEventImpl(UserContent.single(blockCommentContent))
        )
        assertThat(day[4]).isEqualTo(
            LogCommentEventImpl(UserContent.single("Standalone comment"))
        )
    }

    @Test
    fun test() {
        val testReference: MovieOrShowMediaReference = MovieOrShowMediaReference("Show name", mediaWatchExtension.id, mediaWatchExtension.strings.mediaReferenceTypeId)

        val dayContent: String = "Gay people stay winning [Test!](/media/movie/${testReference.mediaId})"
        val eventReference: String = "転生王女と天才令嬢の魔法革命"

        val text: String = """
----- ${templateDate.format(ISO)}

Watched $eventReference (first watch, eps 1-5) {
    $dayContent
}

Read $eventReference (2nd read, pages 1-87) {
    $dayContent
}

Played $eventReference (third play, 4 hours) {
    $dayContent
}

Listened to $eventReference (12th listen) {
    $dayContent
}
        """

        val expectedEvents: List<LogEvent> =
            listOf(
                MovieOrShowMediaConsumeEvent(
                    MovieOrShowMediaReference(eventReference, mediaWatchExtension.id, mediaWatchExtension.strings.mediaReferenceTypeId),
                    content = dayContent.parse(),
                    iteration = 1
                ),
                BookMediaConsumeEvent(
                    BookMediaReference(eventReference, mediaWatchExtension.id, mediaWatchExtension.strings.mediaReferenceTypeId),
                    content = dayContent.parse(),
                    iteration = 2
                ),
                GameMediaConsumeEvent(
                    GameMediaReference(eventReference, mediaWatchExtension.id, mediaWatchExtension.strings.mediaReferenceTypeId),
                    content = dayContent.parse(),
                    iteration = 3
                ),
                SongMediaConsumeEvent(
                    SongMediaReference(eventReference, mediaWatchExtension.id, mediaWatchExtension.strings.mediaReferenceTypeId),
                    content = dayContent.parse(),
                    iteration = 12
                )
            )

        val result: LogFileConverter.ParseResult = converter.parseLogFile(text.splitToSequence('\n'))
        assertThat(result.alerts).isEmpty()

        assertThat(result.days).hasSize(1)

        val day: List<LogEvent>? = result.days[LogDateImpl(templateDate, ambiguous = false)]
        println(day)
        assertThat(day).isEqualTo(expectedEvents)
    }
}
