package org.dhis2.usescases.troubleshooting

import android.R
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.MainNavigator
import org.dhis2.usescases.troubleshooting.ui.TroubleshootingScreen
import javax.inject.Inject

const val OPEN_LANGUAGE_SECTION = "OPEN_LANGUAGE_SECTION"

class TroubleshootingFragment : FragmentGlobalAbstract() {

    @Inject
    lateinit var troubleshootingViewModelFactory: TroubleshootingViewModelFactory

    private val troubleshootingViewModel: TroubleshootingViewModel by viewModels {
        troubleshootingViewModelFactory
    }

    companion object {
        fun instance(languageSelectorOpen: Boolean = false): TroubleshootingFragment {
            return TroubleshootingFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(OPEN_LANGUAGE_SECTION, languageSelectorOpen)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            context.mainComponent.plus(
                TroubleshootingModule(
                    arguments?.getBoolean(OPEN_LANGUAGE_SECTION) ?: false,
                ),
            ).inject(this)
        }
    }

    @ExperimentalFoundationApi
    @ExperimentalAnimationApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
                )
                MdcTheme {
                    TroubleshootingScreen(troubleshootingViewModel) {
                        refreshScreenLanguageChange()
                    }
                }
            }
        }
    }

    private fun refreshScreenLanguageChange() {
        startActivity(
            MainActivity.intent(
                requireContext(),
                MainNavigator.MainScreen.TROUBLESHOOTING,
            ),
        )
        requireActivity().finish()
        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
