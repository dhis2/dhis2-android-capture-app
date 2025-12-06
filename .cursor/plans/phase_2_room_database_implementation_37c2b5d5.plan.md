---
name: Phase 2 Room Database Implementation
overview: "フェーズ2: Room Databaseによるダウンロード状態管理の実装。ダウンロード済み動画の情報をローカルDBに保存・管理するためのRoom Database、Entity、DAO、LocalDataSource、Repository拡張を実装します。"
todos:
  - id: create-entity
    content: DownloadedVideoEntity.ktを作成 - Roomエンティティクラスの実装（videoId, title, description, videoUrl, thumbnailUrl, localFilePath, downloadedAt, fileSize, duration, tag, categoryフィールド）
    status: completed
  - id: create-dao
    content: DownloadedVideoDao.ktを作成 - Room DAOインターフェースの実装（getAll, getById, insert, delete, deleteByIdメソッド）
    status: completed
    dependencies:
      - create-entity
  - id: create-database
    content: VideoDatabase.ktを作成 - Room Databaseクラスの実装（entities, version, DAOメソッド）
    status: completed
    dependencies:
      - create-dao
  - id: create-local-datasource-interface
    content: VideoLocalDataSource.ktを作成 - ローカルデータソースのインターフェース定義（getAllDownloadedVideos, getDownloadedVideoById, saveDownloadedVideo, deleteDownloadedVideo, isDownloaded）
    status: completed
  - id: create-local-datasource-impl
    content: RoomVideoLocalDataSource.ktを作成 - VideoLocalDataSourceの実装（Entity ↔ VideoItem変換ロジック含む）
    status: completed
    dependencies:
      - create-local-datasource-interface
      - create-dao
  - id: extend-repository
    content: VideoGuideRepository.ktを拡張 - VideoLocalDataSourceを追加し、getDownloadedVideoList()とisVideoDownloaded()メソッドを実装
    status: completed
    dependencies:
      - create-local-datasource-impl
  - id: extend-module
    content: VideoGuideModule.ktを拡張 - Room Database、DAO、LocalDataSourceのProviderメソッドを追加し、RepositoryのProviderを更新
    status: completed
    dependencies:
      - create-database
      - extend-repository
---

# Phase 2: Room Database実装計画

## 実装目標

ダウンロード済み動画の情報をローカルDBに保存・管理するための基盤を構築する。

## 実装ファイル一覧

### 1. Room Database関連ファイル

#### 1.1 DownloadedVideoEntity.kt

**パス**: `app/src/main/java/org/dhis2/usescases/videoGuide/data/local/DownloadedVideoEntity.kt`

**実装内容**:

- `@Entity`アノテーション付きデータクラス
- テーブル名: `downloaded_videos`
- フィールド:
- `videoId: String` (PrimaryKey)
- `title: String`
- `description: String`
- `videoUrl: String`
- `thumbnailUrl: String?`
- `localFilePath: String` (ダウンロード先のローカルパス)
- `downloadedAt: Long` (ダウンロード日時、ミリ秒)
- `fileSize: Long` (ファイルサイズ、バイト)
- `duration: Long?` (動画の長さ、ミリ秒)
- `tag: String?` (VideoItemのtagフィールドに対応)
- `category: String?` (VideoItemのcategoryフィールドに対応)

**注意**: VideoItemの`tag`と`category`フィールドも保存するため、Entityに追加。

#### 1.2 DownloadedVideoDao.kt

**パス**: `app/src/main/java/org/dhis2/usescases/videoGuide/data/local/DownloadedVideoDao.kt`

**実装内容**:

- `@Dao`インターフェース
- メソッド:
- `getAll(): List<DownloadedVideoEntity>` (suspend)
- `getById(videoId: String): DownloadedVideoEntity?` (suspend)
- `insert(video: DownloadedVideoEntity)` (suspend, OnConflictStrategy.REPLACE)
- `delete(video: DownloadedVideoEntity)` (suspend)
- `deleteById(videoId: String)` (suspend)

#### 1.3 VideoDatabase.kt

**パス**: `app/src/main/java/org/dhis2/usescases/videoGuide/data/local/VideoDatabase.kt`

**実装内容**:

- `@Database`アノテーション付き抽象クラス
- entities: `[DownloadedVideoEntity::class]`
- version: `1`
- exportSchema: `false`
- `downloadedVideoDao(): DownloadedVideoDao` 抽象メソッド

### 2. DataSource関連ファイル

#### 2.1 VideoLocalDataSource.kt (インターフェース)

**パス**: `app/src/main/java/org/dhis2/usescases/videoGuide/data/datasource/VideoLocalDataSource.kt`

**実装内容**:

- インターフェース定義
- メソッド:
- `getAllDownloadedVideos(): List<VideoItem>` (suspend)
- `getDownloadedVideoById(videoId: String): VideoItem?` (suspend)
- `saveDownloadedVideo(video: VideoItem, localFilePath: String)` (suspend)
- `deleteDownloadedVideo(videoId: String)` (suspend)
- `isDownloaded(videoId: String): Boolean` (suspend)

#### 2.2 RoomVideoLocalDataSource.kt (実装)

**パス**: `app/src/main/java/org/dhis2/usescases/videoGuide/data/datasource/RoomVideoLocalDataSource.kt`

**実装内容**:

- `VideoLocalDataSource`の実装クラス
- `DownloadedVideoDao`を依存関係として受け取る
- Entity ↔ VideoItemの変換ロジックを実装
- `@Inject constructor(dao: DownloadedVideoDao)`でDI

**変換ロジック**:

- Entity → VideoItem: `DownloadedVideoEntity`を`VideoItem`に変換（`localFilePath`は`videoUrl`として使用しない）
- VideoItem → Entity: `VideoItem`と`localFilePath`から`DownloadedVideoEntity`を作成

### 3. Repository拡張

#### 3.1 VideoGuideRepository.kt の拡張

**パス**: `app/src/main/java/org/dhis2/usescases/videoGuide/VideoGuideRepository.kt`

**変更内容**:

- `VideoLocalDataSource`をコンストラクタに追加
- 追加メソッド:
- `getDownloadedVideoList(): List<VideoItem>` - ローカルDBから全ダウンロード済み動画を取得
- `isVideoDownloaded(videoId: String): Boolean` - ダウンロード済みかチェック

**注意**: 既存の`getVideoList()`と`getVideoById()`は変更しない。

### 4. DI Module拡張

#### 4.1 VideoGuideModule.kt の拡張

**パス**: `app/src/main/java/org/dhis2/usescases/videoGuide/VideoGuideModule.kt`

**追加する`@Provides`メソッド**:

1. `provideVideoDatabase(context: Context): VideoDatabase`

- `Room.databaseBuilder()`を使用
- `context.applicationContext`を使用（メモリリーク防止）
- データベース名: `"video_database"`
- `@PerFragment`スコープ（既存パターンに合わせる）

2. `provideDownloadedVideoDao(database: VideoDatabase): DownloadedVideoDao`

- `database.downloadedVideoDao()`を返す
- `@PerFragment`スコープ

3. `provideVideoLocalDataSource(dao: DownloadedVideoDao): VideoLocalDataSource`

- `RoomVideoLocalDataSource(dao)`を返す
- `@PerFragment`スコープ

**既存の`provideRepository()`メソッドの修正**:

- `VideoLocalDataSource`パラメータを追加
- `VideoGuideRepository(dataSource, localDataSource)`に変更

## 実装順序

1. **Room Database関連** (1.1 → 1.2 → 1.3)

- Entity → DAO → Database の順で実装

2. **DataSource関連** (2.1 → 2.2)

- インターフェース → 実装の順で実装

3. **Repository拡張** (3.1)

- VideoGuideRepositoryにLocalDataSourceを追加

4. **DI Module拡張** (4.1)

- VideoGuideModuleにRoom関連のProviderを追加

## 注意事項

1. **Contextの使用**: Room Databaseの作成時は`context.applicationContext`を使用（Activity ContextではなくApplication Context）

2. **スコープ**: Room Databaseは通常Singletonだが、既存パターンに合わせて`@PerFragment`スコープを使用。Roomは内部的にSingleton管理されるため問題なし。

3. **Entity ↔ Domain変換**: `RoomVideoLocalDataSource`内でEntityとVideoItemの変換を行う。`localFilePath`はVideoItemには含めない（フェーズ4でオフライン再生時に使用）。

4. **既存コードへの影響**: 既存の`getVideoList()`と`getVideoById()`は変更しない。フェーズ4でオフライン再生を実装する際に拡張する可能性がある。

5. **データベースバージョン**: 初回実装なのでversion 1。将来的にスキーマ変更が必要な場合はマイグレーションを実装。

6. **エラーハンドリング**: Room操作はsuspend関数なので、呼び出し側でtry-catchでエラーハンドリングを行う（Repository層で実装）。

## テスト考慮事項

- Room Databaseのテストには`androidx.room:room-testing`を使用可能（今回は実装のみ）
- インメモリデータベースを使用したテストを検討（フェーズ5以降）