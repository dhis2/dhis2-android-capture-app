package org.dhis2.usescases.main

import android.transition.ChangeBounds
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import dhis2.org.analytics.charts.ui.GroupAnalyticsFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.R
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.usescases.about.AboutFragment
import org.dhis2.usescases.main.program.ProgramFragment
import org.dhis2.usescases.qrReader.QrReaderFragment
import org.dhis2.usescases.settings.SyncManagerFragment
import org.dhis2.usescases.troubleshooting.TroubleshootingFragment

class MainNavigator(
    private val dispatcherProvider: DispatcherProvider,
    private val fragmentManager: FragmentManager,
) {
    private var currentFragment: Fragment? = null
    private var lastHomeFragment: Fragment? = null

    fun getCurrentIfProgram(): ProgramFragment? =
        currentFragment?.takeIf { it is ProgramFragment } as? ProgramFragment


    fun openHome(): MainScreenType {
        return when {
            lastHomeFragment is GroupAnalyticsFragment -> {
                MainScreenType.Home(HomeScreen.Visualizations)
            }

            else -> {
                MainScreenType.Home(HomeScreen.Programs)
            }
        }
    }

    fun openPrograms() {
        val programFragment = ProgramFragment()
        lastHomeFragment = ProgramFragment()
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
            fragment = TroubleshootingFragment.instance(languageSelectorOpened),
            useFadeInTransition = languageSelectorOpened,
        )
    }

    private fun beginTransaction(
        fragment: Fragment,
        sharedView: View? = null,
        useFadeInTransition: Boolean = false,
    ) {
        currentFragment = fragment
        CoroutineScope(dispatcherProvider.ui()).launch {
            withContext(dispatcherProvider.io()) {
                val transaction: FragmentTransaction = fragmentManager.beginTransaction()
                transaction
                    .apply {
                        if (sharedView == null) {
                            val (enterAnimation, exitAnimation) = getEnterExitAnimation(
                                useFadeInTransition
                            )
                            val (enterPopAnimation, exitPopAnimation) = getEnterExitPopAnimation(
                                useFadeInTransition
                            )
                            setCustomAnimations(
                                enterAnimation,
                                exitAnimation,
                                enterPopAnimation,
                                exitPopAnimation,
                            )
                        } else {
                            setReorderingAllowed(true)
                            addSharedElement(sharedView, "contenttest")
                        }
                    }.replace(R.id.fragment_container, fragment, fragment::class.simpleName)
                    .commitAllowingStateLoss()
            }
        }
    }

    private fun getEnterExitPopAnimation(useFadeInTransition: Boolean): Pair<Int, Int> =
        if (useFadeInTransition) {
            Pair(android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            Pair(R.anim.fragment_enter_left, R.anim.fragment_exit_right)
        }

    private fun getEnterExitAnimation(useFadeInTransition: Boolean): Pair<Int, Int> =
        if (useFadeInTransition) {
            Pair(android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            Pair(R.anim.fragment_enter_right, R.anim.fragment_exit_left)
        }
}
