package dev.toastbits.lifelog.extension.mediawatch.testutil.parser

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.toastbits.lifelog.core.specification.converter.LogFileConverter
import dev.toastbits.lifelog.core.specification.impl.converter.LogFileConverterImpl
import dev.toastbits.lifelog.core.specification.impl.converter.usercontent.MarkdownUserContentParser
import dev.toastbits.lifelog.core.specification.impl.extension.ExtensionRegistryImpl
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceGenerator
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceParser
import dev.toastbits.lifelog.extension.mediawatch.MediaWatchExtension
import dev.toastbits.lifelog.extension.mediawatch.impl.model.reference.BookMediaReference
import kotlinx.datetime.LocalDate
import kotlin.test.BeforeTest

open class ParserTest {
    lateinit var converter: LogFileConverter
        private set
    lateinit var markdownParser: MarkdownUserContentParser
        private set
    lateinit var referenceParser: LogEntityReferenceParser
        private set
    lateinit var referenceGenerator: LogEntityReferenceGenerator
        private set

    val mediaWatchExtension: MediaWatchExtension = MediaWatchExtension()

    fun createTestReference(id: String): LogEntityReference =
        BookMediaReference(id, mediaWatchExtension.id, mediaWatchExtension.strings.mediaReferenceTypeId)

    @BeforeTest
    fun setUp() {
        referenceParser = mock {
            every { parseReference(any(), any()) } calls { createTestReference(it.arg<String>(0)) }
        }
        referenceGenerator = mock {

        }
        markdownParser = MarkdownUserContentParser()

        converter = LogFileConverterImpl(referenceParser, { referenceGenerator }, extensionRegistry = ExtensionRegistryImpl(listOf(mediaWatchExtension)))
    }

    val templateDate: LocalDate = LocalDate.parse("2024-07-02")

    fun String.inTemplate(): String =
        """
----- 02 July 2024
Watched 転生王女と天才令嬢の魔法革命 (first watch, eps 1-5) {
    $this
}
        """

    fun String.parse(): UserContent =
        markdownParser.parseUserContent(this, referenceParser) { alert, _ -> assertThat(alert).isNull() }

    fun parseAndTest(text: String, renderedText: String): UserContent {
        val parsed: UserContent = text.parse()
        assertThat(parsed.asText()).isEqualTo(renderedText)
        assertThat(parsed).isEqualTo(parsed.normalised())
        return parsed.normalised()
    }
}
