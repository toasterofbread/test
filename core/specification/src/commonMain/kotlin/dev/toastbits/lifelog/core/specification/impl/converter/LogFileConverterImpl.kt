package dev.toastbits.lifelog.core.specification.impl.converter

import dev.toastbits.lifelog.core.specification.converter.LogFileConverter
import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.extension.ExtensionRegistry
import dev.toastbits.lifelog.core.specification.impl.converter.usercontent.MarkdownUserContentGenerator
import dev.toastbits.lifelog.core.specification.impl.converter.usercontent.MarkdownUserContentParser
import dev.toastbits.lifelog.core.specification.impl.converter.usercontent.UserContentGenerator
import dev.toastbits.lifelog.core.specification.impl.converter.usercontent.UserContentParser
import dev.toastbits.lifelog.core.specification.impl.extension.ExtensionRegistryImpl
import dev.toastbits.lifelog.core.specification.model.entity.date.LogDate
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEventType
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceGenerator
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceParser
import kotlinx.datetime.LocalDate
import okio.Path

private typealias TypePath = List<String>

class LogFileConverterImpl(
    override val referenceParser: LogEntityReferenceParser,
    override val referenceGeneratorProvider: (LocalDate) -> LogEntityReferenceGenerator,
    private val formats: LogFileConverterStrings = DEFAULT_FORMATS,
    private val extensionRegistry: ExtensionRegistry = ExtensionRegistryImpl(),
//    eventTypes: List<LogEventType> = DEFAULT_EVENT_TYPES,
//    referenceTypes: List<LogEntityReferenceType> = DEFAULT_REFERENCE_TYPES,
    override val userContentParser: UserContentParser = MarkdownUserContentParser,
    override val userContentGenerator: UserContentGenerator = MarkdownUserContentGenerator()
): LogFileConverter {
    override fun parseLogFile(
        fileLines: Sequence<String>,
        filePath: Path,
        initialDate: LogDate?
    ): LogFileConverter.ParseResult =
        LogFileParser(
            filePath,
            formats,
            extensionRegistry.getAllExtensions().flatMap { it.extraEventTypes },
            userContentParser,
            referenceParser,
            initialDate
        ).parse(fileLines)

    override fun generateLogFile(days: Map<LogDate, List<LogEvent>>): LogFileConverter.GenerateResult =
        LogFileGenerator(
            formats,
            extensionRegistry.getAllExtensions().flatMap { it.extraEventTypes },
            userContentGenerator,
            referenceGeneratorProvider
        ).generate(days)

    companion object {
        val INITIAL_EVENT_TYPES: Map<TypePath, List<LogEventType>> = mapOf(

        )

        val DEFAULT_FORMATS: LogFileConverterStringsImpl =
            LogFileConverterStringsImpl()
    }
}
