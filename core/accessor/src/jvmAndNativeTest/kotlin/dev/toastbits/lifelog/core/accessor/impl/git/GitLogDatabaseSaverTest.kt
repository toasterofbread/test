@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.toastbits.lifelog.core.accessor.impl.git

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dev.mokkery.mock
import dev.toastbits.kogit.system.GitWrapper
import dev.toastbits.lifelog.core.accessor.DatabaseFileStructureProvider
import dev.toastbits.lifelog.core.accessor.RemoteLogDatabaseAccessor
import dev.toastbits.lifelog.core.accessor.impl.DatabaseFileStructureProviderImpl
import dev.toastbits.lifelog.core.accessor.impl.DatabaseFilesGeneratorImpl
import dev.toastbits.lifelog.core.accessor.impl.DatabaseFilesParserImpl
import dev.toastbits.lifelog.core.accessor.model.GitRemoteBranch
import dev.toastbits.lifelog.core.accessor.reference.LogEntityReferenceGeneratorImpl
import dev.toastbits.lifelog.core.specification.converter.LogFileConverterStrings
import dev.toastbits.lifelog.core.specification.database.LogDataFile
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import dev.toastbits.lifelog.core.specification.database.LogDatabaseConfiguration
import dev.toastbits.lifelog.core.specification.database.LogFileSplitStrategy
import dev.toastbits.lifelog.core.specification.extension.ExtensionRegistry
import dev.toastbits.lifelog.core.specification.impl.converter.LogFileConverterImpl
import dev.toastbits.lifelog.core.specification.impl.extension.ExtensionRegistryImpl
import dev.toastbits.lifelog.core.specification.impl.model.entity.date.LogDateImpl
import dev.toastbits.lifelog.core.specification.model.UserContent
import dev.toastbits.lifelog.core.specification.model.entity.event.LogEvent
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityPath
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReference
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceGenerator
import dev.toastbits.lifelog.core.specification.model.reference.LogEntityReferenceParser
import dev.toastbits.lifelog.core.test.FileSystemTest
import dev.toastbits.lifelog.core.test.extension.TestExtension
import dev.toastbits.lifelog.core.test.extension.TestLogEntityReferenceType
import dev.toastbits.lifelog.core.test.extension.TestLogEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import okio.FileSystem
import okio.Path
import okio.SYSTEM
import kotlin.test.BeforeTest
import kotlin.test.Test

class GitLogDatabaseSaverTest: FileSystemTest {
    private lateinit var directory: Path
    private lateinit var repository: GitWrapper
    private lateinit var saver: RemoteLogDatabaseAccessor

    override val fileSystem: FileSystem = FileSystem.SYSTEM

    private val configuration: LogDatabaseConfiguration =
        object : LogDatabaseConfiguration {
            override val extensionRegistry: ExtensionRegistry = ExtensionRegistryImpl(listOf(TestExtension))
            override val splitStrategy: LogFileSplitStrategy = LogFileSplitStrategy.Month
            override val strings: LogFileConverterStrings = LogFileConverterImpl.DEFAULT_FORMATS
        }

    private val fileStructureProvider: DatabaseFileStructureProvider =
        DatabaseFileStructureProviderImpl(configuration)

    private val referenceParser: LogEntityReferenceParser = fileStructureProvider
    private val logFileConverter: LogFileConverterImpl =
        LogFileConverterImpl(
            referenceParser,
            { LogEntityReferenceGeneratorImpl(fileStructureProvider) },
            configuration.strings,
            extensionRegistry = configuration.extensionRegistry
        )

    @BeforeTest
    fun setUp() {
        directory = getEmptyTempDir("lifelog-repo")

        val ioDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()
        repository = GitWrapper.createSystemDefault(directory, ioDispatcher)
        repository.setCredentials(SecretKeys.credentials)

        val remote: GitRemoteBranch = GitRemoteBranch("testRemote", "https://github.com/toasterofbread/test", "lifelog-test")

        saver = GitLogDatabaseAccessor(
            repository,
            remote,
            DatabaseFilesParserImpl(logFileConverter, configuration, fileStructureProvider, ioDispatcher),
            DatabaseFilesGeneratorImpl(logFileConverter, fileStructureProvider, configuration.splitStrategy)
        )
    }

    @Test
    fun testGitLogDatabaseSaver() = runTest {
        val content: UserContent = UserContent.single("Hello World!", setOf(UserContent.Mod.Italic, UserContent.Mod.Bold))
        val renderedContent: String = "***Hello World!***"

        val date: LocalDate = LocalDate.parse("2024-08-15")
        val reference: LogEntityReference.InMetadata =
            LogEntityReference.InMetadataData(
                LogEntityPath.of("test 2"),
                TestExtension.id,
                TestLogEntityReferenceType.id
            )
        val event: LogEvent = TestLogEvent(reference, content = content)

        val database: LogDatabase =
            LogDatabase(
                mock(),
                days = mapOf(
                    LogDateImpl(date, false) to listOf(
                        event
                    )
                ),
                data = mapOf(
                    reference to LogDataFile.Lines(listOf("test"))
                ),
                converter = logFileConverter,
                gitCommitRef = null
            )

        saver.saveDatabaseRemotely(database, "Test ${configuration.strings.preferredDateFormat.format(date)}") { assertThat(it).isNull() }

        val logFile: Path = fileStructureProvider.getLogFilePath(date)
        val logFileContent: String =
            fileSystem.read(repository.directory.resolve(logFile)) {
                readUtf8()
            }

        val referenceGenerator: LogEntityReferenceGenerator = LogEntityReferenceGeneratorImpl(fileStructureProvider)
        val referencePath: LogEntityPath = referenceGenerator.generateReferencePath(reference) { assertThat(it).isNull() }

        assertThat(logFileContent).contains(configuration.strings.datePrefix)
        assertThat(logFileContent).contains(configuration.strings.preferredDateFormat.format(date))
        assertThat(logFileContent).contains(renderedContent.prependIndent(configuration.strings.contentIndentation))
        assertThat(logFileContent).contains(referencePath.toString())

        val metadataFile: Path = fileStructureProvider.getEntityReferenceFilePath(reference)
        val metadataFileContent: String =
            fileSystem.read(repository.directory.resolve(metadataFile)) {
                readUtf8()
            }

        val expectedMetadataFileContent: String =
            buildString {
                append("test")
            }
        assertThat(metadataFileContent).isEqualTo(expectedMetadataFileContent)
    }
}
