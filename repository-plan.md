
# 🎯 最終目標：

`VideoGuideRepository` が **どんなデータソースにも依存しないようにする**

将来的に以下を自由に切り替えられるようにする：

* Dummy データ（開発・検証用）
* Drupal API（本番用）
* キャッシュ（Room）
* 追加メタデータ（DHIS2 側を使う可能性がある場合）

これを実現するには Repository を「抽象化」して、
**DataSource（インターフェース）を本体に差し込む構造** が最適です。

---

# 🧭 改善後のアーキテクチャ（推奨構成）

```
VideoGuideRepository（ビジネスロジック）
    ↓
VideoRemoteDataSource（インターフェース）
    ├── DrupalVideoApiDataSource（本番）
    ├── DummyVideoDataSource（開発用）
    └── LocalCacheDataSource（将来的にRoom用）
```

---

# 📁 推奨ディレクトリ構造（現実的・保守性◎）

```
videoGuide/
├── data/
│   ├── datasource/
│   │   ├── VideoRemoteDataSource.kt
│   │   ├── DrupalVideoApiDataSource.kt
│   │   └── DummyVideoDataSource.kt
│   ├── api/
│   │   ├── VideoApiService.kt      # Retrofitインターフェース
│   │   └── VideoDto.kt             # APIレスポンスDTO
│   ├── mapper/
│   │   └── VideoMapper.kt          # DTO → Domain Model変換
│   └── repository/
│       └── VideoGuideRepository.kt
├── domain/
│   └── model/
│       └── VideoItem.kt
└── ui/
    └── ...（既存のUI）
```

---

# 🔥 **1. DataSource を定義する（最重要）**

```kotlin
interface VideoRemoteDataSource {
    suspend fun getVideoList(): List<VideoItem>
    suspend fun getVideoById(videoId: String): VideoItem?
}
```

### ✨ この利点

* Repository は DataSource にしか依存しなくなる
* Drupal API が変わっても Repository を書き換える必要がない
* 依存注入を変えるだけで DataSource を切り替えられる

Capture の一般的な DataSource パターンとも一致。

---

# 🔥 **2. Drupal API 用の DataSource 実装**

### Retrofit API Client（VideoApiService）

```kotlin
interface VideoApiService {
    @GET("videos")
    suspend fun getVideos(): List<VideoDto>

    @GET("videos/{id}")
    suspend fun getVideo(@Path("id") id: String): VideoDto
}
```

### DTO → Domain Model

```kotlin
data class VideoDto(
    val id: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
    val videoUrl: String
)
```

---

### DrupalVideoApiDataSource の実装

```kotlin
class DrupalVideoApiDataSource @Inject constructor(
    private val api: VideoApiService,
    private val mapper: VideoMapper
) : VideoRemoteDataSource {

    override suspend fun getVideoList(): List<VideoItem> {
        return api.getVideos().map { mapper.mapToDomain(it) }
    }

    override suspend fun getVideoById(videoId: String): VideoItem? {
        return runCatching {
            api.getVideo(videoId)
        }.getOrNull()?.let { mapper.mapToDomain(it) }
    }
}
```

---

# 🔥 **3. DummyDataSource（開発・オフライン用）**

```kotlin
class DummyVideoDataSource @Inject constructor() : VideoRemoteDataSource {

    private val dummy = listOf(
        VideoItem(
            id = "1",
            title = "サンプル動画",
            description = "デモ用動画",
            thumbnailUrl = null,
            videoUrl = "https://example.com/sample.mp4"
        )
    )

    override suspend fun getVideoList() = dummy

    override suspend fun getVideoById(videoId: String) =
        dummy.find { it.id == videoId }
}
```

👉 開発中はこれだけで UI が動かせるようになる！

---

# 🔥 **4. Repository の再設計（スッキリする）**

```kotlin
class VideoGuideRepository @Inject constructor(
    private val dataSource: VideoRemoteDataSource
) {

    suspend fun getVideos(): List<VideoItem> {
        return dataSource.getVideoList()
    }

    suspend fun getVideo(videoId: String): VideoItem? {
        return dataSource.getVideoById(videoId)
    }
}
```

### ✨ Repository が軽量化されるメリット

* データ取得の手段に依存しない
* ビジネスロジックに専念できる
* 将来「キャッシュ追加」「Room対応」などが簡単に実装できる

---

# 🔥 **5. Daggerでの切り替え（超強力）**

### VideoGuideModule で DataSource を選択可能

```kotlin
@Module
class VideoGuideModule {

    @Provides
    @PerFragment
    fun provideDataSource(
        api: VideoApiService,
        mapper: VideoMapper
    ): VideoRemoteDataSource {
        // ★ ここを切り替えるだけ！
        return DrupalVideoApiDataSource(api, mapper)
        // return DummyVideoDataSource()
    }

    @Provides
    @PerFragment
    fun provideRepository(
        dataSource: VideoRemoteDataSource
    ) = VideoGuideRepository(dataSource)
}
```

これが Capture の DI パターンと完全に一致する。

---

# 🔥 **6. Mapper（Domain モデルを未来永劫安定させる）**

```kotlin
class VideoMapper @Inject constructor() {
    fun mapToDomain(dto: VideoDto): VideoItem {
        return VideoItem(
            id = dto.id,
            title = dto.title,
            description = dto.description ?: "",
            thumbnailUrl = dto.thumbnailUrl,
            videoUrl = dto.videoUrl
        )
    }
}
```

Mapper を噛ませることで：

* Drupal の API 仕様変更に強くなる
* Domain Model（VideoItem）を安定して保てる

---

# 🎯 **最終的な構造（完璧な拡張性）**

```
VideoGuideViewModel
    ↓
VideoGuideRepository
    ↓
VideoRemoteDataSource（インターフェース）
    ├── DrupalVideoApiDataSource（本番）
    ├── DummyVideoDataSource（開発）
    └── LocalCacheDataSource（将来）
```

* DataSource を複数持てる
* Repository は固定
* ViewModel は Repository のみ依存
* DI で差し替え可能

Capture のパターン（「データソースを抽象化する」）にも完全一致。

---

# ✨ まとめ：この設計にすれば後から無限に拡張できる

* 明確な責務分離
* Drupal API を追加しやすい
* Repository の肥大化を防ぐ
* オフライン/キャッシュ対応がしやすい
* Capture の既存アーキテクチャに完全一致
