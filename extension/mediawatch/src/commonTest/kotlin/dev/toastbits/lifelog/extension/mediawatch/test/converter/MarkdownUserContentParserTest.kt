package dev.toastbits.lifelog.extension.mediawatch.test.converter

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.extension.mediawatch.testutil.parser.ParserTest
import kotlin.test.Test

class MarkdownUserContentParserTest: ParserTest() {
    @Test
    fun testEntityReference() {
        val reference: String = "ref"
        val markdownText: String = "Hello [World!]($reference)".inTemplate()
        val renderedText: String = "Hello World!".inTemplate()

        val parsed: UserContent = parseAndTest(markdownText, renderedText)

        assertThat(parsed.parts).hasSize(3)

        assertThat(parsed.parts[0].mods).isEmpty()
        assertThat(parsed.parts[2].mods).isEmpty()

        assertThat(parsed.parts[1].mods).hasSize(1)
        assertThat(parsed.parts[1].mods.single()).isEqualTo(UserContent.Mod.Reference(createTestReference(reference)))
    }

    @Test
    fun testTextFormatting() {
        val text: String = "Normal *Italic* **Bold** ***Both*** **Bold _Both_** ~~Strikethrough~~ `Code`"
        val renderedText: String = "Normal Italic Bold Both Bold Both Strikethrough Code"

        val expectedParts: List<UserContent.Part> =
            listOf(
                UserContent.Part.Single("Normal "),
                UserContent.Part.Single("Italic", setOf(UserContent.Mod.Italic)),
                UserContent.Part.Single(" "),
                UserContent.Part.Single("Bold", setOf(UserContent.Mod.Bold)),
                UserContent.Part.Single(" "),
                UserContent.Part.Single("Both", setOf(UserContent.Mod.Bold, UserContent.Mod.Italic)),
                UserContent.Part.Single(" "),
                UserContent.Part.Composite(
                    listOf(
                        UserContent.Part.Single("Bold "),
                        UserContent.Part.Single("Both", setOf(UserContent.Mod.Italic))
                    ),
                    setOf(UserContent.Mod.Bold)
                ),
                UserContent.Part.Single(" "),
                UserContent.Part.Single("Strikethrough", setOf(UserContent.Mod.Strikethrough)),
                UserContent.Part.Single(" "),
                UserContent.Part.Single("Code", setOf(UserContent.Mod.Code)),
            )

        val parsed: UserContent = parseAndTest(text, renderedText)
        assertThat(parsed.parts).isEqualTo(expectedParts)
    }

    @Test
    fun testCodeBlock() {
        val text: String = "```\nThis is a code block\n```"
        val renderedText: String = "This is a code block"

        val expectedParts: List<UserContent.Part.Single> =
            listOf(
                UserContent.Part.Single("This is a code block", setOf(UserContent.Mod.CodeBlock))
            )

        val parsed: UserContent = parseAndTest(text, renderedText)
        assertThat(parsed.parts).isEqualTo(expectedParts)
    }
}
