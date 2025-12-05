# VideoGuide メタ情報追加実装ガイド

## 概要

このドキュメントでは、Drupal JSON:APIから動画のメタ情報（title、description、thumbnail、tag、category）を取得するための実装方法を説明します。

## 取得するメタ情報

以下のメタ情報をDrupalから取得します：

- **title**: 動画のタイトル（`field_title`フィールド、なければ`name`フィールドを使用）
- **description**: 動画の説明（`field_description`フィールド）
- **thumbnail**: サムネイル画像URL（`thumbnail`または`field_video_thumbnail`から取得）
- **tag**: タグ（単一のタクソノミー用語、`field_video_tag`）
- **category**: カテゴリ（タクソノミー用語、`field_video_category`）

## 実装手順

### 1. Domain Model（VideoItem）の更新

`VideoItem`に`tag`と`category`フィールドを追加します。

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/domain/model/VideoItem.kt`

```kotlin
package org.dhis2.usescases.videoGuide.domain.model

data class VideoItem(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String?,
    val videoUrl: String,
    val duration: String? = null,
    val tag: String? = null,        // タグ（単一）
    val category: String? = null,   // カテゴリ
)
```

### 2. DTOクラスの更新

#### 2.1 VideoMediaDtoの更新

`VideoMediaRelationshipsDto`にthumbnail、tag、categoryのrelationshipsを追加します。

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/data/dto/VideoMediaDto.kt`

```kotlin
package org.dhis2.usescases.videoGuide.data.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoMediaDto(
    val id: String,
    val type: String,
    val attributes: VideoMediaAttributesDto,
    val relationships: VideoMediaRelationshipsDto? = null,
)

@JsonClass(generateAdapter = true)
data class VideoMediaAttributesDto(
    val name: String,
    @Json(name = "field_description")
    val fieldDescription: String? = null,
    @Json(name = "field_title")
    val fieldTitle: String? = null,
)

@JsonClass(generateAdapter = true)
data class VideoMediaRelationshipsDto(
    @Json(name = "field_media_video_file")
    val fieldMediaVideoFile: VideoFileRelationshipDto? = null,
    // サムネイル（直接ファイル参照）
    @Json(name = "thumbnail")
    val thumbnail: VideoRelationshipDto? = null,
    // サムネイル（メディア参照）
    @Json(name = "field_video_thumbnail")
    val fieldVideoThumbnail: VideoRelationshipDto? = null,
    // カテゴリ（タクソノミー用語への参照、単一）
    @Json(name = "field_video_category")
    val fieldVideoCategory: VideoRelationshipDto? = null,
    // タグ（タクソノミー用語への参照、単一）
    @Json(name = "field_video_tag")
    val fieldVideoTag: VideoRelationshipDto? = null,
)

@JsonClass(generateAdapter = true)
data class VideoFileRelationshipDto(
    val data: VideoFileReferenceDto,
)

@JsonClass(generateAdapter = true)
data class VideoFileReferenceDto(
    val id: String,
    val type: String,
)

// 単一のrelationship用（thumbnail、categoryなど）
@JsonClass(generateAdapter = true)
data class VideoRelationshipDto(
    val data: VideoReferenceDto? = null,
)

@JsonClass(generateAdapter = true)
data class VideoReferenceDto(
    val id: String,
    val type: String,
)
```

#### 2.2 VideoListResponseDtoの更新

`included`配列には異なるタイプのエンティティ（ファイル、タクソノミー用語、画像メディアなど）が含まれるため、汎用的なDTOに変更します。

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/data/dto/VideoListResponseDto.kt`

```kotlin
package org.dhis2.usescases.videoGuide.data.dto

import com.squareup.moshi.JsonClass

// included配列には異なるタイプのエンティティが含まれる可能性があるため、
// 汎用的なDTOとして定義
@JsonClass(generateAdapter = true)
data class VideoListResponseDto(
    val data: List<VideoMediaDto>,
    val included: List<VideoIncludedDto>? = null,
)

// included配列の各要素を表す汎用DTO
// 実際の型はtypeフィールドで判別
@JsonClass(generateAdapter = true)
data class VideoIncludedDto(
    val id: String,
    val type: String,
    val attributes: Map<String, Any>? = null,
    val relationships: Map<String, Any>? = null,
)
```

### 3. VideoMapperの更新

メタ情報をマッピングするためのメソッドを追加し、`mapToDomain`メソッドを更新します。

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/data/mapper/VideoMapper.kt`

```kotlin
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
```

### 4. VideoApiServiceの更新

APIリクエストの`include`パラメータに、サムネイル画像、タグ、カテゴリを含めます。

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/data/api/VideoApiService.kt`

```kotlin
package org.dhis2.usescases.videoGuide.data.api

import org.dhis2.usescases.videoGuide.data.dto.VideoListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface VideoApiService {
    @GET("jsonapi/media/video")
    suspend fun getVideos(
        @Query("include") include: String = "field_media_video_file,thumbnail,field_video_thumbnail.field_media_image,field_video_category,field_video_tag",
    ): VideoListResponseDto

    @GET("jsonapi/media/video/{id}")
    suspend fun getVideo(
        @Path("id") id: String,
        @Query("include") include: String = "field_media_video_file,thumbnail,field_video_thumbnail.field_media_image,field_video_category,field_video_tag",
    ): VideoListResponseDto
}
```

**includeパラメータの説明**:
- `field_media_video_file`: 動画ファイル（既存）
- `thumbnail`: サムネイル画像（直接ファイル参照）
- `field_video_thumbnail.field_media_image`: サムネイル画像（メディア → ファイルのネストした関係）
- `field_video_category`: カテゴリ（タクソノミー用語）
- `field_video_tag`: タグ（タクソノミー用語）

### 5. DrupalVideoApiDataSourceの更新

`VideoMapper`の新しいメソッドを使用して、メタ情報をマッピングします。

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/data/datasource/DrupalVideoApiDataSource.kt`

```kotlin
package org.dhis2.usescases.videoGuide.data.datasource

import org.dhis2.usescases.videoGuide.data.api.VideoApiService
import org.dhis2.usescases.videoGuide.data.mapper.VideoMapper
import org.dhis2.usescases.videoGuide.domain.model.VideoItem
import timber.log.Timber
import javax.inject.Inject

class DrupalVideoApiDataSource @Inject constructor(
    private val api: VideoApiService,
    private val mapper: VideoMapper,
    private val baseUrl: String,
) : VideoRemoteDataSource {

    override suspend fun getVideoList(): List<VideoItem> {
        return runCatching {
            val response = api.getVideos()
            
            // 各種マップを作成
            val filesMap = mapper.createFilesMap(response.included)
            val imageFilesMap = mapper.createImageFilesMap(response.included)
            val taxonomyMap = mapper.createTaxonomyMap(response.included)
            val mediaImageMap = mapper.createMediaImageMap(response.included)

            response.data.mapNotNull { media ->
                mapper.mapToDomain(
                    media = media,
                    filesMap = filesMap,
                    baseUrl = baseUrl,
                    imageFilesMap = imageFilesMap,
                    taxonomyMap = taxonomyMap,
                    mediaImageMap = mediaImageMap,
                )
            }
        }.onFailure { throwable ->
            Timber.e(throwable, "Failed to fetch videos from Drupal API")
        }.getOrElse { emptyList() }
    }

    override suspend fun getVideoById(videoId: String): VideoItem? {
        return runCatching {
            val response = api.getVideo(videoId)
            
            // 各種マップを作成
            val filesMap = mapper.createFilesMap(response.included)
            val imageFilesMap = mapper.createImageFilesMap(response.included)
            val taxonomyMap = mapper.createTaxonomyMap(response.included)
            val mediaImageMap = mapper.createMediaImageMap(response.included)

            response.data.firstOrNull()?.let { media ->
                mapper.mapToDomain(
                    media = media,
                    filesMap = filesMap,
                    baseUrl = baseUrl,
                    imageFilesMap = imageFilesMap,
                    taxonomyMap = taxonomyMap,
                    mediaImageMap = mediaImageMap,
                )
            }
        }.onFailure { throwable ->
            Timber.e(throwable, "Failed to fetch video by id: $videoId")
        }.getOrNull()
    }
}
```

## データフロー

```
1. VideoApiService.getVideos()がAPIを呼び出す
   ↓
2. VideoListResponseDtoが返される
   - data: メディア情報の配列
   - included: ファイル、タクソノミー用語、画像メディアなどの配列
   ↓
3. VideoMapperで各種マップを作成
   - createFilesMap(): 動画ファイルID → URL
   - createImageFilesMap(): 画像ファイルID → URL
   - createTaxonomyMap(): タクソノミー用語ID → 名前
   - createMediaImageMap(): 画像メディアID → ファイルID
   ↓
4. mapToDomain()でDTO → Domain Model変換
   - 動画URLを構築
   - タイトルを取得（field_titleを優先、なければname）
   - 説明を取得（field_description）
   - サムネイルURLを構築（field_video_thumbnailを優先、なければthumbnail）
   - タグを取得（タクソノミー用語ID → 名前、単一）
   - カテゴリを取得（タクソノミー用語ID → 名前、単一）
   ↓
5. VideoItemが返される
```

## Drupalフィールド名の確認方法

実装前に、実際のDrupal APIレスポンスを確認して、フィールド名が正しいか確認してください。

### APIレスポンスの確認方法

1. ブラウザで以下のURLにアクセス：
   ```
   https://your-drupal-site.com/jsonapi/media/video?include=field_media_video_file,thumbnail,field_video_thumbnail.field_media_image,field_video_category,field_video_tag
   ```

2. JSONレスポンスを確認し、以下のフィールド名を確認：
   - `attributes`内のフィールド名（例: `field_title`, `field_description`）
   - `relationships`内のフィールド名（例: `thumbnail`, `field_video_thumbnail`, `field_video_tag`, `field_video_category`）
   - `included`配列の`type`フィールド（例: `file--file`, `taxonomy_term--video_category`, `taxonomy_term--video_tags`, `media--image`）

3. フィールド名が異なる場合は、以下のファイルを修正：
   - `VideoMediaDto.kt`: `@Json(name = "...")`アノテーション
   - `VideoApiService.kt`: `include`パラメータ
   - `VideoMapper.kt`: `type`フィルタリング条件

## よくある問題と対処法

### 1. サムネイル画像が取得できない

**原因**: 
- `thumbnail`または`field_video_thumbnail`フィールド名が異なる
- `include`パラメータが正しくない（ネストした関係が含まれていない）

**対処法**:
- Drupal APIレスポンスで`relationships`内のフィールド名を確認
- `include`パラメータに`thumbnail`と`field_video_thumbnail.field_media_image`が含まれているか確認
- `field_video_thumbnail`が`media--image`型の場合、そのメディアの`field_media_image`からファイルIDを取得する必要がある

### 2. タグやカテゴリが取得できない

**原因**:
- タクソノミー用語の`type`が`taxonomy_term--video_category`や`taxonomy_term--video_tags`ではなく、別の形式
- `included`配列にタクソノミー用語が含まれていない

**対処法**:
- Drupal APIレスポンスで`included`配列の`type`を確認
- `VideoMapper.createTaxonomyMap()`のフィルタ条件を調整（例: `it.type.startsWith("taxonomy_term--")`）
- `field_video_tag`と`field_video_category`が単一の参照であることを確認（配列ではない）

### 3. フィールド名が異なる

**原因**: Drupalの設定により、フィールド名が異なる可能性がある

**対処法**:
- `VideoMediaDto.kt`の`@Json(name = "...")`アノテーションを修正
- `VideoApiService.kt`の`include`パラメータを修正

## テスト方法

1. **APIレスポンスの確認**
   - Drupal APIに直接アクセスして、期待するフィールドが含まれているか確認

2. **マッパーのテスト**
   - `VideoMapper`の各メソッドが正しくマップを作成しているか確認
   - `mapToDomain`が正しくメタ情報をマッピングしているか確認

3. **統合テスト**
   - アプリを実行して、動画一覧にメタ情報が表示されるか確認
   - `field_title`が設定されている場合はそれが表示され、なければ`name`が表示されるか確認
   - `field_description`が表示されるか確認
   - サムネイル画像が表示されるか確認（`field_video_thumbnail`と`thumbnail`の両方をテスト）
   - タグやカテゴリが表示されるか確認（単一の値として）

## 参考

- [Drupal JSON:API Documentation](https://www.drupal.org/docs/core-modules-and-modules/jsonapi-module)
- [VIDEO_GUIDE_IMPLEMENTATION.md](./VIDEO_GUIDE_IMPLEMENTATION.md): 既存の実装ドキュメント
