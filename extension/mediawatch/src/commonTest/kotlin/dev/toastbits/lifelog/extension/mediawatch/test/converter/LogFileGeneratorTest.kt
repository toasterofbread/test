package dev.toastbits.lifelog.extension.mediawatch.test.converter

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import dev.mokkery.mock
import dev.toastbits.lifelog.core.specification.converter.LogFileConverter
import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.impl.converter.LogFileConverterImpl
import dev.toastbits.lifelog.core.specification.impl.converter.LogFileConverterImpl.Companion.DEFAULT_FORMATS
import dev.toastbits.lifelog.core.specification.impl.converter.usercontent.MarkdownUserContentGenerator
import dev.toastbits.lifelog.core.specification.impl.extension.ExtensionRegistryImpl
import dev.toastbits.lifelog.core.specification.impl.model.entity.date.LogDateImpl
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityPath
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceGenerator
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtension
import dev.toastbits.lifelog.extension.mediawatch.impl.model.reference.MovieOrShowMediaReference
import dev.toastbits.lifelog.extension.mediawatch.model.entity.event.MovieOrShowMediaConsumeEvent
import dev.toastbits.lifelog.extension.mediawatch.model.reference.MediaReference
import kotlinx.datetime.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test

class LogFileGeneratorTest {
    private lateinit var converter: LogFileConverter
    private lateinit var markdownGenerator: MarkdownUserContentGenerator
    private lateinit var referenceGenerator: LogEntityReferenceGenerator

    private val formats: LogFileConverterStrings = DEFAULT_FORMATS
    private val mediaWatchExtension: MediaWatchExtension = MediaWatchExtension()
    private val mockResultReferencePath: LogEntityPath = LogEntityPath.of("TEST1", "TEST2")

    @BeforeTest
    fun setUp() {
        referenceGenerator = mock {
//            every { generateReferencePath(any(), any(), any()) } returns mockResultReferencePath
        }
        converter = LogFileConverterImpl(
            mock {},
            { referenceGenerator },
            formats = formats,
            extensionRegistry = ExtensionRegistryImpl(listOf(mediaWatchExtension))
        )

        markdownGenerator = MarkdownUserContentGenerator()
    }

    @Test
    fun temp() {
        val date: LocalDate = LocalDate.parse("2024-08-04")
        val mediaReference: MediaReference = MovieOrShowMediaReference("test 2", mediaWatchExtension.id, mediaWatchExtension.strings.mediaReferenceTypeId)
        val days: Map<LogDate, List<LogEvent>> =
            mapOf(
                LogDateImpl(date, ambiguous = false) to listOf(
                    MovieOrShowMediaConsumeEvent(mediaReference, iteration = 1)
                )
            )

        val result: LogFileConverter.GenerateResult = converter.generateLogFile(days)
        assertThat(result.alerts).isEmpty()

        val expectedLines: List<String> =
            listOf(
                formats.datePrefix + formats.preferredDateFormat.format(date),
                "",
                mediaWatchExtension.strings.getMediaEntityTypeConsumeEventPrefixes(mediaReference.mediaType).first() + "[${mediaReference.mediaId}](<$mockResultReferencePath>) (first watch)",
                ""
            )

        assertThat(result.lines).isEqualTo(expectedLines)
    }
}
