package dev.toastbits.lifelog.application.dbsource.inmemorygit.mapper

import dev.toastbits.kogit.memory.handler.stage.GitHandlerStage
import dev.toastbits.lifelog.application.dbsource.domain.accessor.DatabaseAccessor.LoadProgress
import dev.toastbits.lifelog.application.dbsource.domain.accessor.create
import lifelog.application.dbsource.inmemorygit.generated.resources.Res
import lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_clone_pulling
import lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_clone_retrieving_ref
import lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_pack_file_generating
import lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_pack_file_parse_checksum
import lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_pack_file_parse_parsing_objects
import lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_pack_file_parse_preparing_pack_file
import lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_pack_file_parse_reading_header
import lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_push
import lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_rendering_commit_tree
import lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_serialising_file_structure
import lifelog.application.dbsource.inmemorygit.generated.resources.accessor_progress_writing_objects_to_cache
import org.jetbrains.compose.resources.StringResource

internal fun GitHandlerStage.toLoadProgress(part: Long?, total: Long?): LoadProgress {
    val type: LoadProgress.Type =
        when (this) {
            GitHandlerStage.Clone.PULL -> LoadProgress.Type.NETWORK
            GitHandlerStage.ResolveRef -> LoadProgress.Type.NETWORK
            GitHandlerStage.PackFileParse.PREPARE_PACK -> LoadProgress.Type.GENERIC
            GitHandlerStage.PackFileParse.READ_HEADER -> LoadProgress.Type.GENERIC
            GitHandlerStage.PackFileParse.PARSE_OBJECTS -> LoadProgress.Type.GENERIC
            GitHandlerStage.PackFileParse.CHECKSUM -> LoadProgress.Type.GENERIC
            GitHandlerStage.RenderCommitTree -> LoadProgress.Type.GENERIC
            GitHandlerStage.SerialisingFileStructure -> LoadProgress.Type.GENERIC
            GitHandlerStage.WritingObjectsToCache -> LoadProgress.Type.GENERIC
            GitHandlerStage.PackFileGenerate -> LoadProgress.Type.GENERIC
            GitHandlerStage.Push -> LoadProgress.Type.NETWORK
        }

    val messageResource: StringResource =
        when (this) {
            GitHandlerStage.Clone.PULL -> Res.string.accessor_progress_clone_pulling
            GitHandlerStage.ResolveRef -> Res.string.accessor_progress_clone_retrieving_ref
            GitHandlerStage.PackFileParse.PREPARE_PACK -> Res.string.accessor_progress_pack_file_parse_preparing_pack_file
            GitHandlerStage.PackFileParse.READ_HEADER -> Res.string.accessor_progress_pack_file_parse_reading_header
            GitHandlerStage.PackFileParse.PARSE_OBJECTS -> Res.string.accessor_progress_pack_file_parse_parsing_objects
            GitHandlerStage.PackFileParse.CHECKSUM -> Res.string.accessor_progress_pack_file_parse_checksum
            GitHandlerStage.RenderCommitTree -> Res.string.accessor_progress_rendering_commit_tree
            GitHandlerStage.SerialisingFileStructure -> Res.string.accessor_progress_serialising_file_structure
            GitHandlerStage.WritingObjectsToCache -> Res.string.accessor_progress_writing_objects_to_cache
            GitHandlerStage.PackFileGenerate -> Res.string.accessor_progress_pack_file_generating
            GitHandlerStage.Push -> Res.string.accessor_progress_push
        }

    return type.create(part, total, messageResource)
}
