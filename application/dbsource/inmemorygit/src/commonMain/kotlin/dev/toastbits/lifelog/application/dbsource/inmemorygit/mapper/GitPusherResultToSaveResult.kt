package dev.toastbits.lifelog.application.dbsource.inmemorygit.mapper

import dev.toastbits.kogit.memory.handler.GitPusher
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.SaveResult
import dev.toastbits.lifelog.application.dbsource.domain.model.Alert
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.Res
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.`git_pusher_response_message_unknown_$text`
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.`git_pusher_response_warning_repository_moved_$to`
import dev.toastbits.lifelog.core.specification.database.LogDatabase
import org.jetbrains.compose.resources.getString

internal suspend fun GitPusher.Result.toSaveResult(originalDatabase: LogDatabase): SaveResult {
    val alerts: List<Alert> =
        messages.map {
            when (it) {
                is GitPusher.ResponseMessage.RepositoryMoved ->
                    Alert(
                        getString(Res.string.`git_pusher_response_warning_repository_moved_$to`)
                            .replace("\$to", it.movedTo),
                        Alert.Severity.WARNING
                    )
                is GitPusher.ResponseMessage.Unknown ->
                    Alert(
                        getString(Res.string.`git_pusher_response_message_unknown_$text`)
                            .replace("\$text", it.text),
                        Alert.Severity.WARNING
                    )
                is GitPusher.ResponseMessage.Error ->
                    Alert(
                        it.text,
                        Alert.Severity.ERROR
                    )
            }
        }

    return if (isSuccess) {
        SaveResult.Success(originalDatabase.copy(gitCommitHash = commitHash), alerts)
    }
    else {
        SaveResult.Failure(alerts)
    }
}
