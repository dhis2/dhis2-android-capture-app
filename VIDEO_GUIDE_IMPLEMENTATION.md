# VideoGuideモジュール実装ドキュメント

## 概要

VideoGuideモジュールは、DHIS2 Android Captureアプリに動画チュートリアル機能を追加するための新しいモジュールです。既存のTutorialsメニューをVideoGuideに変更し、WebViewActivityへの遷移からFragmentベースの実装に置き換えました。

本モジュールは、Drupal JSON:APIから動画データを取得し、動画一覧を表示する機能を提供します。DataSourceパターンを使用しており、開発用のDummyDataSourceと本番用のDrupalVideoApiDataSourceを切り替え可能な設計になっています。

## ディレクトリ構造

```
app/src/main/java/org/dhis2/usescases/videoGuide/
├── VideoGuideFragment.kt              # 動画一覧を表示するフラグメント
├── VideoGuideModule.kt                # Dagger DIモジュール（依存関係の提供）
├── VideoGuideComponent.kt             # Dagger DIコンポーネント（注入先の定義）
├── VideoGuideRepository.kt           # データアクセス層（動画データの取得）
├── VideoGuideViewModel.kt            # UI状態管理（ViewModel）
├── VideoGuideViewModelFactory.kt     # ViewModelのファクトリ
├── data/                              # データ層
│   ├── api/
│   │   └── VideoApiService.kt        # Retrofit APIインターフェース
│   ├── datasource/
│   │   ├── VideoRemoteDataSource.kt # DataSourceインターフェース
│   │   ├── DrupalVideoApiDataSource.kt # Drupal API実装
│   │   └── DummyVideoDataSource.kt  # 開発用ダミーデータソース
│   ├── dto/
│   │   ├── VideoListResponseDto.kt   # APIレスポンスDTO
│   │   ├── VideoMediaDto.kt          # メディアエンティティDTO
│   │   └── VideoFileDto.kt           # ファイルエンティティDTO
│   └── mapper/
│       └── VideoMapper.kt            # DTO → Domain Model変換
├── domain/
│   └── model/
│       └── VideoItem.kt              # 動画アイテムのドメインモデル
├── ui/                                # UI関連ファイル
│   └── VideoGuideScreen.kt          # Compose画面（動画一覧UI）
└── video/                             # 動画再生関連
    └── VideoPlayerActivity.kt       # 動画再生用のActivity
```

## アーキテクチャ

本モジュールは、既存のコードベースで使用されている**MVVM（Model-View-ViewModel）パターン**と**クリーンアーキテクチャ**の原則に従って実装されています。さらに、**DataSourceパターン**を採用し、データ取得の抽象化を実現しています。

### レイヤー構造

```
┌─────────────────────────────────────┐
│         Presentation Layer           │
│  (Fragment, Compose UI, Activity)  │
├─────────────────────────────────────┤
│         ViewModel Layer             │
│    (ViewModel, ViewModelFactory)    │
├─────────────────────────────────────┤
│          Domain Layer               │
│         (Repository)                │
├─────────────────────────────────────┤
│          Data Layer                 │
│  ┌──────────────────────────────┐ │
│  │      DataSource Layer         │ │
│  │  (VideoRemoteDataSource)      │ │
│  └──────────────────────────────┘ │
│  ┌──────────────────────────────┐ │
│  │      API Layer               │ │
│  │  (Retrofit, VideoApiService) │ │
│  └──────────────────────────────┘ │
│  ┌──────────────────────────────┐ │
│  │      DTO Layer                │ │
│  │  (VideoListResponseDto, etc.) │ │
│  └──────────────────────────────┘ │
│  ┌──────────────────────────────┐ │
│  │      Mapper Layer            │ │
│  │  (VideoMapper)               │ │
│  └──────────────────────────────┘ │
└─────────────────────────────────────┘
```

## 各ファイルの責務

### 1. Presentation Layer（プレゼンテーション層）

#### VideoGuideFragment.kt
**責務**: UIのライフサイクル管理とDIの設定

- `FragmentGlobalAbstract`を継承し、既存のフラグメントパターンに従う
- `MainActivity`の`mainComponent`から`VideoGuideComponent`を取得してDIを実行
- `ComposeView`を使用して`VideoGuideScreen`を表示
- 動画アイテムクリック時に`VideoPlayerActivity`へ遷移

**主要なメソッド**:
- `onAttach()`: DIの設定
- `onCreateView()`: Compose UIの設定
- `navigateToVideoPlayer()`: 動画再生画面への遷移

#### VideoGuideScreen.kt
**責務**: 動画一覧のUI実装

- Jetpack Composeを使用したUI実装
- `VideoGuideViewModel`から動画一覧とローディング状態を取得
- `LazyColumn`で動画一覧を表示
- 各動画アイテムは`VideoItemCard`コンポーネントで表示

**主要なコンポーネント**:
- `VideoGuideScreen`: メイン画面コンポーネント
- `VideoItemCard`: 動画アイテムのカードコンポーネント

#### VideoPlayerActivity.kt
**責務**: 動画再生画面の管理

- `ActivityGlobalAbstract`を継承
- Intentから`videoId`を受け取り、動画を再生
- 初期実装では基本的な構造のみ（動画再生機能は後で実装予定）

**主要なメソッド**:
- `start()`: Activity起動用のcompanion objectメソッド

### 2. ViewModel Layer（ビューモデル層）

#### VideoGuideViewModel.kt
**責務**: UI状態の管理とビジネスロジックの実行

- `LiveData`を使用してUI状態を管理
- `VideoGuideRepository`から動画データを取得
- コルーチンを使用した非同期処理（suspend関数）

**主要なプロパティ**:
- `videoList: LiveData<List<VideoItem>>`: 動画一覧
- `isLoading: LiveData<Boolean>`: ローディング状態

**主要なメソッド**:
- `loadVideos()`: 動画一覧の読み込み（suspend関数を呼び出し）

#### VideoGuideViewModelFactory.kt
**責務**: ViewModelのインスタンス生成

- `ViewModelProvider.Factory`を実装
- `VideoGuideRepository`を依存関係として受け取り、`VideoGuideViewModel`を生成

### 3. Domain Layer（ドメイン層）

#### VideoGuideRepository.kt
**責務**: データアクセスの抽象化

- `VideoRemoteDataSource`を依存関係として受け取る
- DataSourceパターンにより、データ取得の実装詳細を隠蔽
- `suspend`関数を使用して非同期処理を実現

**主要なメソッド**:
- `getVideoList(): List<VideoItem>`: 動画一覧の取得（suspend関数）
- `getVideoById(videoId: String): VideoItem?`: IDから動画情報を取得（suspend関数）

#### VideoItem.kt（Domain Model）
**責務**: 動画情報のドメインモデル定義

```kotlin
data class VideoItem(
    val id: String,              // 動画ID
    val title: String,           // タイトル
    val description: String,      // 説明
    val thumbnailUrl: String?,   // サムネイル画像URL
    val videoUrl: String,        // 動画URL
    val duration: String? = null // 再生時間
)
```

### 4. Data Layer（データ層）

#### DataSource Layer（データソース層）

##### VideoRemoteDataSource.kt（インターフェース）
**責務**: データソースの抽象化インターフェース

- `getVideoList()`: 動画一覧の取得
- `getVideoById()`: IDから動画情報を取得
- 実装クラス（DrupalVideoApiDataSource、DummyVideoDataSource）がこのインターフェースを実装

##### DrupalVideoApiDataSource.kt
**責務**: Drupal JSON:APIからの動画データ取得

- `VideoApiService`を使用してAPI呼び出し
- `VideoMapper`でDTO → Domain Model変換
- `runCatching`でエラーハンドリング
- `Timber.e()`でエラーログを出力
- `included`配列からファイルマップを作成し、`relationships`でファイルURLを解決

**主要な処理**:
- APIレスポンスの`included`配列からファイルID → URLのマップを作成
- `relationships.field_media_video_file`からファイルIDを取得
- MapperでDomain Modelに変換

##### DummyVideoDataSource.kt
**責務**: 開発・テスト用のダミーデータ提供

- ハードコードされたダミーデータを返す
- UI開発やテスト時に使用
- `VideoGuideModule`で簡単に切り替え可能

#### API Layer（API層）

##### VideoApiService.kt
**責務**: Retrofit APIインターフェース定義

- Drupal JSON:APIのエンドポイントを定義
- `@GET`アノテーションでエンドポイントを指定
- `include`パラメータでファイル情報を含める

**主要なメソッド**:
- `getVideos()`: 動画一覧取得（`jsonapi/media/video`）
- `getVideo(id)`: 単一動画取得（`jsonapi/media/video/{id}`）

#### DTO Layer（DTO層）

##### VideoListResponseDto.kt
**責務**: APIレスポンスのDTO定義

- `data`: メディア情報の配列
- `included`: ファイル情報の配列（オプショナル）

##### VideoMediaDto.kt
**責務**: メディアエンティティのDTO定義

- `id`: メディアID
- `type`: エンティティタイプ
- `attributes`: メディア属性（name, description）
- `relationships`: ファイルへの参照（`field_media_video_file`）

**重要**: `@Json(name = "field_media_video_file")`でsnake_caseのJSONキーをマッピング

##### VideoFileDto.kt
**責務**: ファイルエンティティのDTO定義

- `id`: ファイルID
- `type`: エンティティタイプ（`file--file`）
- `attributes.uri.url`: ファイルURL

#### Mapper Layer（マッパー層）

##### VideoMapper.kt
**責務**: DTO → Domain Model変換

- **純粋関数**として実装（try/catchなし）
- `createFilesMap()`: `included`配列からファイルID → URLのマップを作成
- `mapToDomain()`: DTOをDomain Modelに変換
  - `relationships`がnullの場合はnullを返してスキップ
  - ファイルが見つからない場合はnullを返してスキップ
  - ベースURLと相対パスを結合して完全なURLを生成

**設計の特徴**:
- エラーハンドリングはDataSource層で行う
- 変換ロジックのみに集中
- null安全性を考慮

### 5. Dependency Injection（依存性注入）

#### VideoGuideModule.kt
**責務**: 依存関係の提供

- `@Module`アノテーションでDaggerモジュールとして定義
- `@Provides`メソッドで各種依存関係を提供
- `@PerFragment`スコープでフラグメントのライフサイクルに紐づく

**提供する依存関係**:
- `VideoApiService`: Retrofit APIサービス（Moshi Converter使用）
- `VideoMapper`: DTO → Domain Model変換マッパー
- `OkHttpClient`: HTTPクライアント
- `VideoRemoteDataSource`: データソース（Dummy/Drupal切り替え可能）
- `VideoGuideRepository`: データアクセス層
- `VideoGuideViewModelFactory`: ViewModelのファクトリ

**Moshi Singleton化**:
- `companion object`でMoshiインスタンスをSingleton化
- パフォーマンスとメモリ効率を向上

**BuildConfig使用**:
- `BuildConfig.DRUPAL_BASE_URL`でベースURLを取得
- `build.gradle.kts`で設定

**DataSource切り替え**:
```kotlin
fun provideDataSource(...): VideoRemoteDataSource {
    // 開発時は以下に切り替え可能
    // return DummyVideoDataSource()
    return DrupalVideoApiDataSource(api, mapper, baseUrl)
}
```

#### VideoGuideComponent.kt
**責務**: 注入先の定義

- `@Subcomponent`で`MainComponent`のサブコンポーネントとして定義
- `@PerFragment`スコープでフラグメント単位のインスタンス管理
- `inject(fragment: VideoGuideFragment)`メソッドでFragmentへの注入を定義

## データフロー

```
1. ユーザーがメニューから「Video Guide」を選択
   ↓
2. MainActivity → MainNavigator.openVideoGuide()
   ↓
3. MainNavigator → VideoGuideFragment()を生成して表示
   ↓
4. VideoGuideFragment.onAttach()
   → MainActivity.mainComponent.plus(VideoGuideModule()).inject(this)
   ↓
5. VideoGuideViewModelが初期化され、loadVideos()が実行される
   ↓
6. VideoGuideRepository.getVideoList()が呼ばれる（suspend関数）
   ↓
7. VideoRemoteDataSource.getVideoList()が呼ばれる
   ↓
8. DrupalVideoApiDataSource.getVideoList()が実行される
   ↓
9. VideoApiService.getVideos()でAPI呼び出し
   ↓
10. VideoListResponseDtoが返される
   ↓
11. VideoMapper.createFilesMap()でファイルマップを作成
   ↓
12. VideoMapper.mapToDomain()でDTO → Domain Model変換
   ↓
13. データが取得されると、ViewModelのvideoListが更新される
   ↓
14. VideoGuideScreenがLiveDataを監視し、UIが更新される
   ↓
15. ユーザーが動画をクリック
   ↓
16. VideoGuideFragment.navigateToVideoPlayer()が呼ばれる
   ↓
17. VideoPlayerActivityが起動される
```

## DI（依存性注入）の仕組み

### コンポーネント階層

```
AppComponent
  └── UserComponent
       └── MainComponent
            └── VideoGuideComponent (今回追加)
```

### 注入の流れ

1. **MainComponent**に`plus(videoGuideModule: VideoGuideModule): VideoGuideComponent`メソッドを追加
2. **VideoGuideFragment**の`onAttach()`で`MainActivity.mainComponent`から`VideoGuideComponent`を取得
3. `VideoGuideModule()`を渡して`VideoGuideComponent`を生成
4. `inject(this)`でFragmentに依存関係を注入
5. `@Inject`アノテーションが付いたプロパティ（`videoGuideViewModelFactory`）に自動注入される

### 依存関係の解決順序

```
VideoGuideViewModelFactory
  └── VideoGuideRepository
       └── VideoRemoteDataSource (DrupalVideoApiDataSource)
            ├── VideoApiService
            │   ├── OkHttpClient
            │   └── Moshi (Singleton)
            ├── VideoMapper
            └── String (baseUrl from BuildConfig)
```

## 変更点のまとめ

### 1. リソースファイルの変更

- **strings.xml**: `menu_tutorials` → `menu_video_guide`
- **main_menu.xml** (main & debug): メニューIDを`menu_tutorials` → `menu_video_guide`に変更

### 2. MainNavigatorの変更

- `MainScreen` enum: `TUTORIALS` → `VIDEO_GUIDE`
- `openTutorials()` → `openVideoGuide()`に変更
- WebViewActivityへの遷移からFragment遷移に変更
- `restoreScreen()`メソッドを更新

### 3. MainActivityの変更

- メニュークリックハンドラー: `R.id.menu_tutorials` → `R.id.menu_video_guide`

### 4. MainComponentの変更

- `VideoGuideComponent`と`VideoGuideModule`のインポートを追加
- `plus(videoGuideModule: VideoGuideModule): VideoGuideComponent`メソッドを追加

### 5. BuildConfigの変更

- **build.gradle.kts**: `DRUPAL_BASE_URL`を追加
  ```kotlin
  buildConfigField("String", "DRUPAL_BASE_URL", "\"https://drupal.ddev.site/\"")
  ```

### 6. 依存関係の追加

- **build.gradle.kts**: Retrofit、Moshi、MoshiConverterFactoryを追加
  ```kotlin
  implementation("com.squareup.retrofit2:retrofit:2.9.0")
  implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
  implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
  kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
  ```

### 7. 新規作成ファイル

- **Data Layer**:
  - `data/api/VideoApiService.kt`: Retrofit APIインターフェース
  - `data/datasource/VideoRemoteDataSource.kt`: DataSourceインターフェース
  - `data/datasource/DrupalVideoApiDataSource.kt`: Drupal API実装
  - `data/datasource/DummyVideoDataSource.kt`: 開発用ダミーデータソース
  - `data/dto/VideoListResponseDto.kt`: APIレスポンスDTO
  - `data/dto/VideoMediaDto.kt`: メディアエンティティDTO
  - `data/dto/VideoFileDto.kt`: ファイルエンティティDTO
  - `data/mapper/VideoMapper.kt`: DTO → Domain Model変換

- **Domain Layer**:
  - `domain/model/VideoItem.kt`: ドメインモデル（ui/から移動）

- **Presentation Layer**:
  - VideoGuideモジュール全体（既存ファイル）
  - AndroidManifest.xmlに`VideoPlayerActivity`を登録

## 設計パターン

### 1. 分離パターン（Module/Component）

既存のコードベースの主流パターンに従い、`VideoGuideModule`と`VideoGuideComponent`を別ファイルに分離しています。

**メリット**:
- 責務の明確な分離
- 可読性の向上
- 保守性の向上
- 再利用性の向上

### 2. MVVMパターン

- **View**: `VideoGuideFragment`, `VideoGuideScreen`
- **ViewModel**: `VideoGuideViewModel`
- **Model**: `VideoGuideRepository`, `VideoItem`

### 3. Repositoryパターン

データアクセス層を抽象化し、将来的な変更（API、ローカルストレージなど）に対応しやすくしています。

### 4. DataSourceパターン

データ取得の実装を抽象化し、以下の利点を実現：

- **柔軟性**: Dummy/Drupal/Cacheなど、データソースを簡単に切り替え可能
- **テスタビリティ**: モックデータソースでテストが容易
- **保守性**: データ取得ロジックの変更がRepository層に影響しない

### 5. DTOパターン

APIレスポンスをDTOとして定義し、Domain Modelと分離：

- **API仕様変更への対応**: DTO層のみを変更すれば良い
- **Domain Modelの安定性**: Domain Modelは変更不要
- **Mapperによる変換**: DTO → Domain Modelの変換をMapperで実現

## エラーハンドリング

### DataSource層でのエラーハンドリング

- `runCatching`を使用してエラーをキャッチ
- `onFailure`で`Timber.e()`を使用してログ出力
- `getOrElse`でフォールバック値を返す（空リストまたはnull）

### Mapper層でのnull安全性

- `included`がnullの場合は空リストに変換
- `relationships`がnullの場合はnullを返してスキップ
- ファイルが見つからない場合はnullを返してスキップ
- `mapNotNull`でnullを除外

## 実装の特徴

### 1. Moshi Singleton化

`VideoGuideModule`の`companion object`でMoshiインスタンスをSingleton化することで、パフォーマンスとメモリ効率を向上。

### 2. BuildConfigでの設定管理

ベースURLを`BuildConfig.DRUPAL_BASE_URL`で管理し、環境ごとに変更可能。

### 3. JSON:API仕様への準拠

Drupal JSON:APIの仕様に完全準拠：
- `included`配列の処理
- `relationships`による関連エンティティの参照
- snake_caseのJSONキーを`@Json(name = "...")`でマッピング

### 4. 純粋関数としてのMapper

Mapperは純粋関数として実装し、エラーハンドリングはDataSource層で行う。これにより、責務が明確に分離される。

## 今後の実装予定

1. **動画再生機能**
   - ExoPlayerの統合
   - 動画プレイヤーUIの実装
   - 再生状態の管理

2. **UIの改善**
   - サムネイル画像の表示
   - より洗練されたカードデザイン
   - エラーメッセージの表示

3. **キャッシュ機能**
   - Roomデータベースを使用したローカルキャッシュ
   - オフライン対応

4. **認証対応**
   - Drupal APIへの認証が必要な場合の対応

## 参考

- 既存の`TroubleshootingFragment`と同様のパターンで実装
- `AboutModule`と`AboutComponent`の分離パターンを参考
- Jetpack Composeの既存実装パターンに準拠
- Drupal JSON:API仕様: https://www.drupal.org/docs/core-modules-and-modules/jsonapi-module
