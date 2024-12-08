package dev.toastbits.lifelog.application.dbsource.inmemorygit.mapper

import dev.toastbits.kogit.memory.handler.stage.GitHandlerStage
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.LoadProgress
import dev.toastbits.lifelog.application.dbsource.domain.accessor.create
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.Res
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_clone_pulling
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_clone_retrieving_ref
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_pack_file_generating
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_pack_file_parse_checksum
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_pack_file_parse_parsing_objects
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_pack_file_parse_preparing_pack_file
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_pack_file_parse_reading_header
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_push
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_rendering_commit_tree
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_serialising_file_structure
import dev.toastbits.lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_writing_objects_to_cache
import org.jetbrains.compose.resources.StringResource

internal fun GitHandlerStage.toLoadProgress(part: Long?, total: Long?): LoadProgress {
    val type: LoadProgress.Type =
        when (this) {
            GitHandlerStage.Clone.PULL -> LoadProgress.Type.Network
            GitHandlerStage.ResolveRef -> LoadProgress.Type.Network
            GitHandlerStage.PackFileParse.PREPARE_PACK -> LoadProgress.Type.Generic
            GitHandlerStage.PackFileParse.READ_HEADER -> LoadProgress.Type.Generic
            GitHandlerStage.PackFileParse.PARSE_OBJECTS -> LoadProgress.Type.Generic
            GitHandlerStage.PackFileParse.CHECKSUM -> LoadProgress.Type.Generic
            is GitHandlerStage.RenderCommitTree ->
                currentObject?.let { LoadProgress.Type.Object(it) }
                    ?: LoadProgress.Type.Generic
            GitHandlerStage.SerialisingFileStructure -> LoadProgress.Type.Generic
            GitHandlerStage.WritingObjectsToCache -> LoadProgress.Type.Generic
            GitHandlerStage.PackFileGenerate -> LoadProgress.Type.Generic
            GitHandlerStage.Push -> LoadProgress.Type.Network
        }

    val messageResource: StringResource =
        when (this) {
            GitHandlerStage.Clone.PULL -> Res.string.accessor_progress_clone_pulling
            GitHandlerStage.ResolveRef -> Res.string.accessor_progress_clone_retrieving_ref
            GitHandlerStage.PackFileParse.PREPARE_PACK -> Res.string.accessor_progress_pack_file_parse_preparing_pack_file
            GitHandlerStage.PackFileParse.READ_HEADER -> Res.string.accessor_progress_pack_file_parse_reading_header
            GitHandlerStage.PackFileParse.PARSE_OBJECTS -> Res.string.accessor_progress_pack_file_parse_parsing_objects
            GitHandlerStage.PackFileParse.CHECKSUM -> Res.string.accessor_progress_pack_file_parse_checksum
            is GitHandlerStage.RenderCommitTree -> Res.string.accessor_progress_rendering_commit_tree
            GitHandlerStage.SerialisingFileStructure -> Res.string.accessor_progress_serialising_file_structure
            GitHandlerStage.WritingObjectsToCache -> Res.string.accessor_progress_writing_objects_to_cache
            GitHandlerStage.PackFileGenerate -> Res.string.accessor_progress_pack_file_generating
            GitHandlerStage.Push -> Res.string.accessor_progress_push
        }

    return type.create(part, total, messageResource)
}
