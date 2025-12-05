package org.dhis2.usescases.videoGuide

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.videoGuide.ui.VideoGuideScreen
import org.dhis2.usescases.videoGuide.video.VideoPlayerActivity
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import javax.inject.Inject

class VideoGuideFragment : FragmentGlobalAbstract() {

    @Inject
    lateinit var videoGuideViewModelFactory: VideoGuideViewModelFactory

    private val videoGuideViewModel: VideoGuideViewModel by viewModels {
        videoGuideViewModelFactory
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            context.mainComponent.plus(
                VideoGuideModule()
            ).inject(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
                DHIS2Theme {
                    VideoGuideScreen(
                        viewModel = videoGuideViewModel,
                        onVideoClick = { videoId ->
                            navigateToVideoPlayer(videoId)
                        }
                    )
                }
            }
        }
    }

    private fun navigateToVideoPlayer(videoId: String) {
        VideoPlayerActivity.start(requireContext(), videoId)
    }
}

