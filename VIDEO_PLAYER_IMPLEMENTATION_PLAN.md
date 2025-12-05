# VideoPlayer実装計画書 - ExoPlayer統合とオフライン再生機能

## 📋 最終目標

**Drupalから動画をダウンロードして、オフラインで動画を再生できるようにする**

### 達成すべき機能

1. ✅ **動画一覧の表示**（既に実装済み）
2. 🔄 **動画のオンライン再生**（ExoPlayer統合）
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
       └── VideoPlayerRepository
            ├── VideoGuideRepository (動画情報取得)
            ├── VideoDownloadManager (ダウンロード管理)
            └── ExoPlayerManager (再生管理)
```

---

## 📦 実装フェーズ

### フェーズ1: ExoPlayerの基本統合とオンライン再生

**目標**: ExoPlayerを使って動画をオンラインで再生できるようにする

#### 1.1 依存関係の追加

**ファイル**: `app/build.gradle.kts`

```kotlin
dependencies {
    // ExoPlayer Core
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-ui:1.2.0")
    implementation("androidx.media3:media3-common:1.2.0")
    
    // ExoPlayer Download (オフライン再生用)
    implementation("androidx.media3:media3-exoplayer-dash:1.2.0")
    implementation("androidx.media3:media3-datasource:1.2.0")
    implementation("androidx.media3:media3-datasource-okhttp:1.2.0")
    implementation("androidx.media3:media3-database:1.2.0")
    
    // Room Database (ダウンロード状態管理用)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
}
```

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
- `VideoGuideRepository`（既存のものを再利用）
- `VideoPlayerViewModelFactory`
- `ExoPlayer`インスタンス（オプション）

**DIパターン**: `@PerActivity`スコープを使用（既存の`LoginModule`パターンを参考）

#### 1.5 MainComponentへの追加

**ファイル**: `app/src/main/java/org/dhis2/usescases/main/MainComponent.kt`

```kotlin
fun plus(videoPlayerModule: VideoPlayerModule): VideoPlayerComponent
```

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
- ローディングインジケーター
- エラーメッセージ表示

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

#### 2.4 VideoGuideModuleの拡張

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/VideoGuideModule.kt`

**追加する`@Provides`メソッド**:
- `provideVideoDatabase(context: Context): VideoDatabase`
- `provideDownloadedVideoDao(database: VideoDatabase): DownloadedVideoDao`
- `provideVideoLocalDataSource(dao: DownloadedVideoDao): VideoLocalDataSource`

---

### フェーズ3: ExoPlayer DownloadServiceによるダウンロード機能

**目標**: ExoPlayerのDownloadServiceを使って動画をダウンロードする

#### 3.1 VideoDownloadManagerの作成

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/video/VideoDownloadManager.kt`

**責務**:
- ExoPlayerの`DownloadService`との連携
- ダウンロードリクエストの管理
- ダウンロード状態の監視
- ダウンロード完了時のコールバック処理

**主要メソッド**:
```kotlin
fun downloadVideo(videoItem: VideoItem)
fun cancelDownload(videoId: String)
fun getDownloadState(videoId: String): DownloadState
fun getAllDownloads(): List<Download>
```

#### 3.2 VideoDownloadServiceの作成

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/video/VideoDownloadService.kt`

**実装内容**:
- `DownloadService`を継承
- ダウンロードの実行と管理
- フォアグラウンドサービスとして動作

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

#### 3.3 DownloadTrackerの作成

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/video/DownloadTracker.kt`

**責務**:
- ダウンロード状態の監視
- `DownloadManager`からの状態更新を受け取る
- UIへの状態通知（LiveData/Flow）

#### 3.4 VideoPlayerViewModelの拡張

**追加プロパティ**:
- `downloadState: LiveData<DownloadState>`
- `downloadProgress: LiveData<Int>`（0-100%）

**追加メソッド**:
- `startDownload(videoId: String)`
- `cancelDownload(videoId: String)`
- `checkDownloadState(videoId: String)`

#### 3.5 VideoGuideViewModelの拡張

**追加プロパティ**:
- `downloadStates: Map<String, DownloadState>`（各動画のダウンロード状態）

**追加メソッド**:
- `checkDownloadState(videoId: String)`
- `getDownloadedVideos()`（ローカルDBから取得）

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

**処理フロー**:
1. 動画IDから動画情報を取得
2. ローカルDBでダウンロード済みかチェック
3. ダウンロード済みの場合:
   - ローカルファイルパスを取得
   - `FileDataSource`を使用してExoPlayerにセット
4. 未ダウンロードの場合:
   - `HttpDataSource`を使用してオンライン再生

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

#### DownloadRequestの作成

```kotlin
val downloadRequest = DownloadRequest.Builder(videoId, Uri.parse(videoUrl))
    .setMimeType(MimeTypes.VIDEO_MP4)
    .setData(videoItem.title.toByteArray())
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
val downloadManager = DownloadManager(
    context,
    databaseProvider,
    downloadCache,
    dataSourceFactory,
    executor
)

downloadManager.addListener(downloadTracker)
```

### Roomデータベースの実装

#### DatabaseProviderの作成

```kotlin
val databaseProvider = StandaloneDatabaseProvider(context)
```

#### ダウンロード情報の保存

```kotlin
suspend fun saveDownloadedVideo(videoItem: VideoItem, localFilePath: String) {
    val entity = DownloadedVideoEntity(
        videoId = videoItem.id,
        title = videoItem.title,
        description = videoItem.description,
        videoUrl = videoItem.videoUrl,
        thumbnailUrl = videoItem.thumbnailUrl,
        localFilePath = localFilePath,
        downloadedAt = System.currentTimeMillis(),
        fileSize = File(localFilePath).length(),
        duration = null // ExoPlayerから取得可能
    )
    dao.insert(entity)
}
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
   - ダウンロード済み動画の検出
   - ローカルファイルからの再生

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

### フェーズ1: ExoPlayer基本統合
- [ ] ExoPlayer依存関係の追加
- [ ] VideoPlayerActivityの実装
- [ ] VideoPlayerViewModelの作成
- [ ] VideoPlayerModule/Componentの作成
- [ ] MainComponentへの追加
- [ ] AndroidManifest.xmlへの登録
- [ ] レイアウトファイルの作成
- [ ] オンライン再生の動作確認

### フェーズ2: Roomデータベース
- [ ] VideoDatabaseの作成
- [ ] DownloadedVideoEntityの定義
- [ ] DownloadedVideoDaoの実装
- [ ] VideoLocalDataSourceの作成
- [ ] VideoGuideRepositoryの拡張
- [ ] VideoGuideModuleの拡張
- [ ] DB操作の動作確認

### フェーズ3: ダウンロード機能
- [ ] VideoDownloadManagerの作成
- [ ] VideoDownloadServiceの作成
- [ ] DownloadTrackerの作成
- [ ] AndroidManifest.xmlへのサービス登録
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

### パーミッション

- **Android 10 (API 29)以降**: Scoped Storageの影響で、アプリ専用ディレクトリを使用する必要がある
- **Android 14 (API 34)以降**: `FOREGROUND_SERVICE_MEDIA_PLAYBACK`権限が必要

### ストレージ管理

- ダウンロードした動画は`context.getExternalFilesDir()`または`context.filesDir`に保存
- アプリのアンインストール時に自動削除される
- ストレージ容量の管理（最大容量の設定、古い動画の自動削除など）を検討

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

### MainComponentの拡張

既存の`MainComponent`に以下を追加：

```kotlin
fun plus(videoPlayerModule: VideoPlayerModule): VideoPlayerComponent
```

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
**最終更新**: 2024年
