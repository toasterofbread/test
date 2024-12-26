package dev.toastbits.lifelog.core.specification.impl.converter.usercontent

import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.core.specification.converter.alert.SpecificationLogParseAlert
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceParser
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.StrikeThroughDelimiterParser
import org.intellij.markdown.parser.MarkdownParser
import org.intellij.markdown.parser.sequentialparsers.EmphasisLikeParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserManager
import org.intellij.markdown.parser.sequentialparsers.impl.BacktickParser
import org.intellij.markdown.parser.sequentialparsers.impl.EmphStrongDelimiterParser
import org.intellij.markdown.parser.sequentialparsers.impl.ImageParser
import org.intellij.markdown.parser.sequentialparsers.impl.InlineLinkParser
import org.intellij.markdown.parser.sequentialparsers.impl.MathParser
import org.intellij.markdown.parser.sequentialparsers.impl.ReferenceLinkParser

object MarkdownUserContentParser: UserContentParser {
    override fun parseUserContent(
        text: String,
        referenceParser: LogEntityReferenceParser,
        onAlert: (alert: LogParseAlert, line: Int) -> Unit
    ): UserContent {
        val inputText: String = text.trim()
        val parts: MutableList<UserContent.Part> = mutableListOf()

        val nodes: MutableList<ASTNode> =
            mutableListOf(
                MarkdownParser(getFlavour()).buildMarkdownTreeFromString(inputText)
            )

        var currentLine: Int = 0
        var linkLabel: List<UserContent.Part>? = null
        var linkOpeningBracket: Boolean = false
        var linkDestinationText: String = ""

        fun getNodeParts(node: ASTNode): List<UserContent.Part> {
            fun List<ASTNode>.getParts(): List<UserContent.Part> = flatMap { getNodeParts(it) }

            if (linkOpeningBracket && node.type.name != ")") {
                linkOpeningBracket = false
                linkDestinationText += node.getTextInNode(inputText)
                linkOpeningBracket = true
                return emptyList()
            }

            when (node.type.name) {
                "PARAGRAPH",
                "CODE_BLOCK",
                "CODE_LINE",
                "HORIZONTAL_RULE",
                "ORDERED_LIST",
                "UNORDERED_LIST",
                "LIST_ITEM",
                "LIST_BULLET",
                "LIST_NUMBER",
                "SHORT_REFERENCE_LINK",
                "BLOCK_QUOTE",
                "HTML_TAG",
                "SETEXT_2",
                "SETEXT_CONTENT",
                "BACKTICK",
                "ATX_CONTENT",
                "ATX_HEADER" -> return node.children.getParts()
                "TEXT", "WHITE_SPACE", "CODE_FENCE_CONTENT" -> {
                    val nodeText: String = node.getTextInNode(inputText).toString()
                    return listOf(UserContent.Part.Single(nodeText))
                }
                "EOL" -> {
                    currentLine++
                    return listOf(UserContent.Part.Single("\n"))
                }
                "BR" -> {
                    currentLine += 2
                    return listOf(UserContent.Part.Single("\n\n"))
                }
                "EMPH", "STRONG", "CODE_SPAN" -> {
                    var children: List<ASTNode> = node.children

                    val mod: UserContent.Mod =
                        when (node.type.name) {
                            "EMPH" -> UserContent.Mod.Italic
                            "STRONG" -> UserContent.Mod.Bold
                            "CODE_SPAN" -> {
                                children = children.removeSides("BACKTICK", "BACKTICK")
                                UserContent.Mod.Code
                            }
                            else -> throw IllegalStateException(node.type.name)
                        }
                    return listOf(UserContent.Part.Composite(children.getParts(), setOf(mod)))
                }
                "STRIKETHROUGH" -> {
                    val children: List<ASTNode> = node.children.removeSides("~", "~").removeSides("~", "~")
                    return listOf(UserContent.Part.Composite(children.flatMap { getNodeParts(it) }, setOf(UserContent.Mod.Strikethrough)))
                }
                "CODE_FENCE" -> {
                    val children: List<ASTNode> = node.children.removeSides("CODE_FENCE_START", "CODE_FENCE_END").removeSides("EOL", "EOL")
                    return listOf(UserContent.Part.Composite(children.getParts(), setOf(UserContent.Mod.CodeBlock)))
                }
                "IMAGE" -> {
                    val children: List<ASTNode>? = node.children.getOrNull(1)?.children

                    var linkNode: ASTNode? = children?.firstOrNull { it.type.name == "LINK_LABEL" || it.type.name == "LINK_DESTINATION" }
                    if (linkNode?.type?.name == "LINK_LABEL") {
                        linkNode = linkNode.children.getOrNull(1)
                    }

                    return listOfNotNull(UserContent.Part.Image(linkNode?.getTextInNode(inputText).toString()))
                }
                "ATX_1", "ATX_2", "ATX_3", "ATX_4", "ATX_5", "ATX_6" -> {
                    val level: Int = node.type.name.last().digitToInt()

                    return listOf(
                        UserContent.Part.Composite(
                            node.children
                                .drop(1)
                                .getParts()
                                .dropWhile {
                                    it is UserContent.Part.Single && it.text.isBlank()
                                },
                            setOf(UserContent.Mod.Heading(level))
                        )
                    )
                }
                "GFM_AUTOLINK" -> {
                    val link: String = node.getTextInNode(inputText).toString()
                    return listOf(UserContent.Part.Single(link))
                }
                "LINK_LABEL" -> {
                    linkLabel = node.children.subList(1, node.children.size - 1).getParts()
                    linkOpeningBracket = false
                    linkDestinationText = ""
                    return emptyList()
                }
                "INLINE_LINK" -> {
                    var linkTextParts: List<UserContent.Part>? = null
                    var linkReference: LogEntityReference? = null

                    for (linkChild in node.children) {
                        when (linkChild.type.name) {
                             "LINK_TEXT" -> {
                                val linkTextNodes: List<ASTNode> = linkChild.children.drop(1).dropLast(1)
                                linkTextParts = linkTextNodes.flatMap { getNodeParts(it) }
                            }
                            "LINK_DESTINATION" -> {
                                var linkText: String = linkChild.getTextInNode(inputText).toString()
                                if (linkText.startsWith('<') && linkText.endsWith('>')) {
                                    linkText = linkText.substring(1, linkText.length - 1)
                                }
                                linkReference = referenceParser.parseReference(linkText, onAlert = { onAlert(it, currentLine) })
                            }
                            "(", ")" -> {}
                            else -> onAlert(node.toUnhandledAlert("LINK", inputText), currentLine)
                        }
                    }

                    val referenceMod: UserContent.Mod? = linkReference?.let { UserContent.Mod.Reference(it) }
                    return listOf(UserContent.Part.Composite(linkTextParts.orEmpty(), setOfNotNull(referenceMod)))
                }
                else -> {
                    if (linkLabel != null) {
                        if (linkOpeningBracket) {
                            if (node.type.name == ")") {
                                val linkDestination: LogEntityReference? =
                                    referenceParser.parseReference(
                                        linkDestinationText,
                                        onAlert = { onAlert(it, currentLine) }
                                    )

                                val referenceMod: UserContent.Mod? = linkDestination?.let { UserContent.Mod.Reference(it) }
                                val ret: List<UserContent.Part> = listOf(UserContent.Part.Composite(linkLabel!!, setOfNotNull(referenceMod)))

                                linkLabel = null
                                linkOpeningBracket = false
                                linkDestinationText = ""

                                return ret
                            }
                        }
                        else if (node.type.name == "(") {
                            linkOpeningBracket = true
                            return emptyList()
                        }
                    }

                    if (node.type.name.length == 1) {
                        val nodeText: String = node.getTextInNode(inputText).toString()
                        return listOf(UserContent.Part.Single(nodeText))
                    }

                    onAlert(node.toUnhandledAlert("TOP", inputText), currentLine)
                    return emptyList()
                }
            }
        }

        while (nodes.isNotEmpty()) {
            val parent: ASTNode = nodes.removeLast()
            for (child in parent.children) {
                parts.addAll(getNodeParts(child))
            }
        }

        return UserContent(parts).normalised()
    }

    private fun ASTNode.toUnhandledAlert(scope: String, markdownText: String): LogParseAlert =
        SpecificationLogParseAlert.UnhandledMarkdownNodeType(
            type.name,
            startOffset,
            endOffset,
            scope,
            getTextInNode(markdownText).toString()
        )

    private fun List<ASTNode>.removeSides(startType: String, endType: String): List<ASTNode> {
        if (firstOrNull()?.type?.name != startType) {
            return this
        }

        return drop(1).run {
            if (lastOrNull()?.type?.name == endType) dropLast(1) else this
        }
    }

    private fun getFlavour(): MarkdownFlavourDescriptor =
        object : GFMFlavourDescriptor() {
            override val sequentialParserManager = object : SequentialParserManager() {
                override fun getParserSequence(): List<SequentialParser> =
                    listOf(
                        // AutolinkParser(listOf(MarkdownTokenTypes.AUTOLINK, GFMTokenTypes.GFM_AUTOLINK)),
                        BacktickParser(),
                        MathParser(),
                        ImageParser(),
                        InlineLinkParser(),
                        ReferenceLinkParser(),
                        EmphasisLikeParser(EmphStrongDelimiterParser(), StrikeThroughDelimiterParser())
                    )
            }
        }
}
