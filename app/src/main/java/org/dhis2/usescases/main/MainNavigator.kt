package org.dhis2.usescases.main

import android.transition.ChangeBounds
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import dhis2.org.analytics.charts.ui.GroupAnalyticsFragment
import org.dhis2.R
import org.dhis2.usescases.about.AboutFragment
import org.dhis2.usescases.jira.JiraFragment
import org.dhis2.usescases.main.program.ProgramFragment
import org.dhis2.usescases.qrReader.QrReaderFragment
import org.dhis2.usescases.settings.SyncManagerFragment
import org.dhis2.usescases.troubleshooting.TroubleshootingFragment
import org.dhis2.utils.customviews.navigationbar.NavigationBottomBar

class MainNavigator(
    private val fragmentManager: FragmentManager,
    private val onTransitionStart: () -> Unit,
    private val onScreenChanged: (
        titleRes: Int,
        showFilterButton: Boolean,
        showBottomNavigation: Boolean
    ) -> Unit
) {
    enum class MainScreen(@StringRes val title: Int, @IdRes val navViewId: Int) {
        PROGRAMS(R.string.done_task, R.id.menu_home),
        VISUALIZATIONS(R.string.done_task, R.id.menu_home),
        QR(R.string.QR_SCANNER, R.id.qr_scan),
        SETTINGS(R.string.SYNC_MANAGER, R.id.sync_manager),
        TROUBLESHOOTING(R.string.main_menu_troubleshooting, R.id.menu_troubleshooting),
        JIRA(R.string.jira_report, R.id.menu_jira),
        ABOUT(R.string.about, R.id.menu_about)
    }

    private var currentScreen: MainScreen? = null
    private var currentFragment: Fragment? = null

    fun isHome(): Boolean = isPrograms() || isVisualizations()

    fun isPrograms(): Boolean = currentScreen == MainScreen.PROGRAMS

    fun isVisualizations(): Boolean = currentScreen == MainScreen.VISUALIZATIONS

    fun getCurrentIfProgram(): ProgramFragment? {
        return currentFragment?.takeIf { it is ProgramFragment } as ProgramFragment
    }

    fun currentScreenName() = currentScreen?.name

    fun currentNavigationViewItemId(screenName: String): Int =
        MainScreen.valueOf(screenName).navViewId

    fun openHome(navigationBottomBar: NavigationBottomBar) {
        when {
            isVisualizations() -> navigationBottomBar.selectedItemId = R.id.navigation_analytics
            else -> navigationBottomBar.selectedItemId = R.id.navigation_programs
        }
    }

    fun openPrograms() {
        val programFragment = ProgramFragment()
        val sharedView = if (isVisualizations()) {
            (currentFragment as GroupAnalyticsFragment).sharedView()
        } else {
            null
        }
        if (sharedView != null) {
            programFragment.sharedElementEnterTransition = ChangeBounds()
            programFragment.sharedElementReturnTransition = ChangeBounds()
        }
        beginTransaction(
            ProgramFragment(),
            MainScreen.PROGRAMS,
            sharedView
        )
    }

    fun restoreScreen(screenToRestoreName: String, languageSelectorOpened: Boolean = false) {
        when (MainScreen.valueOf(screenToRestoreName)) {
            MainScreen.PROGRAMS -> openPrograms()
            MainScreen.VISUALIZATIONS -> openVisualizations()
            MainScreen.QR -> openQR()
            MainScreen.SETTINGS -> openSettings()
            MainScreen.JIRA -> openJira()
            MainScreen.ABOUT -> openAbout()
            MainScreen.TROUBLESHOOTING -> openTroubleShooting(languageSelectorOpened)
        }
    }

    fun openVisualizations() {
        val visualizationFragment = GroupAnalyticsFragment.forHome()
        val sharedView = if (isPrograms()) {
            (currentFragment as ProgramFragment).sharedView()
        } else {
            null
        }
        if (sharedView != null) {
            visualizationFragment.sharedElementEnterTransition = ChangeBounds()
            visualizationFragment.sharedElementReturnTransition = ChangeBounds()
        }
        beginTransaction(
            visualizationFragment,
            MainScreen.VISUALIZATIONS,
            sharedView
        )
    }

    fun openSettings() {
        beginTransaction(
            SyncManagerFragment(),
            MainScreen.SETTINGS
        )
    }

    fun openQR() {
        beginTransaction(
            QrReaderFragment(),
            MainScreen.QR
        )
    }

    fun openJira() {
        beginTransaction(
            JiraFragment(),
            MainScreen.JIRA
        )
    }

    fun openAbout() {
        beginTransaction(
            AboutFragment(),
            MainScreen.ABOUT
        )
    }

    fun openTroubleShooting(languageSelectorOpened: Boolean = false) {
        beginTransaction(
            fragment = TroubleshootingFragment.instance(languageSelectorOpened),
            screen = MainScreen.TROUBLESHOOTING,
            useFadeInTransition = languageSelectorOpened
        )
    }

    private fun beginTransaction(
        fragment: Fragment,
        screen: MainScreen,
        sharedView: View? = null,
        useFadeInTransition: Boolean = false
    ) {
        if (currentScreen != screen) {
            onTransitionStart()
            currentScreen = screen
            currentFragment = fragment
            val transaction: FragmentTransaction = fragmentManager.beginTransaction()
            transaction.apply {
                if (sharedView == null) {
                    val (enterAnimation, exitAnimation) = if (useFadeInTransition) {
                        Pair(android.R.anim.fade_in, android.R.anim.fade_out)
                    } else {
                        Pair(R.anim.fragment_enter_right, R.anim.fragment_exit_left)
                    }
                    val (enterPopAnimation, exitPopAnimation) = if (useFadeInTransition) {
                        Pair(android.R.anim.fade_in, android.R.anim.fade_out)
                    } else {
                        Pair(R.anim.fragment_enter_left, R.anim.fragment_exit_right)
                    }
                    setCustomAnimations(
                        enterAnimation,
                        exitAnimation,
                        enterPopAnimation,
                        exitPopAnimation
                    )
                } else {
                    setReorderingAllowed(true)
                    addSharedElement(sharedView, "contenttest")
                }
            }
                .replace(R.id.fragment_container, fragment, fragment::class.simpleName)
                .commitAllowingStateLoss()

            onScreenChanged(
                screen.title,
                isPrograms(),
                isHome()
            )
        }
    }
}
