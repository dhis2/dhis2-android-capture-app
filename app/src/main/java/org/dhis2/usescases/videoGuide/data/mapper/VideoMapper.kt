package org.dhis2.usescases.videoGuide.data.mapper

import org.dhis2.usescases.videoGuide.data.dto.VideoIncludedDto
import org.dhis2.usescases.videoGuide.data.dto.VideoMediaDto
import org.dhis2.usescases.videoGuide.domain.model.VideoItem
import javax.inject.Inject

class VideoMapper @Inject constructor() {

    /**
     * 動画ファイルのマップを作成（ID → URL）
     */
    fun createFilesMap(included: List<VideoIncludedDto>?): Map<String, String> {
        val files = included ?: emptyList()
        return files
            .filter { it.type == "file--file" }
            .mapNotNull { file ->
                // attributesからuriを取得
                val uri = (file.attributes?.get("uri") as? Map<*, *>)?.get("url") as? String
                if (uri != null) {
                    file.id to uri
                } else {
                    null
                }
            }
            .associate { it }
    }

    /**
     * 画像ファイルのマップを作成（サムネイル用、ID → URL）
     */
    fun createImageFilesMap(included: List<VideoIncludedDto>?): Map<String, String> {
        val files = included ?: emptyList()
        return files
            .filter { it.type == "file--file" }
            .mapNotNull { file ->
                val uri = (file.attributes?.get("uri") as? Map<*, *>)?.get("url") as? String
                if (uri != null) {
                    file.id to uri
                } else {
                    null
                }
            }
            .associate { it }
    }

    /**
     * タクソノミー用語のマップを作成（ID → 名前）
     */
    fun createTaxonomyMap(included: List<VideoIncludedDto>?): Map<String, String> {
        val taxonomies = included ?: emptyList()
        return taxonomies
            .filter { it.type.startsWith("taxonomy_term--") }
            .mapNotNull { term ->
                val name = term.attributes?.get("name") as? String
                if (name != null) {
                    term.id to name
                } else {
                    null
                }
            }
            .associate { it }
    }

    /**
     * メディア画像のマップを作成（メディアID → ファイルID）
     * サムネイル画像のメディアエンティティからファイルIDを取得するために使用
     */
    fun createMediaImageMap(included: List<VideoIncludedDto>?): Map<String, String> {
        val mediaImages = included ?: emptyList()
        return mediaImages
            .filter { it.type == "media--image" }
            .mapNotNull { media ->
                val relationships = media.relationships
                val fieldMediaImage = relationships?.get("field_media_image") as? Map<*, *>
                val data = fieldMediaImage?.get("data") as? Map<*, *>
                val fileId = data?.get("id") as? String
                if (fileId != null) {
                    media.id to fileId
                } else {
                    null
                }
            }
            .associate { it }
    }

    /**
     * DTOをDomain Modelに変換
     */
    fun mapToDomain(
        media: VideoMediaDto,
        filesMap: Map<String, String>,
        baseUrl: String,
        imageFilesMap: Map<String, String> = emptyMap(),
        taxonomyMap: Map<String, String> = emptyMap(),
        mediaImageMap: Map<String, String> = emptyMap(),
    ): VideoItem? {
        // relationshipsがnullの場合はスキップ
        val fileId = media.relationships?.fieldMediaVideoFile?.data?.id ?: return null

        val relativePath = filesMap[fileId] ?: return null

        val videoUrl = if (relativePath.startsWith("http")) {
            relativePath
        } else {
            "$baseUrl$relativePath"
        }

        // サムネイル画像URLを取得
        // 優先順位: field_video_thumbnail (media--image) > thumbnail (file--file)
        val thumbnailUrl = media.relationships?.fieldVideoThumbnail?.data?.id?.let { mediaImageId ->
            // メディア画像IDからファイルIDを取得
            val imageFileId = mediaImageMap[mediaImageId]
            // ファイルIDからURLを取得
            imageFileId?.let { fileId ->
                val imagePath = imageFilesMap[fileId]
                imagePath?.let { path ->
                    if (path.startsWith("http")) {
                        path
                    } else {
                        "$baseUrl$path"
                    }
                }
            }
        } ?: media.relationships?.thumbnail?.data?.id?.let { thumbnailFileId ->
            // 直接ファイル参照の場合
            val imagePath = imageFilesMap[thumbnailFileId]
            imagePath?.let { path ->
                if (path.startsWith("http")) {
                    path
                } else {
                    "$baseUrl$path"
                }
            }
        }

        // タグを取得（単一）
        val tag = media.relationships?.fieldVideoTag?.data?.id?.let { tagId ->
            taxonomyMap[tagId]
        }

        // カテゴリを取得（単一）
        val category = media.relationships?.fieldVideoCategory?.data?.id?.let { categoryId ->
            taxonomyMap[categoryId]
        }

        // タイトルを取得（field_titleを優先、なければname）
        val title = media.attributes.fieldTitle ?: media.attributes.name

        return VideoItem(
            id = media.id,
            title = title,
            description = media.attributes.fieldDescription ?: "",
            thumbnailUrl = thumbnailUrl,
            videoUrl = videoUrl,
            duration = null,
            tag = tag,
            category = category,
        )
    }
}

