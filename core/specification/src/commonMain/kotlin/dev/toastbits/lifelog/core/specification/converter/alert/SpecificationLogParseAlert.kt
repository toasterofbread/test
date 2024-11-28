package dev.toastbits.lifelog.core.specification.converter.alert

import dev.toastbits.lifelog.core.specification.extension.ExtensionId
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference

sealed class SpecificationLogParseAlert: LogParseAlert {
    override val alertExtensionId: String? = null

    data class NoMatchingDateFormat(val dateText: String): SpecificationLogParseAlert(), LogParseAlert.Error
    data object MissingDateError: SpecificationLogParseAlert(), LogParseAlert.Error
    data object UnterminatedEventMetadata: SpecificationLogParseAlert(), LogParseAlert.Error
    data object UnterminatedBlockComment: SpecificationLogParseAlert(), LogParseAlert.Error
    data object EventContentNotTerminated: SpecificationLogParseAlert(), LogParseAlert.Warning
    data class UnhandledMarkdownNodeType(val typeName: String, val startIndex: Int, val endIndex: Int, val scope: String, val text: String): SpecificationLogParseAlert(), LogParseAlert.Warning
    data class UnmatchedEventFormat(val text: String, val availablePrefixes: List<String>): SpecificationLogParseAlert(), LogParseAlert.Warning

    data class InvalidReferenceFormat(val referenceText: String): SpecificationLogParseAlert(), LogParseAlert.Warning
    data class UnknownReferenceType(val referencePath: List<String>, val firstUnknownSegment: Int): SpecificationLogParseAlert(), LogParseAlert.Warning
    data class InvalidReferenceSize(val referencePath: List<String>, val expectedSize: Int): SpecificationLogParseAlert(), LogParseAlert.Error

    data class RedefinedMetadataValue(val key: LogEntityReference): SpecificationLogParseAlert(), LogParseAlert.Warning

    data class InvalidEpisodesSpecifier(val text: String): SpecificationLogParseAlert(), LogParseAlert.Warning

    data class UnregisteredExtension(val extensionId: ExtensionId): SpecificationLogParseAlert(), LogParseAlert.Warning
    data class UnregisteredReferenceType(val referenceTypeId: ExtensionId?, val extensionId: ExtensionId?): SpecificationLogParseAlert(), LogParseAlert.Warning

    data object LogEventOutsideDay: SpecificationLogParseAlert(), LogParseAlert.Warning

    data class UnknownImageFormat(val format: String): SpecificationLogParseAlert(), LogParseAlert.Error
    data class UnknownDataEncoding(val encoding: String): SpecificationLogParseAlert(), LogParseAlert.Error
}
