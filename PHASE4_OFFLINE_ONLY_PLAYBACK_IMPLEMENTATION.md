# Phase4 オフライン専用再生機能の実装計画

## 実装目標

動画再生を**ダウンロード済み動画のみ**に制限し、オンライン再生を無効化する。
必ずダウンロードしてから再生されるようにする。

## 現状の問題点

現在の実装では、以下の箇所でオンライン再生が可能になっている：

1. **`VideoGuideRepository.getVideoById()`**: ローカルDBにない場合、リモートから取得する
2. **`ExoPlayerManager.prepareMediaItem()`**: ファイルが見つからない場合や`isOffline`が`false`の場合、オンライン再生にフォールバック
3. **`VideoPlayerActivity.playVideo()`**: `isOffline`が`false`でも再生を試みる

これにより、ネットワーク状態によってオンライン/オフライン再生が切り替わってしまう。

## 実装方針

ダウンロード済み動画のみを再生対象とし、未ダウンロードの場合はエラーメッセージを表示する。

## 実装ファイル一覧

### 1. VideoGuideRepositoryの変更

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/VideoGuideRepository.kt`

**変更内容**:
- `getVideoById()`メソッドを変更し、ローカルDBからしか取得しないようにする
- リモートからの取得を削除

**変更前**:
```kotlin
suspend fun getVideoById(videoId: String): VideoItem? {
    // まずローカルDBから取得を試みる
    val localVideo = localDataSource.getDownloadedVideoById(videoId)
    if (localVideo != null) {
        return localVideo
    }
    // ローカルにない場合はリモートから取得
    return dataSource.getVideoById(videoId)
}
```

**変更後**:
```kotlin
suspend fun getVideoById(videoId: String): VideoItem? {
    // ローカルDBからしか取得しない（ダウンロード済みのみ）
    return localDataSource.getDownloadedVideoById(videoId)
}
```

**注意**: この変更により、`getVideoById()`はダウンロード済み動画のみを返すようになる。動画一覧の取得など、他の用途でリモートから取得する必要がある場合は、`dataSource.getVideoById()`を直接使用する。

### 2. VideoPlayerViewModelの変更

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/video/VideoPlayerViewModel.kt`

**変更内容**:
- `loadVideo()`メソッドを変更し、`getDownloadedVideoById()`を使用するようにする
- ダウンロード済みでない場合はエラーメッセージを表示

**変更前**:
```kotlin
fun loadVideo(videoId: String) {
    viewModelScope.launch {
        _isLoading.postValue(true)
        _errorMessage.postValue(null)
        try {
            // getVideoById()は既にローカルDBからも取得を試みるように拡張済み
            val video = repository.getVideoById(videoId)
            
            if (video != null) {
                _videoItem.postValue(video)
                // 動画情報を取得したら、オフライン再生可能かチェック
                checkOfflineAvailability(videoId)
                // ダウンロード状態も確認
                checkDownloadState()
            } else {
                _errorMessage.postValue("Video not found")
            }
        } catch (e: Exception) {
            _errorMessage.postValue("Failed to load video: ${e.message}")
        } finally {
            _isLoading.postValue(false)
        }
    }
}
```

**変更後**:
```kotlin
fun loadVideo(videoId: String) {
    viewModelScope.launch {
        _isLoading.postValue(true)
        _errorMessage.postValue(null)
        try {
            // ローカルDBからしか取得しない（ダウンロード済みのみ）
            val video = repository.getDownloadedVideoById(videoId)
            
            if (video != null) {
                _videoItem.postValue(video)
                // オフライン再生可能かチェック
                checkOfflineAvailability(videoId)
                // ダウンロード状態も確認
                checkDownloadState()
            } else {
                _errorMessage.postValue("この動画はダウンロードされていません。先にダウンロードしてください。")
            }
        } catch (e: Exception) {
            _errorMessage.postValue("動画の読み込みに失敗しました: ${e.message}")
        } finally {
            _isLoading.postValue(false)
        }
    }
}
```

### 3. ExoPlayerManagerの変更

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/video/ExoPlayerManager.kt`

**変更内容**:
- `prepareMediaItem()`メソッドを変更し、オフライン再生のみをサポートする
- ローカルファイルが見つからない場合は例外を投げる
- オンライン再生のフォールバック処理を削除

**変更前**:
```kotlin
fun prepareMediaItem(
    videoUrl: String,
    isOffline: Boolean,
    localFilePath: String? = null
) {
    val player = exoPlayer ?: initializePlayer()

    val mediaItem = if (isOffline && localFilePath != null) {
        // オフライン再生: ローカルファイルから再生
        val file = File(localFilePath)
        if (file.exists()) {
            Timber.d("Preparing offline media item from: $localFilePath")
            MediaItem.fromUri(Uri.fromFile(file))
        } else {
            Timber.w("Local file not found: $localFilePath, falling back to online")
            // ファイルが見つからない場合はオンライン再生にフォールバック
            MediaItem.fromUri(videoUrl)
        }
    } else {
        // オンライン再生: URLから再生
        Timber.d("Preparing online media item from: $videoUrl")
        MediaItem.fromUri(videoUrl)
    }

    player.setMediaItem(mediaItem)
    player.prepare()
}
```

**変更後**:
```kotlin
fun prepareMediaItem(
    videoUrl: String,
    isOffline: Boolean,
    localFilePath: String? = null
) {
    val player = exoPlayer ?: initializePlayer()

    // オフライン再生のみをサポート
    if (!isOffline || localFilePath == null) {
        throw IllegalStateException("動画はダウンロード済みのもののみ再生できます")
    }

    val file = File(localFilePath)
    if (!file.exists()) {
        throw IllegalStateException("ローカルファイルが見つかりません: $localFilePath")
    }

    Timber.d("Preparing offline media item from: $localFilePath")
    val mediaItem = MediaItem.fromUri(Uri.fromFile(file))

    player.setMediaItem(mediaItem)
    player.prepare()
}
```

**注意**: `videoUrl`パラメータは将来の拡張のために残すが、現在は使用しない。

### 4. VideoPlayerActivityの変更

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/video/VideoPlayerActivity.kt`

**変更内容**:
- `updateVideoPlayback()`メソッドを変更し、ダウンロード済みでない場合は再生しない
- `playVideo()`メソッドにエラーハンドリングを追加

**変更前**:
```kotlin
private fun updateVideoPlayback() {
    val videoItem = viewModel.videoItem.value ?: return
    val isOffline = viewModel.isOfflineMode.value ?: false
    val localFilePath = viewModel.localFilePath.value
    playVideo(videoItem.videoUrl, isOffline, localFilePath)
}

private fun playVideo(videoUrl: String, isOffline: Boolean, localFilePath: String?) {
    exoPlayerManager.prepareMediaItem(videoUrl, isOffline, localFilePath)
    exoPlayerManager.getPlayer()?.let { player ->
        player.playWhenReady = true
    }
}
```

**変更後**:
```kotlin
private fun updateVideoPlayback() {
    val videoItem = viewModel.videoItem.value ?: return
    val isOffline = viewModel.isOfflineMode.value ?: false
    val localFilePath = viewModel.localFilePath.value
    
    // ダウンロード済みでない場合は再生しない
    if (!isOffline || localFilePath == null) {
        showError("この動画はダウンロードされていません。先にダウンロードしてください。")
        return
    }
    
    playVideo(videoItem.videoUrl, isOffline, localFilePath)
}

private fun playVideo(videoUrl: String, isOffline: Boolean, localFilePath: String?) {
    try {
        exoPlayerManager.prepareMediaItem(videoUrl, isOffline, localFilePath)
        exoPlayerManager.getPlayer()?.let { player ->
            player.playWhenReady = true
        }
    } catch (e: Exception) {
        showError("再生エラー: ${e.message}")
    }
}
```

## 実装の詳細

### エラーメッセージ

以下のエラーメッセージを表示する：

1. **動画がダウンロードされていない場合**:
   - "この動画はダウンロードされていません。先にダウンロードしてください。"

2. **ローカルファイルが見つからない場合**:
   - "ローカルファイルが見つかりません: [ファイルパス]"

3. **その他の再生エラー**:
   - "再生エラー: [エラーメッセージ]"

### 動作フロー

1. ユーザーが動画をタップ
2. `VideoPlayerActivity`が起動
3. `VideoPlayerViewModel.loadVideo()`が呼ばれる
4. `VideoGuideRepository.getDownloadedVideoById()`でローカルDBから取得を試みる
5. ダウンロード済みの場合:
   - 動画情報を取得
   - `checkOfflineAvailability()`でファイルの存在確認
   - ファイルが存在する場合のみ再生開始
6. 未ダウンロードの場合:
   - エラーメッセージを表示
   - 再生を開始しない

### 影響範囲

#### 変更の影響を受ける機能

- **動画再生**: ダウンロード済み動画のみ再生可能
- **動画一覧**: 影響なし（`getVideoList()`は変更しない）

#### 影響を受けない機能

- **動画一覧の取得**: `getVideoList()`はリモートから取得するため影響なし
- **ダウンロード機能**: 影響なし
- **その他の機能**: 影響なし

## 実装の優先順位

1. `VideoGuideRepository.getVideoById()`の変更
2. `VideoPlayerViewModel.loadVideo()`の変更
3. `ExoPlayerManager.prepareMediaItem()`の変更
4. `VideoPlayerActivity.updateVideoPlayback()`と`playVideo()`の変更
5. 動作確認とテスト

## テスト計画

### ユニットテスト

1. **VideoGuideRepositoryのテスト**
   - `getVideoById()`がローカルDBからしか取得しないことを確認
   - 未ダウンロードの動画IDを渡した場合、`null`を返すことを確認

2. **ExoPlayerManagerのテスト**
   - `prepareMediaItem()`で`isOffline`が`false`の場合、例外を投げることを確認
   - `localFilePath`が`null`の場合、例外を投げることを確認
   - ファイルが存在しない場合、例外を投げることを確認

### 統合テスト

1. **ダウンロード済み動画の再生**
   - ダウンロード済み動画を再生できることを確認
   - エラーメッセージが表示されないことを確認

2. **未ダウンロード動画の再生**
   - 未ダウンロード動画をタップした場合、エラーメッセージが表示されることを確認
   - 再生が開始されないことを確認

3. **ファイルが存在しない場合**
   - DBには記録があるがファイルが存在しない場合、エラーメッセージが表示されることを確認
   - 再生が開始されないことを確認

## 注意事項

- `VideoGuideRepository.getVideoById()`の変更により、動画一覧から動画をタップした場合でも、ダウンロード済みでないと再生できない
- 動画一覧画面で、ダウンロード済みかどうかを表示するUIが必要になる可能性がある
- エラーメッセージは日本語で表示する（将来的に多言語対応が必要な場合は、リソースファイルに移動）

## 将来の拡張

- 動画一覧画面に「ダウンロード済み」バッジを表示
- 未ダウンロード動画をタップした場合、ダウンロード画面に遷移するオプション
- ダウンロード中の動画をタップした場合、ダウンロード進捗を表示

---

**作成日**: 2024年12月
**最終更新**: 2024年12月

