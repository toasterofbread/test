package dev.toastbits.lifelog.core.specification.impl.converter.usercontent

import dev.toastbits.lifelog.core.specification.converter.alert.LogGenerateAlert
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceGenerator
import dev.toastbits.lifelog.core.specification.model.sorted

class MarkdownUserContentGenerator: UserContentGenerator {
    override fun generateUserContent(
        content: UserContent,
        referenceGenerator: LogEntityReferenceGenerator,
        onAlert: (alert: LogGenerateAlert, line: Int) -> Unit
    ): String = buildString {
        val builder: StringBuilderWrapper = StringBuilderWrapper(this, referenceGenerator, onAlert)
        for (part in content.parts) {
            builder.addPart(part)
        }
    }.trim()

    private class StringBuilderWrapper(
        private val stringBuilder: StringBuilder,
        private val referenceGenerator: LogEntityReferenceGenerator,
        private val onAlert: (alert: LogGenerateAlert, line: Int) -> Unit
    ) {
        var currentLine: Int = 0

        fun append(string: String) {
            stringBuilder.append(string)
            currentLine += string.count { it == '\n' }
        }

        fun addPart(part: UserContent.Part) {
            inModifiers(part.mods) {
                when (part) {
                    is UserContent.Part.Single -> append(part.text)
                    is UserContent.Part.Composite -> {
                        for (subPart in part.parts) {
                            addPart(subPart)
                        }
                    }
                    is UserContent.Part.Image -> append("![](${part.location})")
                }
            }
        }

        private fun inModifiers(
            modifiers: Iterable<UserContent.Mod>,
            block: () -> Unit
        ) {
            val sorted: List<UserContent.Mod> = modifiers.sorted()
            for (modifier in sorted) {
                append(modifier.getStart())
            }

            block()

            for (modifier in sorted.asReversed()) {
                append(modifier.getEnd())
            }
        }

        private fun UserContent.Mod.getStart(): String =
            when (this) {
                UserContent.Mod.Bold -> "**"
                UserContent.Mod.Code -> "`"
                UserContent.Mod.CodeBlock -> "```\n"
                UserContent.Mod.Italic -> "*"
                UserContent.Mod.Strikethrough -> "~~"
                is UserContent.Mod.Heading -> "#".repeat(level) + ' '
                is UserContent.Mod.Reference -> "["
            }

        private fun UserContent.Mod.getEnd(): String =
            when (this) {
                UserContent.Mod.CodeBlock -> "\n```"
                is UserContent.Mod.Reference -> {
                    val referenceLink: String =
                        if (reference is LogEntityReference.URL) reference.url
                        else referenceGenerator.generateReferencePath(reference, onAlert = { onAlert(it, currentLine) }).toString()

                    "]($referenceLink)"
                }
                is UserContent.Mod.Heading -> ""
                else -> getStart()
            }
    }
}
