package org.dhis2.usescases.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.net.toUri
import org.dhis2.R
import org.dhis2.usescases.about.model.AboutLink
import org.dhis2.usescases.about.ui.AboutScreen
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class AboutFragment : FragmentGlobalAbstract() {
    private val aboutViewModel: AboutViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DHIS2Theme {
                    AboutScreen(
                        viewModel = aboutViewModel,
                        onOpenLink = ::openLink,
                    )
                }
            }
        }

    private fun openLink(link: AboutLink) {
        when (link) {
            AboutLink.PRIVACY_POLICY -> startActivity(Intent(requireContext(), PolicyView::class.java))
            else -> link.url?.let(::openInBrowser)
        }
    }

    private fun openInBrowser(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        } catch (e: ActivityNotFoundException) {
            Timber.e(e)
            displayMessage(getString(R.string.about_no_browser))
        }
    }
}
