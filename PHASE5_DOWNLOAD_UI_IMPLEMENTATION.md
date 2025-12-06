# Phase5: ダウンロードUIと導線の実装計画

## 実装目標

動画のダウンロード機能をユーザーが操作できるように、UIと導線を実装する。
ダウンロード状態を視覚的に表示し、ユーザーが簡単にダウンロードを開始・管理できるようにする。

## 現状の問題点

### バックエンドは実装済み
- `VideoGuideViewModel.startDownload()`: ダウンロード開始メソッド ✅
- `VideoGuideViewModel.cancelDownload()`: ダウンロードキャンセルメソッド ✅
- `VideoGuideViewModel.downloadStates`: ダウンロード状態のLiveData ✅
- `VideoGuideViewModel.downloadProgress`: ダウンロード進捗のLiveData ✅
- `VideoPlayerViewModel.startDownload()`: 動画再生画面でのダウンロード開始メソッド ✅

### UIが未実装
- 動画一覧画面にダウンロードボタンがない
- ダウンロード状態の表示がない
- ダウンロード進捗の表示がない
- 動画をタップすると即座に再生画面に遷移（未ダウンロードでも遷移してしまう）

## 実装方針

1. **動画一覧画面**: 各動画カードにダウンロードボタンと状態表示を追加
2. **動画再生画面**: 未ダウンロード/ダウンロード中/ダウンロード済みの状態に応じたUIを表示
3. **導線の改善**: 未ダウンロード動画をタップした場合の適切な処理を実装

## 実装ファイル一覧

### 1. VideoGuideScreenの拡張

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/ui/VideoGuideScreen.kt`

**変更内容**:
- `VideoItemCard`にダウンロードボタンを追加
- ダウンロード状態の表示（未ダウンロード/ダウンロード中/ダウンロード済み）
- ダウンロード進捗バーの表示
- ダウンロード済みバッジの表示

**追加パラメータ**:
```kotlin
@Composable
fun VideoGuideScreen(
    viewModel: VideoGuideViewModel,
    onVideoClick: (String) -> Unit,
    onDownloadClick: (String) -> Unit, // 新規追加
    onCancelDownloadClick: (String) -> Unit, // 新規追加
)

@Composable
fun VideoItemCard(
    video: VideoItem,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit, // 新規追加
    onCancelDownloadClick: () -> Unit, // 新規追加
    downloadState: Download?, // 新規追加
    downloadProgress: Int, // 新規追加
    isDownloaded: Boolean, // 新規追加
)
```

**UI要素**:
- ダウンロードボタン（未ダウンロード時）
- ダウンロード中インジケーター（ダウンロード中）
- ダウンロード進捗バー（ダウンロード中）
- ダウンロード済みバッジ（ダウンロード済み）
- キャンセルボタン（ダウンロード中）

### 2. VideoGuideFragmentの拡張

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/VideoGuideFragment.kt`

**変更内容**:
- `VideoGuideScreen`に`onDownloadClick`と`onCancelDownloadClick`を渡す
- 動画をタップした場合の処理を改善（未ダウンロードの場合はエラーメッセージまたはダウンロード確認ダイアログ）

**変更前**:
```kotlin
VideoGuideScreen(
    viewModel = videoGuideViewModel,
    onVideoClick = { videoId ->
        navigateToVideoPlayer(videoId)
    }
)
```

**変更後**:
```kotlin
VideoGuideScreen(
    viewModel = videoGuideViewModel,
    onVideoClick = { videoId ->
        handleVideoClick(videoId)
    },
    onDownloadClick = { videoId ->
        videoGuideViewModel.startDownload(videoId)
    },
    onCancelDownloadClick = { videoId ->
        videoGuideViewModel.cancelDownload(videoId)
    }
)

private fun handleVideoClick(videoId: String) {
    // ダウンロード済みかチェック
    val isDownloaded = videoGuideViewModel.isVideoDownloaded(videoId)
    if (isDownloaded) {
        navigateToVideoPlayer(videoId)
    } else {
        // 未ダウンロードの場合はエラーメッセージを表示
        // またはダウンロード確認ダイアログを表示
        showDownloadRequiredDialog(videoId)
    }
}
```

### 3. VideoGuideViewModelの拡張

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/VideoGuideViewModel.kt`

**追加メソッド**:
- `isVideoDownloaded(videoId: String): Boolean`: 動画がダウンロード済みかチェック

**変更内容**:
- `downloadStates`と`downloadProgress`を既に公開しているが、UIで使用しやすいようにヘルパーメソッドを追加

**追加メソッド**:
```kotlin
/**
 * 動画がダウンロード済みかチェック
 */
fun isVideoDownloaded(videoId: String): Boolean {
    return repository.isVideoDownloaded(videoId)
}

/**
 * 特定の動画のダウンロード状態を取得
 */
fun getDownloadStateForVideo(videoId: String): Download? {
    return downloadStates.value?.get(videoId)
}

/**
 * 特定の動画のダウンロード進捗を取得
 */
fun getDownloadProgressForVideo(videoId: String): Int {
    return downloadProgress.value?.get(videoId) ?: 0
}
```

### 4. VideoPlayerActivityの拡張

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/video/VideoPlayerActivity.kt`

**変更内容**:
- レイアウトファイルにダウンロードボタンと進捗表示を追加
- ViewModelからダウンロード状態を観測
- 未ダウンロード/ダウンロード中/ダウンロード済みの状態に応じたUIを表示

**追加UI要素**:
- ダウンロードボタン（未ダウンロード時）
- ダウンロード進捗バー（ダウンロード中）
- キャンセルボタン（ダウンロード中）
- オフライン再生インジケーター（ダウンロード済み）

### 5. レイアウトファイルの拡張

**ファイル**: `app/src/main/res/layout/activity_video_player.xml`

**追加要素**:
- ダウンロードボタン（Button）
- ダウンロード進捗バー（ProgressBar）
- ダウンロード状態表示（TextView）
- キャンセルボタン（Button）

## 実装の詳細

### ダウンロード状態の判定

動画の状態は以下のように判定する：

1. **未ダウンロード**: 
   - `repository.isVideoDownloaded(videoId) == false`
   - `downloadStates.value?.get(videoId) == null`

2. **ダウンロード中**:
   - `downloadStates.value?.get(videoId)?.state == Download.STATE_DOWNLOADING`
   - または `Download.STATE_QUEUED`

3. **ダウンロード済み**:
   - `repository.isVideoDownloaded(videoId) == true`
   - `downloadStates.value?.get(videoId)?.state == Download.STATE_COMPLETED`

### UIの状態遷移

```
未ダウンロード
  ↓ [ダウンロードボタンクリック]
ダウンロード中
  ↓ [ダウンロード完了]
ダウンロード済み
  ↓ [動画タップ]
再生画面
```

### 動画一覧画面のUI要素

#### VideoItemCardのレイアウト

```
┌─────────────────────────────────┐
│ [サムネイル画像]                │
│                                 │
│ タイトル                        │
│ 説明                            │
│                                 │
│ [ダウンロードボタン] [状態表示] │
│ [進捗バー]                     │
└─────────────────────────────────┘
```

**状態別の表示**:

1. **未ダウンロード**:
   - ダウンロードボタン（「ダウンロード」）
   - 状態表示なし

2. **ダウンロード中**:
   - キャンセルボタン（「キャンセル」）
   - 進捗バー（XX%）
   - 状態表示（「ダウンロード中...」）

3. **ダウンロード済み**:
   - ダウンロード済みバッジ（「✓ ダウンロード済み」）
   - 状態表示なし

### 動画再生画面のUI要素

**状態別の表示**:

1. **未ダウンロード**:
   - エラーメッセージ（「この動画はダウンロードされていません」）
   - ダウンロードボタン（「ダウンロードして再生」）

2. **ダウンロード中**:
   - 進捗バー（XX%）
   - 状態表示（「ダウンロード中...」）
   - キャンセルボタン（「キャンセル」）

3. **ダウンロード済み**:
   - オフライン再生インジケーター（「オフライン再生可能」）
   - 動画プレイヤー

### 導線の改善

#### オプション1: エラーメッセージ表示（シンプル）

未ダウンロード動画をタップした場合：
- エラーメッセージを表示
- 「ダウンロード」ボタンを表示

**実装**:
```kotlin
private fun handleVideoClick(videoId: String) {
    viewModelScope.launch {
        val isDownloaded = repository.isVideoDownloaded(videoId)
        if (isDownloaded) {
            navigateToVideoPlayer(videoId)
        } else {
            // エラーメッセージを表示（VideoGuideScreenで処理）
            // または、VideoPlayerActivityに遷移してエラーメッセージを表示
            navigateToVideoPlayer(videoId) // VideoPlayerActivityでエラーを表示
        }
    }
}
```

#### オプション2: ダウンロード確認ダイアログ（推奨）

未ダウンロード動画をタップした場合：
- 確認ダイアログを表示
- 「ダウンロードしてから再生」または「キャンセル」

**実装**:
```kotlin
private fun handleVideoClick(videoId: String) {
    viewModelScope.launch {
        val isDownloaded = repository.isVideoDownloaded(videoId)
        if (isDownloaded) {
            navigateToVideoPlayer(videoId)
        } else {
            showDownloadConfirmationDialog(videoId)
        }
    }
}

private fun showDownloadConfirmationDialog(videoId: String) {
    AlertDialog.Builder(requireContext())
        .setTitle("動画をダウンロード")
        .setMessage("この動画をダウンロードしてから再生しますか？")
        .setPositiveButton("ダウンロード") { _, _ ->
            videoGuideViewModel.startDownload(videoId)
            // ダウンロード完了を待つか、進捗を表示
        }
        .setNegativeButton("キャンセル", null)
        .show()
}
```

#### オプション3: ダウンロード画面に遷移

未ダウンロード動画をタップした場合：
- ダウンロード画面に遷移
- ダウンロード完了後に自動的に再生画面に遷移

**実装**:
```kotlin
private fun handleVideoClick(videoId: String) {
    viewModelScope.launch {
        val isDownloaded = repository.isVideoDownloaded(videoId)
        if (isDownloaded) {
            navigateToVideoPlayer(videoId)
        } else {
            // ダウンロード画面に遷移（新規作成が必要）
            navigateToDownloadScreen(videoId)
        }
    }
}
```

**推奨**: オプション2（ダウンロード確認ダイアログ）が最もシンプルで実装しやすい。

## 実装の優先順位

1. `VideoGuideViewModel`の拡張（`isVideoDownloaded()`などのヘルパーメソッド追加）
2. `VideoGuideScreen`の拡張（ダウンロードボタンと状態表示の追加）
3. `VideoGuideFragment`の拡張（導線の改善）
4. `VideoPlayerActivity`の拡張（ダウンロードUIの追加）
5. レイアウトファイルの拡張
6. 動作確認とテスト

## テスト計画

### ユニットテスト

1. **VideoGuideViewModelのテスト**
   - `isVideoDownloaded()`のテスト
   - `getDownloadStateForVideo()`のテスト
   - `getDownloadProgressForVideo()`のテスト

2. **UIコンポーネントのテスト**
   - `VideoItemCard`の状態表示のテスト
   - ダウンロードボタンの動作テスト

### UIテスト

1. **動画一覧画面のテスト**
   - ダウンロードボタンの表示確認
   - ダウンロード開始の動作確認
   - ダウンロード進捗の表示確認
   - ダウンロード済みバッジの表示確認

2. **動画再生画面のテスト**
   - 未ダウンロード動画のエラーメッセージ表示確認
   - ダウンロードボタンの動作確認
   - ダウンロード進捗の表示確認

3. **導線のテスト**
   - 未ダウンロード動画をタップした場合の動作確認
   - ダウンロード済み動画をタップした場合の動作確認

## 注意事項

- ダウンロード状態は`LiveData`で監視するため、リアルタイムで更新される
- ダウンロード進捗は`0-100%`で表示する
- ダウンロード中の動画をタップした場合の動作を明確にする（進捗表示のみ、またはキャンセル可能）
- エラーメッセージは日本語で表示する（将来的に多言語対応が必要な場合は、リソースファイルに移動）
- ダウンロード完了時に自動的にUIを更新する（`downloadStates`の変更を監視）

## 将来の拡張

- ダウンロード一覧画面の作成（ダウンロード済み動画のみを表示）
- ダウンロード設定画面（保存先の変更、自動ダウンロードの設定など）
- バックグラウンドダウンロードの通知改善
- ダウンロード失敗時のリトライ機能
- 複数動画の一括ダウンロード機能

---

**作成日**: 2024年12月
**最終更新**: 2024年12月

