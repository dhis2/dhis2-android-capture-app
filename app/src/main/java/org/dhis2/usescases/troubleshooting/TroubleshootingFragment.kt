package org.dhis2.usescases.troubleshooting

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.MainScreenType
import org.dhis2.usescases.troubleshooting.ui.TroubleshootingScreen
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

const val OPEN_LANGUAGE_SECTION = "OPEN_LANGUAGE_SECTION"

class TroubleshootingFragment : FragmentGlobalAbstract() {
    private val troubleshootingViewModel: TroubleshootingViewModel by viewModel {
        parametersOf(arguments?.getBoolean(OPEN_LANGUAGE_SECTION) ?: false)
    }

    companion object {
        fun instance(languageSelectorOpen: Boolean = false): TroubleshootingFragment =
            TroubleshootingFragment().apply {
                arguments =
                    Bundle().apply {
                        putBoolean(OPEN_LANGUAGE_SECTION, languageSelectorOpen)
                    }
            }
    }

    @ExperimentalFoundationApi
    @ExperimentalAnimationApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setContent {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
                )
                DHIS2Theme {
                    TroubleshootingScreen(troubleshootingViewModel) {
                        refreshScreenLanguageChange()
                    }
                }
            }
        }

    private fun refreshScreenLanguageChange() {
        startActivity(
            MainActivity.intent(
                requireContext(),
                MainScreenType.TroubleShooting,
            ),
        )
        requireActivity().finish()
        requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
