package org.dhis2.usescases.videoGuide.video

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.dhis2.usescases.general.ActivityGlobalAbstract

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val videoId = intent.getStringExtra(EXTRA_VIDEO_ID)
        // TODO: 動画再生UIの実装
        // 初期実装では基本的なレイアウトのみ
    }
}

