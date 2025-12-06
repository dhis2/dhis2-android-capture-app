---
name: Phase3実装修正計画
overview: Phase3の実装で発生した問題を修正し、正しいアーキテクチャパターンに従って再実装します。
todos:
  - id: singleton-simple-cache
    content: SimpleCacheをシングルトン化し、DownloadManagerとExoPlayerManagerで共有できるようにする
    status: completed
  - id: fix-exoplayer-manager
    content: ExoPlayerManagerを修正して、SimpleCacheを使用し、常に元のURLを使用するように変更（Media3の正しい使い方）
    status: completed
  - id: simplify-download-manager
    content: VideoDownloadManagerからobserveForeverと不要なメソッドを削除（setupDownloadCompletionListener, handleDownloadCompleted, getDownloadedFilePath）
    status: completed
  - id: add-download-completion-observer
    content: VideoGuideViewModelにdownloadStatesの監視を追加し、ダウンロード完了時にRoom DBに保存
    status: completed
  - id: add-download-completion-observer-player
    content: VideoPlayerViewModelにdownloadStatesの監視を追加し、ダウンロード完了時にRoom DBに保存。isOfflineModeとlocalFilePathを削除
    status: completed
  - id: fix-download-manager-init
    content: VideoGuideModuleでVideoDownloadService.setDownloadManager()を呼び出すことを確認
    status: completed
  - id: update-video-player-activity
    content: VideoPlayerActivityを修正して、isOfflineModeとlocalFilePathの参照を削除
    status: completed
  - id: test-download-flow
    content: ダウンロード開始→進行→完了→Room DB保存のフローをテスト。Media3のキャッシュからの自動読み込みを確認
    status: pending
---

## 修正方針

### 1. SimpleCacheのシングルトン化とExoPlayerとの共有

**問題**: `DownloadManager`と`ExoPlayer`で別々の`SimpleCache`インスタンスを使用している

**解決策**:

- `SimpleCache`をApplicationスコープまたはモジュールレベルでシングルトンとして管理
- `DownloadManager`と`ExoPlayerManager`の両方で同じ`SimpleCache`インスタンスを使用
- `ExoPlayerManager`の`initializePlayer()`で、`SimpleCache`を使用する`CacheDataSourceFactory`を設定

**実装**:

```kotlin
// VideoGuideModule.kt
@Provides
@PerFragment
fun provideSimpleCache(context: Context): SimpleCache {
    val databaseProvider = StandaloneDatabaseProvider(context)
    return SimpleCache(
        File(context.cacheDir, "video_downloads"),
        NoOpCacheEvictor(),
        databaseProvider
    )
}

// ExoPlayerManager.kt
class ExoPlayerManager(
    private val context: Context,
    private val cache: SimpleCache, // 追加
) {
    fun initializePlayer(): ExoPlayer {
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
        
        val dataSourceFactory = DefaultDataSourceFactory(
            context,
            cacheDataSourceFactory
        )
        
        exoPlayer = ExoPlayer.Builder(context)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(dataSourceFactory)
            )
            .build()
    }
}
```

### 2. ExoPlayerManagerの修正（Media3の正しい使い方）

**問題**: オフライン時に`FileDataSource`を使用しているが、Media3では元のURLを使用すべき

**解決策**:

- `prepareMediaItem()`から`isOffline`と`localFilePath`パラメータを削除
- 常に元のURLを使用して`MediaItem.fromUri(videoUrl)`を作成
- `SimpleCache`が自動的にキャッシュから読み込む

**実装**:

```kotlin
// ExoPlayerManager.kt
fun prepareMediaItem(videoUrl: String) {
    val player = exoPlayer ?: initializePlayer()
    // Media3では、常に元のURLを使用
    // SimpleCacheが自動的にキャッシュから読み込む
    val mediaItem = MediaItem.fromUri(videoUrl)
    player.setMediaItem(mediaItem)
    player.prepare()
}
```

### 3. DownloadManagerのシングルトン化

**問題**: `VideoGuideModule`と`VideoPlayerModule`の両方でDownloadManagerを初期化している

**解決策**:

- `VideoGuideModule`でDownloadManagerを`@PerFragment`スコープで提供（既存の実装を維持）
- `VideoPlayerModule`では、`VideoGuideModule`から提供されるDownloadManagerを参照するか、同じインスタンスを共有する仕組みを導入
- または、Applicationスコープで管理する

**実装**:

- `VideoGuideModule`でDownloadManagerを1つのインスタンスとして提供
- `VideoPlayerModule`では、既に設定済みのDownloadManagerを使用する（DIで注入）

### 4. observeForeverの削除とViewModelベースの監視

**問題**: `VideoDownloadManager.setupDownloadCompletionListener()`で`observeForever`を使用している

**解決策**:

- `VideoDownloadManager`から`observeForever`を削除
- `VideoGuideViewModel`と`VideoPlayerViewModel`で`downloadStates`を監視し、ダウンロード完了時にRoom DBに保存

**実装**:

- `VideoDownloadManager.setupDownloadCompletionListener()`を削除
- `VideoGuideViewModel`と`VideoPlayerViewModel`の`init`ブロックまたは適切な場所で、`downloadStates`を監視してダウンロード完了時に`repository.saveDownloadedVideo()`を呼び出す

### 5. ダウンロード完了時の処理の改善

**問題**: ダウンロード完了時にRoom DBに保存するロジックが不完全

**解決策**:

- ViewModelで`downloadStates`を監視
- ダウンロード完了時（`Download.STATE_COMPLETED`）に、`repository.getVideoById()`で動画情報を取得
- キャッシュディレクトリのパスを`File(context.cacheDir, "video_downloads").absolutePath`として保存

**実装**:

```kotlin
// VideoGuideViewModel.kt / VideoPlayerViewModel.kt
init {
    downloadStates.observeForever { downloads ->
        downloads.values.forEach { download ->
            if (download.state == Download.STATE_COMPLETED) {
                viewModelScope.launch {
                    val video = repository.getVideoById(download.request.id)
                    if (video != null) {
                        val cachePath = File(context.cacheDir, "video_downloads").absolutePath
                        repository.saveDownloadedVideo(video, cachePath)
                    }
                }
            }
        }
    }
}
```

**注意**: ViewModelの`onCleared()`で`observeForever`を削除する必要がある

### 6. VideoDownloadManagerの簡素化

**問題**: `VideoDownloadManager`に不要な処理が含まれている

**解決策**:

- `setupDownloadCompletionListener()`と`handleDownloadCompleted()`を削除
- `getDownloadedFilePath()`も削除（Media3では不要）
- `saveDownloadedVideoToDatabase()`は残す（ViewModelから呼び出し可能にするため）

### 7. DownloadManagerの初期化タイミング

**問題**: `VideoDownloadService.setDownloadManager()`の呼び出しタイミングが不適切

**解決策**:

- `VideoGuideModule`の`provideDownloadManager()`内で`VideoDownloadService.setDownloadManager()`を呼び出す
- `VideoPlayerModule`では、既に設定済みのDownloadManagerを使用する

### 8. VideoPlayerViewModelの修正

**問題**: `isOfflineMode`と`localFilePath`を使用しているが、Media3では不要

**解決策**:

- `isOfflineMode`と`localFilePath`を削除
- `checkOfflineAvailability()`と`getLocalFilePath()`を削除
- 常に元のURLを使用して再生（Media3が自動的にキャッシュから読み込む）

## 修正方針

### 1. DownloadManagerのシングルトン化

**問題**: `VideoGuideModule`と`VideoPlayerModule`の両方でDownloadManagerを初期化している

**解決策**:

- `AppComponent`またはApplicationスコープでDownloadManagerを管理
- または、`VideoGuideModule`で1つのインスタンスを作成し、`VideoPlayerModule`からも参照できるようにする

**実装**:

- `VideoGuideModule`でDownloadManagerを`@PerFragment`スコープで提供（既存の実装を維持）
- `VideoPlayerModule`では、`VideoGuideModule`から提供されるDownloadManagerを参照するか、同じインスタンスを共有する仕組みを導入

### 2. observeForeverの削除とViewModelベースの監視

**問題**: `VideoDownloadManager.setupDownloadCompletionListener()`で`observeForever`を使用している

**解決策**:

- `VideoDownloadManager`から`observeForever`を削除
- `VideoGuideViewModel`と`VideoPlayerViewModel`で`downloadStates`を監視し、ダウンロード完了時にRoom DBに保存

**実装**:

- `VideoDownloadManager.setupDownloadCompletionListener()`を削除
- `VideoGuideViewModel`と`VideoPlayerViewModel`の`init`ブロックまたは適切な場所で、`downloadStates`を監視してダウンロード完了時に`repository.saveDownloadedVideo()`を呼び出す

### 3. ダウンロード完了時の処理の改善

**問題**: ダウンロード完了時にRoom DBに保存するロジックが不完全

**解決策**:

- ViewModelで`downloadStates`を監視
- ダウンロード完了時（`Download.STATE_COMPLETED`）に、`repository.getVideoById()`で動画情報を取得
- キャッシュディレクトリのパスを`File(context.cacheDir, "video_downloads").absolutePath`として保存

**実装**:

```kotlin
// VideoGuideViewModel.kt / VideoPlayerViewModel.kt
init {
    downloadStates.observeForever { downloads ->
        downloads.values.forEach { download ->
            if (download.state == Download.STATE_COMPLETED) {
                viewModelScope.launch {
                    val video = repository.getVideoById(download.request.id)
                    if (video != null) {
                        val cachePath = File(context.cacheDir, "video_downloads").absolutePath
                        repository.saveDownloadedVideo(video, cachePath)
                    }
                }
            }
        }
    }
}
```

**注意**: ViewModelの`onCleared()`で`observeForever`を削除する必要がある

### 4. VideoDownloadManagerの簡素化

**問題**: `VideoDownloadManager`に不要な処理が含まれている

**解決策**:

- `setupDownloadCompletionListener()`と`handleDownloadCompleted()`を削除
- `getDownloadedFilePath()`も削除（Media3では不要）
- `saveDownloadedVideoToDatabase()`は残す（ViewModelから呼び出し可能にするため）

### 5. DownloadManagerの初期化タイミング

**問題**: `VideoDownloadService.setDownloadManager()`の呼び出しタイミングが不適切

**解決策**:

- `VideoGuideModule`の`provideDownloadManager()`内で`VideoDownloadService.setDownloadManager()`を呼び出す
- `VideoPlayerModule`では、既に設定済みのDownloadManagerを使用する

### 6. ExoPlayerManagerの統合確認

**問題**: `ExoPlayerManager`が既に実装されているが、Phase3の実装計画に含まれていない

**解決策**:

- `ExoPlayerManager`はPhase4で使用するため、Phase3では確認のみ
- `VideoPlayerModule`で`ExoPlayerManager`が提供されていることを確認

## 実装タスク

### タスク1: VideoDownloadManagerの簡素化

- `setupDownloadCompletionListener()`を削除
- `handleDownloadCompleted()`を削除
- `getDownloadedFilePath()`を削除（Media3では不要）
- `saveDownloadedVideoToDatabase()`は残す

### タスク3: ViewModelでのダウンロード完了監視

- `VideoGuideViewModel`に`downloadStates`の監視を追加
- `VideoPlayerViewModel`に`downloadStates`の監視を追加
- ダウンロード完了時に`repository.saveDownloadedVideo()`を呼び出す
- `onCleared()`で`observeForever`を削除

### タスク3: DownloadManagerの初期化確認

- `VideoGuideModule`で`VideoDownloadService.setDownloadManager()`を呼び出すことを確認
- `VideoPlayerModule`では既に設定済みのDownloadManagerを使用することを確認

### タスク4: ファイルパスの扱いの統一

- Room DBにはキャッシュディレクトリのパス（`File(context.cacheDir, "video_downloads").absolutePath`）を保存
- 実際のファイルパスは取得しない（Media3が自動的に処理）

### タスク5: テストと動作確認

- ダウンロード開始 → 進行 → 完了 → Room DB保存のフローを確認
- メモリリークがないことを確認（`observeForever`の適切な削除）

## 実装の詳細

### VideoGuideViewModelの修正

```kotlin
class VideoGuideViewModel(
    private val repository: VideoGuideRepository,
    private val downloadManager: VideoDownloadManager,
    private val context: Context, // 追加が必要
) : ViewModel() {
    
    private val downloadStatesObserver = Observer<Map<String, Download>> { downloads ->
        downloads.values.forEach { download ->
            if (download.state == Download.STATE_COMPLETED) {
                viewModelScope.launch {
                    val video = repository.getVideoById(download.request.id)
                    if (video != null) {
                        val cachePath = File(context.cacheDir, "video_downloads").absolutePath
                        repository.saveDownloadedVideo(video, cachePath)
                    }
                }
            }
        }
    }
    
    init {
        downloadStates.observeForever(downloadStatesObserver)
    }
    
    override fun onCleared() {
        super.onCleared()
        downloadStates.removeObserver(downloadStatesObserver)
    }
}
```

### VideoPlayerViewModelの修正

同様のパターンを適用

### VideoDownloadManagerの修正

不要なメソッドを削除し、シンプルな構造にする

## 注意事項

- `observeForever`を使用する場合は、必ず`removeObserver`で削除する
- Media3では、ダウンロード済みファイルの再生に元のURLを使用する（ExoPlayerが自動的にキャッシュから読み込む）
- Room DBにはキャッシュディレクトリのパスを保存するが、実際のファイルパスは取得しない