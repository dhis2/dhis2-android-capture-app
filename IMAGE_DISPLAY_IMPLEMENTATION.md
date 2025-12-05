# 画像表示実装計画書 - Coil統合とサムネイル表示

## 📋 目標

**動画一覧とプレーヤー画面にサムネイル画像を表示し、視覚的に魅力的なUIを実現する**

### 達成すべき機能

1. 🔄 **動画一覧のサムネイル表示**（VideoGuideScreen）
2. 🔄 **動画プレーヤーのサムネイル表示**（VideoPlayerActivity）
3. 🔄 **プレースホルダー画像の表示**（読み込み中・エラー時）
4. 🔄 **画像キャッシング**（メモリ＋ディスク）
5. 🔄 **ローディングインジケーター**
6. 🔄 **エラーハンドリング**

---

## 🎨 なぜCoilを選ぶか？

### Coilの特徴

| 特徴 | 説明 | メリット |
|------|------|---------|
| **Compose First** | Jetpack Compose専用設計 | VideoGuideScreenと完璧に統合 |
| **Kotlin Coroutines** | suspend関数ネイティブサポート | 既存の非同期処理と統一 |
| **軽量** | Glide/Picassoより小さい | APKサイズ削減 |
| **OkHttp使用** | 既存の依存関係を活用 | 重複なし |
| **自動キャッシング** | メモリ＋ディスク | パフォーマンス向上 |

### 他のライブラリとの比較

| ライブラリ | Compose対応 | Kotlin First | APKサイズ | 推奨度 |
|-----------|-------------|--------------|-----------|--------|
| **Coil** | ✅ | ✅ | 小 | ⭐⭐⭐⭐⭐ |
| Glide | ⚠️ (サポートあり) | ❌ | 大 | ⭐⭐⭐ |
| Picasso | ⚠️ (サポートあり) | ❌ | 中 | ⭐⭐ |

---

## 📦 実装フェーズ

### フェーズ1: Coilの基本統合

**目標**: Coilライブラリを追加し、基本的な画像表示を実現する

#### 1.1 依存関係の追加

**ファイル**: `app/build.gradle.kts`

```kotlin
dependencies {
    // 既存の依存関係...
    
    // Coil for Compose (画像読み込みライブラリ)
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // オプション: SVGサポートが必要な場合
    // implementation("io.coil-kt:coil-svg:2.5.0")
    
    // オプション: GIFサポートが必要な場合
    // implementation("io.coil-kt:coil-gif:2.5.0")
}
```

#### 1.2 必要なインポートの追加

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/ui/VideoGuideScreen.kt`

```kotlin
// Coilのインポート
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext

// Material Design 3
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

// レイアウト
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow

// アイコン
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Folder
```

---

### フェーズ2: VideoGuideScreenの改善

**目標**: 動画一覧にサムネイル画像を表示する

#### 2.1 VideoItemCardの完全な書き換え

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/ui/VideoGuideScreen.kt`

**現状のコード**:
```kotlin
@Composable
fun VideoItemCard(
    video: VideoItem,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Text(text = video.title, ...)
        Text(text = video.description, ...)
        Text(text = "Thumbnail: ${video.thumbnailUrl}", ...) // ← これを改善
        // ...
    }
}
```

**改善後のコード**:
```kotlin
@Composable
fun VideoItemCard(
    video: VideoItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            // サムネイル画像（左側）
            VideoThumbnail(
                thumbnailUrl = video.thumbnailUrl,
                title = video.title,
                modifier = Modifier
                    .size(width = 120.dp, height = 90.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 動画情報（右側）
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // タイトル
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // 説明
                if (video.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = video.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // メタ情報（タグ、カテゴリ、再生時間）
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (video.duration != null) {
                        MetadataChip(
                            label = video.duration,
                            icon = Icons.Default.PlayCircle
                        )
                    }
                    if (video.category != null) {
                        MetadataChip(
                            label = video.category,
                            icon = Icons.Default.Folder
                        )
                    }
                }
            }
        }
    }
}
```

#### 2.2 VideoThumbnailコンポーネントの作成

**同じファイル内に追加**:

```kotlin
/**
 * サムネイル画像を表示するコンポーネント
 * ローディング、エラー、プレースホルダーを自動処理
 */
@Composable
fun VideoThumbnail(
    thumbnailUrl: String?,
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (thumbnailUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    // ローディング中の表示
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                error = {
                    // エラー時の表示
                    ThumbnailPlaceholder(isError = true)
                }
            )
        } else {
            // サムネイルURLがnullの場合
            ThumbnailPlaceholder(isError = false)
        }
    }
}

/**
 * プレースホルダー表示
 */
@Composable
fun ThumbnailPlaceholder(
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isError) {
                Icons.Default.BrokenImage
            } else {
                Icons.Default.VideoLibrary
            },
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}
```

#### 2.3 MetadataChipコンポーネントの作成

```kotlin
/**
 * メタ情報を表示する小さなチップ
 */
@Composable
fun MetadataChip(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(24.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
```

#### 2.4 必要なアイコンのインポート

```kotlin
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.VideoLibrary
```

---

### フェーズ3: VideoPlayerActivityでのサムネイル表示

**目標**: 動画プレーヤー画面で、再生前にサムネイルを表示する

#### 3.1 レイアウトファイルの作成

**ファイル**: `app/src/main/res/layout/activity_video_player.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@android:color/black">

    <!-- サムネイル画像（再生前に表示） -->
    <ImageView
        android:id="@+id/thumbnail_image"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerCrop"
        android:contentDescription="@string/video_thumbnail" />

    <!-- ExoPlayer PlayerView -->
    <androidx.media3.ui.PlayerView
        android:id="@+id/player_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <!-- ローディングインジケーター -->
    <ProgressBar
        android:id="@+id/loading_indicator"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:visibility="gone" />

    <!-- エラーメッセージ -->
    <LinearLayout
        android:id="@+id/error_container"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:orientation="vertical"
        android:padding="16dp"
        android:visibility="gone">

        <TextView
            android:id="@+id/error_message"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:gravity="center"
            android:textColor="@android:color/white"
            android:textSize="16sp" />
    </LinearLayout>

</FrameLayout>
```

#### 3.2 VideoPlayerActivityでのCoil使用

**ファイル**: `app/src/main/java/org/dhis2/usescases/videoGuide/video/VideoPlayerActivity.kt`

```kotlin
package org.dhis2.usescases.videoGuide.video

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.load
import org.dhis2.R
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.videoGuide.domain.model.VideoItem
import timber.log.Timber

class VideoPlayerActivity : ActivityGlobalAbstract() {

    companion object {
        private const val EXTRA_VIDEO_ID = "EXTRA_VIDEO_ID"

        fun start(context: Context, videoId: String) {
            val intent = Intent(context, VideoPlayerActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_ID, videoId)
            }
            context.startActivity(intent)
        }
    }

    private var exoPlayer: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var thumbnailImage: ImageView
    private var videoItem: VideoItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        
        // Viewの取得
        playerView = findViewById(R.id.player_view)
        thumbnailImage = findViewById(R.id.thumbnail_image)
        
        val videoId = intent.getStringExtra(EXTRA_VIDEO_ID)
        if (videoId != null) {
            loadVideoData(videoId)
        } else {
            Timber.e("Video ID is null")
            finish()
        }
    }

    private fun loadVideoData(videoId: String) {
        // TODO: ViewModelから動画データを取得
        // 仮のデータで実装例を示す
        
        videoItem?.let { video ->
            // サムネイルを読み込む
            loadThumbnail(video.thumbnailUrl, video.title)
            
            // ExoPlayerをセットアップ
            setupExoPlayer(video.videoUrl)
        }
    }

    /**
     * サムネイル画像を読み込む
     */
    private fun loadThumbnail(thumbnailUrl: String?, title: String) {
        thumbnailImage.visibility = View.VISIBLE
        
        thumbnailImage.load(thumbnailUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_video_placeholder)
            error(R.drawable.ic_video_error)
            listener(
                onSuccess = { _, _ ->
                    Timber.d("Thumbnail loaded successfully")
                },
                onError = { _, result ->
                    Timber.e(result.throwable, "Failed to load thumbnail")
                }
            )
        }
    }

    /**
     * ExoPlayerのセットアップ
     */
    private fun setupExoPlayer(videoUrl: String) {
        // ExoPlayerインスタンスの作成
        exoPlayer = ExoPlayer.Builder(this).build().also { player ->
            playerView.player = player
            
            // 再生状態のリスナー
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_READY -> {
                            // 動画の準備完了 → サムネイルを非表示
                            thumbnailImage.visibility = View.GONE
                        }
                        Player.STATE_ENDED -> {
                            // 再生終了 → サムネイルを再表示（オプション）
                            thumbnailImage.visibility = View.VISIBLE
                        }
                    }
                }
            })
            
            // メディアアイテムの準備
            // val mediaItem = MediaItem.fromUri(videoUrl)
            // player.setMediaItem(mediaItem)
            // player.prepare()
            // player.playWhenReady = true
        }
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}
```

---

### フェーズ4: プレースホルダー画像の作成

**目標**: ローディング中やエラー時に表示する画像リソースを作成する

#### 4.1 Vector Drawableの作成

**ファイル**: `app/src/main/res/drawable/ic_video_placeholder.xml`

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="120dp"
    android:height="90dp"
    android:viewportWidth="120"
    android:viewportHeight="90">
    
    <!-- 背景 -->
    <path
        android:fillColor="#E0E0E0"
        android:pathData="M0,0h120v90h-120z"/>
    
    <!-- 再生アイコン -->
    <path
        android:fillColor="#9E9E9E"
        android:pathData="M45,30L75,45L45,60Z"/>
</vector>
```

**ファイル**: `app/src/main/res/drawable/ic_video_error.xml`

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="120dp"
    android:height="90dp"
    android:viewportWidth="120"
    android:viewportHeight="90">
    
    <!-- 背景 -->
    <path
        android:fillColor="#FFEBEE"
        android:pathData="M0,0h120v90h-120z"/>
    
    <!-- エラーアイコン（✕） -->
    <path
        android:fillColor="#D32F2F"
        android:pathData="M55,35L60,30L65,35L70,30L75,35L70,40L75,45L70,50L65,45L60,50L55,45L60,40Z"
        android:strokeWidth="2"
        android:strokeColor="#D32F2F"/>
</vector>
```

#### 4.2 strings.xmlへの追加

**ファイル**: `app/src/main/res/values/strings.xml`

```xml
<!-- 動画関連 -->
<string name="video_thumbnail">動画のサムネイル</string>
<string name="video_loading">動画を読み込んでいます...</string>
<string name="video_error">動画の読み込みに失敗しました</string>
```

---

### フェーズ5: パフォーマンス最適化

**目標**: 画像読み込みのパフォーマンスを最適化する

#### 5.1 Coilのグローバル設定（オプション）

**ファイル**: `app/src/main/java/org/dhis2/App.kt`（Application クラス）

```kotlin
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger

class App : MultiDexApplication(), HasAndroidInjector, ImageLoaderFactory {

    // 既存のコード...

    /**
     * Coilのグローバル設定
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // メモリの25%を使用
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50 * 1024 * 1024) // 50MB
                    .build()
            }
            .respectCacheHeaders(false) // キャッシュを優先
            .logger(if (BuildConfig.DEBUG) DebugLogger() else null) // デバッグログ
            .build()
    }
}
```

#### 5.2 LazyColumnでの最適化

**既に実装されているVideoGuideScreen**:

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
) {
    items(
        items = videoList,
        key = { video -> video.id } // ← 重要: 再コンポーズの最適化
    ) { video ->
        VideoItemCard(
            video = video,
            onClick = { onVideoClick(video.id) },
        )
    }
}
```

#### 5.3 メモリキャッシュキーの設定

**サムネイルコンポーネントの最適化**:

```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(thumbnailUrl)
        .memoryCacheKey(video.id) // 動画IDをキャッシュキーに使用
        .diskCacheKey(video.id)
        .crossfade(true)
        .build(),
    contentDescription = title,
    modifier = Modifier.fillMaxSize(),
    contentScale = ContentScale.Crop
)
```

---

## 🎨 UI/UXの改善アイデア

### 1. シマー効果（Skeleton Loading）

**オプション**: プレースホルダーをシマー効果にする

```kotlin
// 依存関係の追加（オプション）
implementation("com.valentinilk.shimmer:compose-shimmer:1.2.0")

@Composable
fun VideoThumbnail(
    thumbnailUrl: String?,
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .shimmer() // シマー効果
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // AsyncImage...
    }
}
```

### 2. サムネイルのアスペクト比固定

16:9のアスペクト比を維持：

```kotlin
VideoThumbnail(
    thumbnailUrl = video.thumbnailUrl,
    title = video.title,
    modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(16f / 9f) // アスペクト比を固定
        .clip(RoundedCornerShape(8.dp))
)
```

### 3. ダウンロード済みバッジの追加

```kotlin
Box {
    VideoThumbnail(...)
    
    // ダウンロード済みバッジ
    if (isDownloaded) {
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Downloaded",
                modifier = Modifier
                    .size(24.dp)
                    .padding(4.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
```

---

## 🧪 テスト計画

### 手動テスト

#### 動画一覧画面

1. **正常系**
   - [ ] サムネイル付き動画が正しく表示される
   - [ ] サムネイルなし動画がプレースホルダー表示される
   - [ ] スクロール時にスムーズに画像が読み込まれる
   - [ ] 画像をタップして動画プレーヤーに遷移できる

2. **異常系**
   - [ ] 無効なURL時にエラーアイコンが表示される
   - [ ] ネットワークエラー時に適切にハンドリングされる
   - [ ] 画像読み込み失敗時にプレースホルダーが表示される

3. **パフォーマンス**
   - [ ] 高速スクロール時にメモリリークがない
   - [ ] 画像キャッシュが機能している（2回目の表示が高速）
   - [ ] バックグラウンド → フォアグラウンド遷移が正常

#### 動画プレーヤー画面

1. **正常系**
   - [ ] 再生前にサムネイルが表示される
   - [ ] 再生開始時にサムネイルが非表示になる
   - [ ] 再生終了後の表示が適切

2. **異常系**
   - [ ] サムネイル読み込み失敗時の処理
   - [ ] 動画読み込み失敗時の処理

---

## 📝 実装チェックリスト

### フェーズ1: 基本統合
- [ ] Coil依存関係の追加
- [ ] 必要なインポートの追加
- [ ] ビルドエラーがないことを確認

### フェーズ2: VideoGuideScreen改善
- [ ] VideoItemCardの書き換え
- [ ] VideoThumbnailコンポーネントの作成
- [ ] ThumbnailPlaceholderコンポーネントの作成
- [ ] MetadataChipコンポーネントの作成
- [ ] アイコンのインポート追加
- [ ] 動作確認（サムネイル表示）

### フェーズ3: VideoPlayerActivity
- [ ] レイアウトファイルの作成
- [ ] VideoPlayerActivityの実装
- [ ] Coilでのサムネイル読み込み
- [ ] ExoPlayerとの連携
- [ ] 動作確認（プレーヤー画面）

### フェーズ4: プレースホルダー
- [ ] ic_video_placeholder.xmlの作成
- [ ] ic_video_error.xmlの作成
- [ ] strings.xmlへの追加
- [ ] 表示確認

### フェーズ5: パフォーマンス最適化
- [ ] Coilのグローバル設定（オプション）
- [ ] LazyColumnでのkey設定
- [ ] メモリキャッシュキーの設定
- [ ] パフォーマンステスト

---

## 🚨 注意事項

### 画像サイズとパフォーマンス

- **サムネイルの推奨サイズ**: 320x180px（16:9）または640x360px
- **メモリ使用量**: Coilは自動的に画像をリサイズしますが、大量の画像がある場合は注意
- **ディスクキャッシュ**: デフォルトで有効（最大50MB程度）

### ネットワーク

- **HTTPS**: DrupalサーバーがHTTPSを使用していることを確認
- **CORS**: 必要に応じてサーバー側で設定
- **リダイレクト**: Coilは自動的にリダイレクトに対応

### アクセシビリティ

- **contentDescription**: 必ず設定する（スクリーンリーダー対応）
- **コントラスト**: プレースホルダーの色は十分なコントラストを確保

---

## 📚 参考資料

- [Coil公式ドキュメント](https://coil-kt.github.io/coil/)
- [Compose Image Loading](https://coil-kt.github.io/coil/compose/)
- [Material Design - Imagery](https://m3.material.io/styles/imagery/overview)

---

## 🔄 既存実装との統合

### VideoGuideScreenの変更まとめ

```
変更前:
├── VideoGuideScreen (動画一覧)
└── VideoItemCard (シンプルなテキスト表示)

変更後:
├── VideoGuideScreen (動画一覧)
├── VideoItemCard (Cardデザイン + サムネイル)
├── VideoThumbnail (サムネイル表示コンポーネント)
├── ThumbnailPlaceholder (プレースホルダー)
└── MetadataChip (メタ情報チップ)
```

### VideoPlayerActivityの変更まとめ

```
変更前:
└── VideoPlayerActivity (空のスケルトン)

変更後:
├── VideoPlayerActivity (ExoPlayer + サムネイル統合)
└── activity_video_player.xml (レイアウト)
```

---

## 🎯 実装の優先順位

1. **最優先**: フェーズ1（Coil統合）+ フェーズ2（VideoGuideScreen改善）
   - ユーザーに最も見える部分
   - 動画一覧の見た目が劇的に改善

2. **次**: フェーズ4（プレースホルダー）
   - エラーハンドリングの改善
   - UX向上

3. **その後**: フェーズ3（VideoPlayerActivity）
   - ExoPlayer実装と並行して進める

4. **最後**: フェーズ5（パフォーマンス最適化）
   - 動画数が増えた場合に実施

---

## 📌 次のステップ

1. Coil依存関係の追加
2. VideoGuideScreenの段階的な書き換え
3. 動作確認とUI調整
4. プレースホルダー画像の追加
5. VideoPlayerActivityへの統合（ExoPlayer実装時）

---

**作成日**: 2024年12月5日  
**関連ドキュメント**: 
- [VIDEO_PLAYER_IMPLEMENTATION_PLAN.md](./VIDEO_PLAYER_IMPLEMENTATION_PLAN.md)
- [VIDEO_GUIDE_IMPLEMENTATION.md](./VIDEO_GUIDE_IMPLEMENTATION.md)
- [VIDEO_METADATA_IMPLEMENTATION.md](./VIDEO_METADATA_IMPLEMENTATION.md)

