package org.dhis2.usescases.main

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.transition.ChangeBounds
import com.google.android.material.transition.MaterialFadeThrough
import dhis2.org.analytics.charts.ui.GroupAnalyticsFragment
import org.dhis2.R
import org.dhis2.usescases.about.AboutFragment
import org.dhis2.usescases.main.program.ProgramFragment
import org.dhis2.usescases.qrReader.QrReaderFragment
import org.dhis2.usescases.settings.SyncManagerFragment
import org.dhis2.usescases.troubleshooting.TroubleshootingFragment

class MainNavigator(
    private var fragmentManager: FragmentManager,
) {
    private var currentFragment: Fragment? = null
    private var lastHomeFragment: Fragment? = null

    fun updateFragmentManager(fm: FragmentManager) {
        fragmentManager = fm
    }

    fun getCurrentIfProgram(): ProgramFragment? = currentFragment?.takeIf { it is ProgramFragment } as? ProgramFragment

    fun openHome(): MainScreenType =
        when {
            lastHomeFragment is GroupAnalyticsFragment -> {
                MainScreenType.Home(HomeScreen.Visualizations)
            }

            else -> {
                MainScreenType.Home(HomeScreen.Programs)
            }
        }

    fun openPrograms() {
        val programFragment = ProgramFragment()
        lastHomeFragment = programFragment
        val sharedView =
            (currentFragment as? GroupAnalyticsFragment)?.sharedView()?.let { sharedView ->
                programFragment.sharedElementEnterTransition = ChangeBounds()
                programFragment.sharedElementReturnTransition = ChangeBounds()
                sharedView
            }

        beginTransaction(
            programFragment,
            sharedView,
        )
    }

    fun openVisualizations() {
        val visualizationsFragment = GroupAnalyticsFragment.forHome()
        lastHomeFragment = visualizationsFragment
        beginTransaction(visualizationsFragment)
    }

    fun openSettings() {
        beginTransaction(
            SyncManagerFragment(),
        )
    }

    fun openQR() {
        beginTransaction(
            QrReaderFragment(),
        )
    }

    fun openAbout() {
        beginTransaction(
            AboutFragment(),
        )
    }

    fun openTroubleShooting(languageSelectorOpened: Boolean = false) {
        beginTransaction(
            TroubleshootingFragment.instance(languageSelectorOpened),
        )
    }

    private fun beginTransaction(
        fragment: Fragment,
        sharedView: View? = null,
    ) {
        fragmentManager.findFragmentById(R.id.fragment_container)?.exitTransition = MaterialFadeThrough()
        fragment.enterTransition = MaterialFadeThrough()
        currentFragment = fragment
        fragmentManager
            .beginTransaction()
            .apply {
                // Reordering is needed by the shared element transition, but it also makes the
                // transaction wait for fragments that postpone their enter transition, such as
                // GroupAnalyticsFragment, so it is only allowed when there is a shared element.
                sharedView?.let {
                    setReorderingAllowed(true)
                    addSharedElement(it, "contenttest")
                }
            }.replace(R.id.fragment_container, fragment, fragment::class.simpleName)
            .commitAllowingStateLoss()
    }
}
