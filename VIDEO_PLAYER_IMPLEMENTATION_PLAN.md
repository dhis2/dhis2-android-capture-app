# VideoPlayer実装計画書 - ExoPlayer統合とオフライン再生機能

## 📋 最終目標

**Drupalから動画をダウンロードして、オフラインで動画を再生できるようにする**

### 達成すべき機能

1. ✅ **動画一覧の表示**（既に実装済み）
2. ✅ **動画のオンライン再生**（ExoPlayer統合）← フェーズ1完了
3. 🔄 **動画のダウンロード機能**（ExoPlayer DownloadService）
4. 🔄 **ダウンロード済み動画のオフライン再生**
5. 🔄 **ダウンロード状態の管理とUI表示**
6. 🔄 **ローカルストレージ管理**（Roomデータベース）

---

## 🏗️ アーキテクチャ拡張

### 現状のアーキテクチャ

```
VideoGuideFragment
  └── VideoGuideViewModel
       └── VideoGuideRepository
            └── VideoRemoteDataSource (Drupal API)
```

### 拡張後のアーキテクチャ

```
VideoGuideFragment
  └── VideoGuideViewModel
       └── VideoGuideRepository
            ├── VideoRemoteDataSource (Drupal API)
            └── VideoLocalDataSource (Room DB) ← 新規追加

VideoPlayerActivity
  └── VideoPlayerViewModel
       └── VideoGuideRepository (動画情報取得)
            └── VideoRemoteDataSource (Drupal API)

※ フェーズ3以降で以下を追加予定：
   - VideoDownloadManager (ダウンロード管理)
   - ExoPlayerManager (再生管理)
```

---

## 📦 実装フェーズ

### フェーズ1: ExoPlayerの基本統合とオンライン再生 ✅ 完了

**目標**: ExoPlayerを使って動画をオンラインで再生できるようにする

**実装完了日**: 2024年12月

#### 1.1 依存関係の追加

**ファイル**: `app/build.gradle.kts`

**フェーズ1で追加した依存関係**:
```kotlin
dependencies {
    // ExoPlayer Core（フェーズ1で追加）
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    implementation("androidx.media3:media3-common:1.2.0")
}
```

**注意**: フェーズ2以降で必要になる依存関係は、各フェーズの実装時に追加します：
- ExoPlayer Download関連（フェーズ3で追加予定）
- Room Database関連（フェーズ2で追加予定）

#### 1.2 VideoPlayerActivityの実装

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/video/VideoPlayerActivity.kt`

**実装内容**:
- DIの設定（VideoPlayerModule/Componentの作成）
- ViewModelの注入
- ExoPlayerのセットアップ
- 動画情報の取得（Repository経由）
- ExoPlayerViewのレイアウト設定
- ライフサイクル管理（onPause/onResume/onDestroy）

**必要な権限**: 既に`INTERNET`権限は追加済み

#### 1.3 VideoPlayerViewModelの作成

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/video/VideoPlayerViewModel.kt`

**責務**:
- 動画情報の取得（`VideoGuideRepository.getVideoById()`）
- 再生状態の管理
- エラーハンドリング

**プロパティ**:
- `videoItem: LiveData<VideoItem?>`
- `isLoading: LiveData<Boolean>`
- `errorMessage: LiveData<String?>`

#### 1.4 VideoPlayerModule/Componentの作成

**ファイル**: 
- `app/src/main/java/org/dhis2/usescases/videoGuide/video/VideoPlayerModule.kt`
- `app/src/main/java/org/dhis2/usescases/videoGuide/video/VideoPlayerComponent.kt`

**提供する依存関係**:
- `ActivityGlobalAbstract`（コンストラクタで受け取る）
- `ViewModelStoreOwner`（コンストラクタで受け取る）
- `VideoApiService`（Retrofit API）
- `VideoRemoteDataSource`（DrupalVideoApiDataSource）
- `VideoGuideRepository`（VideoPlayerModule内で作成）
- `VideoPlayerViewModelFactory`
- `VideoPlayerViewModel`（ViewModelProvider経由）

**DIパターン**: `@PerActivity`スコープを使用（既存の`LoginModule`パターンを参考）

**実装の注意点**:
- `VideoGuideRepository`は`@PerFragment`スコープだが、`VideoPlayerModule`は`@PerActivity`スコープのため、`VideoPlayerModule`内で`VideoGuideRepository`を新規作成している
- `VideoGuideModule`と同じ依存関係（VideoApiService、VideoMapper、baseUrl）を使用して`VideoGuideRepository`を構築

#### 1.5 AppComponentへの追加

**ファイル**: `app/src/main/java/org/dhis2/AppComponent.java`

```kotlin
VideoPlayerComponent plus(VideoPlayerModule videoPlayerModule)
```

**注意**: `MainComponent`ではなく`AppComponent`に追加（`LoginActivity`と同様のパターン）

#### 1.6 AndroidManifest.xmlへの登録

**ファイル**: `app/src/main/AndroidManifest.xml`

```xml
<activity
    android:name="org.dhis2.usescases.videoGuide.video.VideoPlayerActivity"
    android:configChanges="orientation|screenSize|keyboardHidden"
    android:screenOrientation="portrait" />
```

#### 1.7 レイアウトファイルの作成

**ファイル**: `app/src/main/res/layout/activity_video_player.xml`

**コンポーネント**:
- `PlayerView`（ExoPlayerのUIコンポーネント）
- ローディングインジケーター（ProgressBar）
- エラーメッセージ表示（TextView）

#### 1.8 VideoResponseDtoの作成（追加実装）

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/data/dto/VideoResponseDto.kt`

**実装理由**: Drupal JSON:APIの個別取得エンドポイント（`jsonapi/media/video/{id}`）は、一覧取得とは異なり、`data`フィールドが配列ではなく単一オブジェクトで返されるため。

```kotlin
@JsonClass(generateAdapter = true)
data class VideoResponseDto(
    val data: VideoMediaDto,  // 配列ではなく単一オブジェクト
    val included: List<VideoIncludedDto>? = null,
)
```

**VideoApiServiceの修正**:
- `getVideo()`メソッドの戻り値の型を`VideoListResponseDto`から`VideoResponseDto`に変更

**DrupalVideoApiDataSourceの修正**:
- `getVideoById()`で`response.data`を直接使用（`firstOrNull()`ではなく）

---

### フェーズ2: Roomデータベースによるダウンロード状態管理

**目標**: ダウンロード済み動画の情報をローカルDBに保存・管理する

#### 2.1 Room Databaseのセットアップ

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/data/local/VideoDatabase.kt`

**エンティティ**: `DownloadedVideoEntity`
```kotlin
@Entity(tableName = "downloaded_videos")
data class DownloadedVideoEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val description: String,
    val videoUrl: String,
    val thumbnailUrl: String?,
    val localFilePath: String, // ダウンロード先のローカルパス
    val downloadedAt: Long, // ダウンロード日時
    val fileSize: Long, // ファイルサイズ（バイト）
    val duration: Long? // 動画の長さ（ミリ秒）
)
```

**DAO**: `DownloadedVideoDao`
```kotlin
@Dao
interface DownloadedVideoDao {
    @Query("SELECT * FROM downloaded_videos")
    suspend fun getAll(): List<DownloadedVideoEntity>
    
    @Query("SELECT * FROM downloaded_videos WHERE videoId = :videoId")
    suspend fun getById(videoId: String): DownloadedVideoEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(video: DownloadedVideoEntity)
    
    @Delete
    suspend fun delete(video: DownloadedVideoEntity)
    
    @Query("DELETE FROM downloaded_videos WHERE videoId = :videoId")
    suspend fun deleteById(videoId: String)
}
```

**Database**: `VideoDatabase`
```kotlin
@Database(
    entities = [DownloadedVideoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VideoDatabase : RoomDatabase() {
    abstract fun downloadedVideoDao(): DownloadedVideoDao
}
```

#### 2.2 VideoLocalDataSourceの作成

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/data/datasource/VideoLocalDataSource.kt`

**インターフェース**:
```kotlin
interface VideoLocalDataSource {
    suspend fun getAllDownloadedVideos(): List<VideoItem>
    suspend fun getDownloadedVideoById(videoId: String): VideoItem?
    suspend fun saveDownloadedVideo(video: VideoItem, localFilePath: String)
    suspend fun deleteDownloadedVideo(videoId: String)
    suspend fun isDownloaded(videoId: String): Boolean
}
```

**実装**: `RoomVideoLocalDataSource.kt`

#### 2.3 VideoGuideRepositoryの拡張

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/VideoGuideRepository.kt`

**追加メソッド**:
```kotlin
suspend fun getDownloadedVideoList(): List<VideoItem>
suspend fun isVideoDownloaded(videoId: String): Boolean
```

**実装方針**: 
- `VideoLocalDataSource`を依存関係として追加
- リモートとローカルの両方から取得できるようにする

**注意**: 既存の`getVideoById()`メソッドは変更不要。フェーズ4でオフライン再生を実装する際に、ローカルDBからも取得できるように拡張する可能性がある。

#### 2.4 VideoGuideModuleの拡張

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/VideoGuideModule.kt`

**追加する`@Provides`メソッド**:
- `provideVideoDatabase(context: Context): VideoDatabase`
- `provideDownloadedVideoDao(database: VideoDatabase): DownloadedVideoDao`
- `provideVideoLocalDataSource(dao: DownloadedVideoDao): VideoLocalDataSource`

---

### フェーズ3: ExoPlayer DownloadServiceによるダウンロード機能

**目標**: ExoPlayerのDownloadServiceを使って動画をダウンロードする

**重要**: Media3 1.2.0では、ダウンロード関連のクラスは`androidx.media3.exoplayer.offline`パッケージにあります。

#### 3.1 DownloadManagerとSimpleCacheのセットアップ

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/VideoGuideModule.kt` / `VideoPlayerModule.kt`

**実装内容**:
- `StandaloneDatabaseProvider`の作成
- `SimpleCache`の作成（キャッシュディレクトリ: `context.cacheDir/video_downloads`）
- `DownloadManager`の作成と設定

**依存関係**:
```kotlin
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.exoplayer.offline.DownloadManager
```

**注意**: Media3では、ダウンロードされたファイルは`SimpleCache`に保存されますが、**実際のファイルパスを取得する必要はありません**。再生時には元のURLを使用し、ExoPlayerが自動的にキャッシュから読み込みます。

#### 3.2 DownloadTrackerの作成

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/video/DownloadTracker.kt`

**責務**:
- `DownloadManager.Listener`を実装
- ダウンロード状態の監視
- `DownloadManager`からの状態更新を受け取る
- UIへの状態通知（LiveData）

**主要プロパティ**:
```kotlin
val downloadStates: LiveData<Map<String, Download>>
val downloadProgress: LiveData<Map<String, Int>>
```

**主要メソッド**:
```kotlin
fun getDownloadState(videoId: String): Download?
fun getDownloadProgress(videoId: String): Int
fun release()
```

#### 3.3 VideoDownloadServiceの作成

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/video/VideoDownloadService.kt`

**実装内容**:
- `DownloadService`を継承
- ダウンロードの実行と管理
- フォアグラウンドサービスとして動作
- ダウンロード進捗の通知表示

**AndroidManifest.xmlへの追加**:
```xml
<service
    android:name="org.dhis2.usescases.videoGuide.video.VideoDownloadService"
    android:foregroundServiceType="mediaPlayback"
    android:exported="false" />
```

**必要な権限**:
- `FOREGROUND_SERVICE`（既に追加済み）
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK`（Android 14+）

#### 3.4 VideoDownloadManagerの作成

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/video/VideoDownloadManager.kt`

**責務**:
- ExoPlayerの`DownloadService`との連携
- ダウンロードリクエストの管理
- ダウンロード状態の公開（DownloadTracker経由）
- Room DBへの保存支援

**主要プロパティ**:
```kotlin
val downloadStates: LiveData<Map<String, Download>>
val downloadProgress: LiveData<Map<String, Int>>
```

**主要メソッド**:
```kotlin
fun downloadVideo(videoItem: VideoItem)
fun cancelDownload(videoId: String)
fun getDownloadState(videoId: String): Download?
fun getAllDownloads(): List<Download>
fun getDownloadProgress(videoId: String): Int
suspend fun saveDownloadedVideoToDatabase(videoItem: VideoItem, localFilePath: String)
```

**重要な実装ポイント**:
- `downloadTracker`はprivateプロパティとして保持し、必要なデータのみをpublicプロパティとして公開
- ダウンロード完了時の処理は簡素化（ファイルパス取得は不要）
- Room DBへの保存は、ViewModelから明示的に呼び出す

**注意**: `getDownloadedFilePath()`のようなメソッドは実装不要です。Media3では、ダウンロード済みファイルの再生に元のURLを使用し、ExoPlayerが自動的にキャッシュから読み込みます。

#### 3.5 VideoPlayerViewModelの拡張

**追加プロパティ**:
- `downloadState: LiveData<Download?>`（現在の動画のダウンロード状態）
- `downloadProgress: LiveData<Int>`（0-100%）

**追加メソッド**:
- `startDownload()`: 現在の動画をダウンロード
- `cancelDownload()`: 現在の動画のダウンロードをキャンセル
- `checkDownloadState()`: ダウンロード状態を確認

**ダウンロード完了時の処理**:
```kotlin
// downloadStatesを監視して、ダウンロード完了時にRoom DBに保存
downloadManager.downloadStates.observeForever { downloads ->
    downloads.values.forEach { download ->
        if (download.state == Download.STATE_COMPLETED) {
            viewModelScope.launch {
                val video = repository.getVideoById(download.request.id)
                if (video != null) {
                    // キャッシュディレクトリのパスを保存（実際のファイルパスは不要）
                    val cachePath = File(context.cacheDir, "video_downloads").absolutePath
                    downloadManager.saveDownloadedVideoToDatabase(video, cachePath)
                }
            }
        }
    }
}
```

#### 3.6 VideoGuideViewModelの拡張

**追加プロパティ**:
- `downloadStates: LiveData<Map<String, Download>>`（各動画のダウンロード状態）
- `downloadProgress: LiveData<Map<String, Int>>`（各動画のダウンロード進捗）

**追加メソッド**:
- `startDownload(videoId: String)`: 動画のダウンロードを開始
- `cancelDownload(videoId: String)`: 動画のダウンロードをキャンセル
- `checkDownloadState(videoId: String)`: ダウンロード状態を確認
- `getDownloadedVideos()`: ローカルDBからダウンロード済み動画を取得

**ダウンロード完了時の処理**:
VideoPlayerViewModelと同様に、`downloadStates`を監視してダウンロード完了時にRoom DBに保存します。

---

### フェーズ4: オフライン再生機能の実装

**目標**: ダウンロード済み動画をオフラインで再生できるようにする

#### 4.1 ExoPlayerManagerの作成

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/video/ExoPlayerManager.kt`

**責務**:
- ExoPlayerインスタンスの管理
- オンライン/オフライン再生の切り替え
- メディアアイテムの準備
- 再生状態の管理

**主要メソッド**:
```kotlin
fun preparePlayer(videoItem: VideoItem, isOffline: Boolean)
fun releasePlayer()
fun getPlayer(): ExoPlayer?
```

#### 4.2 オフライン再生ロジックの実装

**実装場所**: `VideoPlayerActivity`または`ExoPlayerManager`

**重要な理解**: Media3では、ダウンロード済みファイルの再生に**元のURLを使用**します。ExoPlayerが自動的に`SimpleCache`から読み込みます。実際のファイルパスを取得する必要はありません。

**処理フロー**:
1. 動画IDから動画情報を取得
   - まずローカルDBから取得を試みる（`VideoLocalDataSource.getDownloadedVideoById()`）
   - ローカルにない場合はリモートから取得（`VideoGuideRepository.getVideoById()`）
2. ダウンロード済みかチェック
   - `DownloadIndex.getDownload(videoId)`でダウンロード状態を確認
   - または`VideoGuideRepository.isVideoDownloaded(videoId)`でローカルDBを確認
3. 再生方法:
   - **ダウンロード済みの場合**: 元のURLを使用して`MediaItem.fromUri(videoUrl)`を作成
     - ExoPlayerが自動的に`SimpleCache`から読み込む
     - `FileDataSource`は使用しない（Media3が自動的に処理）
   - **未ダウンロードの場合**: 元のURLを使用してオンライン再生

**実装例**:
```kotlin
// ExoPlayerManager.kt
fun prepareMediaItem(videoUrl: String, isDownloaded: Boolean) {
    // Media3では、ダウンロード済みでも元のURLを使用
    // ExoPlayerが自動的にキャッシュから読み込む
    val mediaItem = MediaItem.fromUri(videoUrl)
    player.setMediaItem(mediaItem)
    player.prepare()
}
```

**注意**: 
- `FileDataSource`を直接使用する必要はありません
- `DownloadRequest`のURI（元のURL）を使用することで、ExoPlayerが自動的にキャッシュを検索します
- オフライン時でも、元のURLを使用すればキャッシュから再生されます

#### 4.3 VideoPlayerActivityの拡張

**追加機能**:
- オフライン/オンライン状態の表示
- ダウンロードボタン（未ダウンロード時）
- 削除ボタン（ダウンロード済み時）

---

### フェーズ5: UIの改善とダウンロード状態表示

**目標**: ダウンロード状態を視覚的に表示し、ユーザーが操作しやすくする

#### 5.1 VideoGuideScreenの拡張

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/ui/VideoGuideScreen.kt`

**追加機能**:
- ダウンロード済みバッジの表示
- ダウンロード中インジケーター
- ダウンロード進捗バー
- ダウンロードボタン（各動画カードに）

#### 5.2 VideoItemCardの拡張

**追加UI要素**:
- ダウンロードアイコン（状態に応じて変化）
- ダウンロード進捗バー
- オフライン再生可能バッジ

#### 5.3 VideoPlayerActivityのUI改善

**追加UI要素**:
- ダウンロードボタン（未ダウンロード時）
- ダウンロード進捗表示
- オフライン再生インジケーター
- 削除ボタン（ダウンロード済み動画の削除）

---

## 📁 新しいディレクトリ構造

```
app/src/main/java/org/dhis2/usescases/videoGuide/
├── VideoGuideFragment.kt
├── VideoGuideModule.kt
├── VideoGuideComponent.kt
├── VideoGuideRepository.kt
├── VideoGuideViewModel.kt
├── VideoGuideViewModelFactory.kt
├── data/
│   ├── api/
│   │   └── VideoApiService.kt
│   ├── datasource/
│   │   ├── VideoRemoteDataSource.kt
│   │   ├── DrupalVideoApiDataSource.kt
│   │   ├── DummyVideoDataSource.kt
│   │   ├── VideoLocalDataSource.kt          # 新規
│   │   └── RoomVideoLocalDataSource.kt      # 新規
│   ├── dto/
│   │   ├── VideoListResponseDto.kt
│   │   ├── VideoResponseDto.kt              # 新規（個別取得用）
│   │   ├── VideoMediaDto.kt
│   │   └── VideoFileDto.kt
│   ├── local/                                # 新規
│   │   ├── VideoDatabase.kt                  # 新規
│   │   ├── DownloadedVideoEntity.kt         # 新規
│   │   └── DownloadedVideoDao.kt             # 新規
│   └── mapper/
│       └── VideoMapper.kt
├── domain/
│   └── model/
│       └── VideoItem.kt
├── ui/
│   └── VideoGuideScreen.kt
└── video/                                    # 拡張
    ├── VideoPlayerActivity.kt                # 実装
    ├── VideoPlayerModule.kt                  # 新規
    ├── VideoPlayerComponent.kt               # 新規
    ├── VideoPlayerViewModel.kt               # 新規
    ├── VideoPlayerViewModelFactory.kt         # 新規
    ├── VideoDownloadManager.kt                # 新規
    ├── VideoDownloadService.kt               # 新規
    ├── DownloadTracker.kt                    # 新規
    └── ExoPlayerManager.kt                   # 新規
```

---

## 🔧 実装の詳細

### ExoPlayerのセットアップ

#### ExoPlayerインスタンスの作成

```kotlin
val exoPlayer = ExoPlayer.Builder(context)
    .setMediaSourceFactory(
        DefaultMediaSourceFactory(dataSourceFactory)
    )
    .build()
```

#### データソースファクトリの設定

**オンライン再生用**:
```kotlin
val httpDataSourceFactory = HttpDataSource.Factory()
    .setUserAgent("DHIS2-Android-Capture")
    .setAllowCrossProtocolRedirects(true)
```

**オフライン再生用**:
```kotlin
val fileDataSourceFactory = FileDataSource.Factory()
```

#### メディアアイテムの準備

```kotlin
val mediaItem = MediaItem.fromUri(videoUrl)
exoPlayer.setMediaItem(mediaItem)
exoPlayer.prepare()
```

### ダウンロード機能の実装

#### DownloadManagerとSimpleCacheのセットアップ

```kotlin
// VideoGuideModule.kt または VideoPlayerModule.kt
val databaseProvider = StandaloneDatabaseProvider(context)
val downloadCache = SimpleCache(
    File(context.cacheDir, "video_downloads"),
    NoOpCacheEvictor(),
    databaseProvider
)
val dataSourceFactory: HttpDataSource.Factory =
    DefaultHttpDataSource.Factory()
        .setUserAgent(Util.getUserAgent(context, "DHIS2-Android-Capture"))
        .setAllowCrossProtocolRedirects(true)

val downloadManager = DownloadManager(
    context,
    databaseProvider,
    downloadCache,
    dataSourceFactory,
    Executors.newSingleThreadExecutor()
)
```

#### DownloadRequestの作成

```kotlin
val downloadRequest = DownloadRequest.Builder(videoId, Uri.parse(videoUrl))
    .setMimeType(MimeTypes.VIDEO_MP4)
    .setData(videoItem.title.toByteArray()) // メタデータとして保存
    .build()
```

#### DownloadServiceへの送信

```kotlin
DownloadService.sendAddDownload(
    context,
    VideoDownloadService::class.java,
    downloadRequest,
    false // 即座に開始
)
```

#### ダウンロード状態の監視

```kotlin
// DownloadTrackerをリスナーとして追加
downloadManager.addListener(downloadTracker)

// DownloadTrackerはDownloadManager.Listenerを実装
class DownloadTracker(
    private val downloadManager: DownloadManager,
) : DownloadManager.Listener {
    // 状態をLiveDataで公開
    val downloadStates: LiveData<Map<String, Download>>
    val downloadProgress: LiveData<Map<String, Int>>
}
```

#### ダウンロード完了時の処理

**重要**: Media3では、ダウンロードされたファイルのパスを取得する必要はありません。再生時には元のURLを使用し、ExoPlayerが自動的にキャッシュから読み込みます。

```kotlin
// ViewModelでダウンロード完了を監視
downloadManager.downloadStates.observeForever { downloads ->
    downloads.values.forEach { download ->
        if (download.state == Download.STATE_COMPLETED) {
            viewModelScope.launch {
                // VideoItemを取得してRoom DBに保存
                val video = repository.getVideoById(download.request.id)
                if (video != null) {
                    // キャッシュディレクトリのパスを保存（実際のファイルパスは不要）
                    val cachePath = File(context.cacheDir, "video_downloads").absolutePath
                    downloadManager.saveDownloadedVideoToDatabase(video, cachePath)
                }
            }
        }
    }
}
```

### Roomデータベースの実装

#### DatabaseProviderの作成

```kotlin
val databaseProvider = StandaloneDatabaseProvider(context)
```

#### ダウンロード情報の保存

**注意**: Media3では、ダウンロードされたファイルは`SimpleCache`に保存されますが、実際のファイルパスを取得する必要はありません。`localFilePath`にはキャッシュディレクトリのパスを保存しますが、これは主にダウンロード済みかどうかの判定に使用します。

```kotlin
suspend fun saveDownloadedVideo(videoItem: VideoItem, localFilePath: String) {
    val entity = DownloadedVideoEntity(
        videoId = videoItem.id,
        title = videoItem.title,
        description = videoItem.description,
        videoUrl = videoItem.videoUrl, // 再生時にはこのURLを使用
        thumbnailUrl = videoItem.thumbnailUrl,
        localFilePath = localFilePath, // キャッシュディレクトリのパス（主に判定用）
        downloadedAt = System.currentTimeMillis(),
        fileSize = 0L, // 実際のファイルサイズは取得困難なため、0またはDownloadから取得
        duration = null // ExoPlayerから取得可能
    )
    dao.insert(entity)
}
```

**ファイルサイズの取得**:
```kotlin
// Downloadからファイルサイズを取得する場合
val download = downloadIndex.getDownload(videoId)
val fileSize = download?.contentLength ?: 0L
```

---

## 🧪 テスト計画

### ユニットテスト

1. **VideoLocalDataSourceのテスト**
   - ダウンロード済み動画の保存・取得・削除
   - ダウンロード状態のチェック

2. **VideoDownloadManagerのテスト**
   - ダウンロードリクエストの作成
   - ダウンロード状態の監視

3. **ExoPlayerManagerのテスト**
   - オンライン/オフライン再生の切り替え
   - メディアアイテムの準備

### 統合テスト

1. **ダウンロードフローのテスト**
   - ダウンロード開始 → 進行 → 完了 → DB保存

2. **オフライン再生フローのテスト**
   - ダウンロード済み動画の検出（DownloadIndexまたはRoom DB）
   - 元のURLを使用した再生（ExoPlayerが自動的にキャッシュから読み込み）

### UIテスト

1. **VideoGuideScreenのテスト**
   - ダウンロード状態の表示
   - ダウンロードボタンの動作

2. **VideoPlayerActivityのテスト**
   - オンライン再生
   - オフライン再生
   - ダウンロード機能

---

## 📝 実装チェックリスト

### フェーズ1: ExoPlayer基本統合 ✅ 完了
- [x] ExoPlayer依存関係の追加
- [x] VideoPlayerActivityの実装
- [x] VideoPlayerViewModelの作成
- [x] VideoPlayerViewModelFactoryの作成
- [x] VideoPlayerModule/Componentの作成
- [x] AppComponentへの追加
- [x] AndroidManifest.xmlへの登録
- [x] レイアウトファイルの作成
- [x] VideoResponseDtoの作成（個別取得用）
- [x] VideoApiService.getVideo()の修正
- [x] DrupalVideoApiDataSource.getVideoById()の修正
- [x] オンライン再生の動作確認

### フェーズ2: Roomデータベース
- [ ] VideoDatabaseの作成
- [ ] DownloadedVideoEntityの定義
- [ ] DownloadedVideoDaoの実装
- [ ] VideoLocalDataSourceの作成
- [ ] VideoGuideRepositoryの拡張
- [ ] VideoGuideModuleの拡張
- [ ] DB操作の動作確認

### フェーズ3: ダウンロード機能
- [ ] DownloadManagerとSimpleCacheのセットアップ（VideoGuideModule/VideoPlayerModule）
- [ ] DownloadTrackerの作成
- [ ] VideoDownloadServiceの作成
- [ ] VideoDownloadManagerの作成
- [ ] AndroidManifest.xmlへのサービス登録
- [ ] VideoPlayerViewModelの拡張（ダウンロード機能）
- [ ] VideoGuideViewModelの拡張（ダウンロード機能）
- [ ] ダウンロード完了時のRoom DB保存処理
- [ ] ダウンロード機能の動作確認

### フェーズ4: オフライン再生
- [ ] ExoPlayerManagerの作成
- [ ] オフライン再生ロジックの実装
- [ ] VideoPlayerActivityの拡張
- [ ] オフライン再生の動作確認

### フェーズ5: UI改善
- [ ] VideoGuideScreenの拡張
- [ ] VideoItemCardの拡張
- [ ] VideoPlayerActivityのUI改善
- [ ] ダウンロード状態表示の動作確認

---

## 🚨 注意事項

### Media3 1.2.0に関する重要な注意事項

- **パッケージ名の変更**: ダウンロード関連のクラスは`androidx.media3.exoplayer.offline`パッケージにあります（`androidx.media3.exoplayer.download`ではない）
- **Cacheパッケージ**: Cache関連のクラスは`androidx.media3.datasource.cache`パッケージにあります（`androidx.media3.exoplayer.upstream.cache`ではない）
- **ファイルパス取得**: Media3では、ダウンロードされたファイルの実際のパスを取得する必要はありません。再生時には元のURLを使用し、ExoPlayerが自動的に`SimpleCache`から読み込みます
- **オフライン再生**: ダウンロード済みファイルの再生には`FileDataSource`を使用せず、元のURLを使用してください

### パーミッション

- **Android 10 (API 29)以降**: Scoped Storageの影響で、アプリ専用ディレクトリを使用する必要がある
- **Android 14 (API 34)以降**: `FOREGROUND_SERVICE_MEDIA_PLAYBACK`権限が必要

### ストレージ管理

- ダウンロードした動画は`SimpleCache`に保存され、キャッシュディレクトリ（`context.cacheDir/video_downloads`）に保存されます
- アプリのアンインストール時に自動削除される
- ストレージ容量の管理（最大容量の設定、古い動画の自動削除など）を検討
- `SimpleCache`のサイズ制限を設定することを推奨

### パフォーマンス

- ExoPlayerインスタンスは適切にリリースする（メモリリーク防止）
- 大量の動画がある場合のリスト表示の最適化（ページネーション）
- ダウンロード中のバッテリー消費に注意

### エラーハンドリング

- ネットワークエラー時の適切なエラーメッセージ表示
- ダウンロード失敗時のリトライ機能
- ストレージ容量不足時の処理

---

## 📚 参考資料

- [ExoPlayer公式ドキュメント](https://developer.android.com/guide/topics/media/exoplayer)
- [ExoPlayer Download機能](https://developer.android.com/guide/topics/media/exoplayer/downloading-media)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Android Foreground Services](https://developer.android.com/guide/components/foreground-services)

---

## 🔄 既存実装との統合

### VideoGuideModuleの拡張

既存の`VideoGuideModule`に以下を追加：

```kotlin
@Provides
@PerFragment
fun provideVideoDatabase(context: Context): VideoDatabase {
    return Room.databaseBuilder(
        context.applicationContext,
        VideoDatabase::class.java,
        "video_database"
    ).build()
}
```

### AppComponentの拡張

既存の`AppComponent`に以下を追加：

```kotlin
VideoPlayerComponent plus(VideoPlayerModule videoPlayerModule)
```

**注意**: `MainComponent`ではなく`AppComponent`に追加（`LoginActivity`と同様のパターン）

### VideoPlayerActivityの実装詳細

**実装済みの機能**:
- ExoPlayerインスタンスの作成とセットアップ
- ViewModelから動画情報を取得
- MediaItemの準備と再生
- ライフサイクル管理（onPause/onResume/onDestroy）
- 状態の保存と復元（onSaveInstanceState）
- エラーハンドリング（再生エラーの表示）

**実装パターン**:
- `app().appComponent().plus(VideoPlayerModule(...))`でコンポーネントを作成
- `LoginActivity`と同様のDIパターンを使用

### VideoGuideRepositoryの拡張

既存の`VideoGuideRepository`に`VideoLocalDataSource`を追加：

```kotlin
class VideoGuideRepository @Inject constructor(
    private val remoteDataSource: VideoRemoteDataSource,
    private val localDataSource: VideoLocalDataSource, // 追加
) {
    // 既存メソッド...
    
    suspend fun getDownloadedVideoList(): List<VideoItem> {
        return localDataSource.getAllDownloadedVideos()
    }
    
    suspend fun isVideoDownloaded(videoId: String): Boolean {
        return localDataSource.isDownloaded(videoId)
    }
}
```

---

## 🎯 実装の優先順位

1. **最優先**: フェーズ1（ExoPlayer基本統合）→ オンライン再生ができるようになる
2. **次**: フェーズ2（Room DB）→ ダウンロード状態の管理基盤
3. **その後**: フェーズ3（ダウンロード機能）→ 実際にダウンロードできるように
4. **最後**: フェーズ4（オフライン再生）→ ダウンロード済み動画の再生
5. **並行**: フェーズ5（UI改善）→ 各フェーズと並行して進める

---

## 📌 次のステップ

1. このドキュメントをレビュー
2. フェーズ1から順番に実装を開始
3. 各フェーズ完了時に動作確認とテスト
4. 必要に応じて設計の見直し

---

**作成日**: 2024年
**最終更新**: 2024年12月（フェーズ1完了）
