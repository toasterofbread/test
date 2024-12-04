package dev.toastbits.lifelog.extension.gdocs

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dev.toastbits.lifelog.core.accessor.DatabaseFileStructureProvider
import dev.toastbits.lifelog.core.accessor.LogFileSplitStrategy
import dev.toastbits.lifelog.core.accessor.impl.DatabaseFileStructureProviderImpl
import dev.toastbits.lifelog.core.accessor.reference.LogEntityReferenceGeneratorImpl
import dev.toastbits.lifelog.core.specification.converter.LogFileConverter
import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.impl.converter.LogFileConverterImpl
import dev.toastbits.lifelog.core.specification.impl.converter.LogFileConverterStringsImpl
import dev.toastbits.lifelog.core.specification.impl.model.entity.date.LogDateImpl
import dev.toastbits.lifelog.core.specification.impl.model.entity.event.LogCommentEventImpl
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityPath
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceGenerator
import dev.toastbits.lifelog.core.test.extension.TestExtension
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class MediaExtensionParseTest {
    @Test
    fun testMediaReferenceParse() {
        val date: LocalDate = LocalDate.parse("2024-08-10")

        val strings: LogFileConverterStrings = LogFileConverterStringsImpl()
        val splitStrategy: LogFileSplitStrategy = LogFileSplitStrategy.Month
        val fileStructureProvider: DatabaseFileStructureProvider = DatabaseFileStructureProviderImpl(strings, splitStrategy)
        val referenceGenerator: LogEntityReferenceGenerator = LogEntityReferenceGeneratorImpl(fileStructureProvider, date)
        val converter: LogFileConverter = LogFileConverterImpl(fileStructureProvider, { referenceGenerator })
        converter.registerExtension(TestExtension)

        val reference: LogEntityReference.InLogData = LogEntityReference.InLogData(date, LogEntityPath.of("test"))
        val referencePath: LogEntityPath = referenceGenerator.generateReferencePath(reference, relativeToOverride = LogEntityPath.ROOT) { assertThat(it).isNull() }
        val referenceText: String = "Reference to something"

        val parsed: LogFileConverter.ParseResult =
            converter.parseLogFile(
                listOf(
                    "${strings.datePrefix}${strings.preferredDateFormat.format(date)}",
                    "${strings.commentPrefix}[$referenceText]($referencePath)"
                )
            )
        assertThat(parsed.alerts).isEmpty()

        assertThat(parsed.days).hasSize(1)

        val groupDate: LocalDate = splitStrategy.parseDateComponents(splitStrategy.getDateComponents(date))
        assertThat(parsed.days[LogDateImpl(date)]).isEqualTo(
            listOf(
                LogCommentEventImpl(
                    UserContent.single(
                        referenceText,
                        setOf(UserContent.Mod.Reference(reference.copy(logDate = groupDate)))
                    )
                )
            )
        )
    }
}
