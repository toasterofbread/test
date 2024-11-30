package dev.toastbits.lifelog.core.accessor.reference

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dev.toastbits.lifelog.core.accessor.DatabaseFileStructureProvider
import dev.toastbits.lifelog.core.accessor.impl.DatabaseFileStructureProviderImpl
import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.converter.alert.LogParseAlert
import dev.toastbits.lifelog.core.specification.converter.alert.SpecificationLogParseAlert
import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import dev.toastbits.lifelog.core.specification.database.LogFileSplitStrategy
import dev.toastbits.lifelog.core.specification.extension.ExtensionRegistry
import dev.toastbits.lifelog.core.specification.impl.converter.LogFileConverterStringsImpl
import dev.toastbits.lifelog.core.specification.impl.extension.ExtensionRegistryImpl
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityPath
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceParser
import dev.toastbits.lifelog.core.test.extension.TestExtension
import dev.toastbits.lifelog.core.test.extension.TestLogEntityReferenceType
import kotlin.test.BeforeTest
import kotlin.test.Test

class LogEntityReferenceParserTest {
    private val strings: LogFileConverterStrings = LogFileConverterStringsImpl()
    private lateinit var fileStructureProvider: DatabaseFileStructureProvider
    private lateinit var referenceParser: LogEntityReferenceParser

    @BeforeTest
    fun setUp() {
        fileStructureProvider = DatabaseFileStructureProviderImpl(
            object : LogDatabaseConfiguration {
                override val extensionRegistry: ExtensionRegistry =
                    ExtensionRegistryImpl(listOf(TestExtension))
                override val splitStrategy: LogFileSplitStrategy =
                    LogFileSplitStrategy.Month
                override val strings: LogFileConverterStrings =
                    this@LogEntityReferenceParserTest.strings

            }
        )
        referenceParser = fileStructureProvider
    }

    @Test
    fun testValidPath() {
        val testEntityPath: LogEntityPath = LogEntityPath.of("test path 1", "test path 2", "test path 3")
        val path: String = "${strings.metadataDirectoryName}/${strings.extensionContentDirectoryName}/${TestExtension.id}/${TestLogEntityReferenceType.id}/" + testEntityPath.segments.joinToString("/")

        val result: LogEntityReference? = referenceParser.parseReference(path) { assertThat(it).isNull() }
        assertThat(result).isEqualTo(
            LogEntityReference.InMetadataData(testEntityPath, TestExtension.id, TestLogEntityReferenceType.id)
        )
    }

    @Test
    fun testInvalidMetadataPath() {
        testInvalidPath(
            "${strings.metadataDirectoryName}uistbdstacbybu/${strings.extensionContentDirectoryName}/${TestExtension.id}/${TestLogEntityReferenceType.id}",
            0
        )
    }

    @Test
    fun testInvalidMetadataSubPath() {
        testInvalidPath(
            "${strings.metadataDirectoryName}/${strings.extensionContentDirectoryName}uistbdstacbybu/${TestExtension.id}/${TestLogEntityReferenceType.id}",
            1
        )
    }

    @Test
    fun testInvalidExtensionPath() {
        testInvalidPath(
            "${strings.metadataDirectoryName}/${strings.extensionContentDirectoryName}/${TestExtension.id}uistbdstacbybu/${TestLogEntityReferenceType.id}",
            2
        )
    }

    @Test
    fun testInvalidReferenceTypePath() {
        testInvalidPath(
            "${strings.metadataDirectoryName}/${strings.extensionContentDirectoryName}/${TestExtension.id}/${TestLogEntityReferenceType.id}uistbdstacbybu",
            3
        )
    }

    @Test
    fun testInvalidReferencePath() {
        testInvalidPath(
            "${strings.metadataDirectoryName}/${strings.extensionContentDirectoryName}/${TestExtension.id}/${TestLogEntityReferenceType.id}",
            4
        )
    }

    private fun testInvalidPath(path: String, firstInvalidIndex: Int) {
        val alerts: MutableList<LogParseAlert> = mutableListOf()
        val result: LogEntityReference? = referenceParser.parseReference(path) { alerts.add(it) }

        assertThat(result).isNull()
        assertThat(alerts).isEqualTo(
            listOf(
                SpecificationLogParseAlert.UnknownReferenceType(
                    path.split("/"),
                    firstInvalidIndex
                )
            )
        )
    }
}
