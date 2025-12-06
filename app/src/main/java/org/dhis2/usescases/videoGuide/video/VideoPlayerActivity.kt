package org.dhis2.usescases.videoGuide.video

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.usescases.general.ActivityGlobalAbstract
import javax.inject.Inject

class VideoPlayerActivity : ActivityGlobalAbstract() {

    companion object {
        private const val EXTRA_VIDEO_ID = "EXTRA_VIDEO_ID"
        private const val STATE_PLAYER_POSITION = "state_player_position"
        private const val STATE_PLAYER_PLAYING = "state_player_playing"

        fun start(context: android.content.Context, videoId: String) {
            val intent = android.content.Intent(context, VideoPlayerActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_ID, videoId)
            }
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var viewModel: VideoPlayerViewModel

    @Inject
    lateinit var exoPlayerManager: ExoPlayerManager

    private var playerView: PlayerView? = null
    private var loadingIndicator: ProgressBar? = null
    private var errorMessage: TextView? = null

    private var playerPosition: Long = 0
    private var playerPlaying: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DI設定
        val videoId = intent.getStringExtra(EXTRA_VIDEO_ID) ?: run {
            finish()
            return
        }

        val videoPlayerComponent = app().appComponent().plus(
            VideoPlayerModule(
                activity = this,
                viewModelStoreOwner = this,
            )
        )
        videoPlayerComponent.inject(this)

        setContentView(R.layout.activity_video_player)

        // Viewの初期化
        playerView = findViewById(R.id.playerView)
        loadingIndicator = findViewById(R.id.loadingIndicator)
        errorMessage = findViewById(R.id.errorMessage)

        // ExoPlayerの初期化
        initializePlayer()

        // ViewModelの観測
        observeViewModel()

        // 動画情報の読み込み
        viewModel.loadVideo(videoId)

        // 保存された状態の復元
        if (savedInstanceState != null) {
            playerPosition = savedInstanceState.getLong(STATE_PLAYER_POSITION, 0)
            playerPlaying = savedInstanceState.getBoolean(STATE_PLAYER_PLAYING, true)
        }
    }

    private fun initializePlayer() {
        val player = exoPlayerManager.initializePlayer()
        playerView?.player = player

        // 再生状態のリスナー
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        loadingIndicator?.visibility = View.GONE
                        // 保存された位置から再生
                        if (playerPosition > 0) {
                            player.seekTo(playerPosition)
                        }
                        player.playWhenReady = playerPlaying
                    }
                    Player.STATE_BUFFERING -> {
                        loadingIndicator?.visibility = View.VISIBLE
                    }
                    Player.STATE_ENDED -> {
                        loadingIndicator?.visibility = View.GONE
                    }
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                loadingIndicator?.visibility = View.GONE
                showError("Playback error: ${error.message}")
            }
        })
    }

    private fun observeViewModel() {
        viewModel.videoItem.observe(this) { videoItem ->
            videoItem?.let {
                playVideo(it.videoUrl)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            loadingIndicator?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                showError(it)
            } ?: run {
                errorMessage?.visibility = View.GONE
            }
        }
    }

    private fun playVideo(videoUrl: String) {
        // Media3では、常に元のURLを使用
        // SimpleCacheが自動的にキャッシュから読み込む
        exoPlayerManager.prepareMediaItem(videoUrl)
        exoPlayerManager.getPlayer()?.let { player ->
            player.playWhenReady = true
        }
    }

    private fun showError(message: String) {
        errorMessage?.apply {
            text = message
            visibility = View.VISIBLE
        }
        loadingIndicator?.visibility = View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        exoPlayerManager.getPlayer()?.let { player ->
            outState.putLong(STATE_PLAYER_POSITION, player.currentPosition)
            outState.putBoolean(STATE_PLAYER_PLAYING, player.playWhenReady)
        }
    }

    override fun onPause() {
        super.onPause()
        exoPlayerManager.getPlayer()?.pause()
    }

    override fun onResume() {
        super.onResume()
        exoPlayerManager.getPlayer()?.play()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        exoPlayerManager.getPlayer()?.let { player ->
            playerPosition = player.currentPosition
            playerPlaying = player.playWhenReady
        }
        exoPlayerManager.releasePlayer()
        playerView?.player = null
    }
}
