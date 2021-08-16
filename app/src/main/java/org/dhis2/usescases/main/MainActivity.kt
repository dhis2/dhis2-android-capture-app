package org.dhis2.usescases.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import javax.inject.Inject
import org.dhis2.Bindings.app
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.commons.prefs.Preference
import org.dhis2.databinding.ActivityMainBinding
import org.dhis2.usescases.development.DevelopmentActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.utils.Constants
import org.dhis2.utils.DateUtils
import org.dhis2.utils.analytics.BLOCK_SESSION
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.CLOSE_SESSION
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.dhis2.utils.extension.navigateTo
import org.dhis2.utils.filters.FilterItem
import org.dhis2.utils.filters.FilterManager
import org.dhis2.utils.filters.FiltersAdapter
import org.dhis2.utils.session.PIN_DIALOG_TAG
import org.dhis2.utils.session.PinDialog

private const val FRAGMENT = "Fragment"

class MainActivity :
    ActivityGlobalAbstract(),
    MainView,
    DrawerLayout.DrawerListener {

    private lateinit var binding: ActivityMainBinding
    lateinit var mainComponent: MainComponent

    @Inject
    lateinit var presenter: MainPresenter

    @Inject
    lateinit var newAdapter: FiltersAdapter

    @Inject
    lateinit var pageConfigurator: NavigationPageConfigurator

    private val getDevActivityContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            binding.navigationBar.pageConfiguration(pageConfigurator)
        }

    private var isPinLayoutVisible = false

    private var prefs: SharedPreferences? = null
    private var backDropActive = false
    private var elevation = 0f
    private val mainNavigator = MainNavigator(
        supportFragmentManager,
        {
            if (backDropActive) {
                showHideFilter()
            }
        }
    ) { titleRes, showFilterButton, showBottomNavigation ->
        setTitle(getString(titleRes))
        setFilterButtonVisibility(showFilterButton)
        setBottomNavigationVisibility(showBottomNavigation)
    }

    //region LIFECYCLE

    override fun onCreate(savedInstanceState: Bundle?) {
        app().userComponent()?.let {
            mainComponent = it.plus(MainModule(this)).apply {
                inject(this@MainActivity)
            }
        } ?: navigateTo<LoginActivity>(true)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        if (::presenter.isInitialized) {
            binding.presenter = presenter
        } else {
            navigateTo<LoginActivity>(true)
        }

        binding.navView.setNavigationItemSelectedListener { item ->
            changeFragment(item.itemId)
            false
        }

        val restoreScreenName = savedInstanceState?.getString(FRAGMENT)
        if (restoreScreenName != null) {
            changeFragment(mainNavigator.currentNavigationViewItemId(restoreScreenName))
            mainNavigator.restoreScreen(restoreScreenName)
        } else {
            changeFragment(R.id.menu_home)
            initCurrentScreen()
        }

        binding.mainDrawerLayout.addDrawerListener(this)

        prefs = abstracContext.getSharedPreferences(
            Constants.SHARE_PREFS, Context.MODE_PRIVATE
        )

        binding.filterRecycler.adapter = newAdapter

        binding.navigationBar.pageConfiguration(pageConfigurator)
        binding.navigationBar.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_tasks -> {
                }
                R.id.navigation_programs -> {
                    mainNavigator.openPrograms()
                }
                R.id.navigation_analytics -> {
                    mainNavigator.openVisualizations()
                }
            }
            true
        }

        if (BuildConfig.DEBUG) {
            binding.menu.setOnLongClickListener {
                getDevActivityContent.launch(Intent(this, DevelopmentActivity::class.java))
                false
            }
        }

        elevation = ViewCompat.getElevation(binding.toolbar)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(FRAGMENT, mainNavigator.currentScreenName())
    }

    override fun onResume() {
        super.onResume()

        presenter.init()
        presenter.initFilters()

        binding.totalFilters = FilterManager.getInstance().totalFilters
    }

    override fun onPause() {
        presenter.setOpeningFilterToNone()
        presenter.onDetach()
        super.onPause()
    }

    override fun renderUsername(username: String) {
        binding.userName = username
        (binding.navView.getHeaderView(0).findViewById<View>(R.id.user_info) as TextView)
            .text = username
        binding.executePendingBindings()
    }

    override fun openDrawer(gravity: Int) {
        if (!binding.mainDrawerLayout.isDrawerOpen(gravity)) {
            binding.mainDrawerLayout.openDrawer(gravity)
        } else {
            binding.mainDrawerLayout.closeDrawer(gravity)
        }
    }

    override fun showHideFilter() {
        val transition = ChangeBounds()
        transition.duration = 200
        TransitionManager.beginDelayedTransition(binding.backdropLayout, transition)
        backDropActive = !backDropActive
        val initSet = ConstraintSet()
        initSet.clone(binding.backdropLayout)
        if (backDropActive) {
            initSet.connect(
                R.id.fragment_container,
                ConstraintSet.TOP,
                R.id.filterRecycler,
                ConstraintSet.BOTTOM,
                50
            )
        } else {
            initSet.connect(
                R.id.fragment_container,
                ConstraintSet.TOP,
                R.id.toolbar,
                ConstraintSet.BOTTOM,
                0
            )
        }
        initSet.applyTo(binding.backdropLayout)
        mainNavigator.getCurrentIfProgram()?.openFilter(backDropActive)
    }

    override fun onLockClick() {
        if (prefs!!.getString(Preference.PIN, null) == null) {
            binding.mainDrawerLayout.closeDrawers()
            PinDialog(
                PinDialog.Mode.SET,
                true,
                { presenter.blockSession() },
                {}
            ).show(supportFragmentManager, PIN_DIALOG_TAG)
            isPinLayoutVisible = true
        } else {
            presenter.blockSession()
        }
    }

    override fun onBackPressed() {
        when {
            !mainNavigator.isHome() -> presenter.onNavigateBackToHome()
            isPinLayoutVisible -> isPinLayoutVisible = false
            else -> super.onBackPressed()
        }
    }

    override fun goToHome() {
        mainNavigator.openPrograms()
    }

    override fun changeFragment(id: Int) {
        binding.navView.setCheckedItem(id)
        binding.mainDrawerLayout.closeDrawers()
    }

    override fun updateFilters(totalFilters: Int) {
        binding.totalFilters = totalFilters
    }

    override fun showPeriodRequest(periodRequest: FilterManager.PeriodRequest) {
        if (periodRequest == FilterManager.PeriodRequest.FROM_TO) {
            DateUtils.getInstance()
                .fromCalendarSelector(this) { FilterManager.getInstance().addPeriod(it) }
        } else {
            DateUtils.getInstance()
                .showPeriodDialog(
                    this,
                    { datePeriods -> FilterManager.getInstance().addPeriod(datePeriods) },
                    true
                )
        }
    }

    fun setTitle(title: String) {
        binding.title.text = title
    }

    private fun setFilterButtonVisibility(showFilterButton: Boolean) {
        binding.filterActionButton.visibility = if (showFilterButton) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun setBottomNavigationVisibility(showBottomNavigation: Boolean) {
        if (showBottomNavigation) {
            binding.navigationBar.show()
        } else {
            binding.navigationBar.hide()
        }
    }

    override fun setFilters(filters: List<FilterItem>) {
        newAdapter.submitList(filters)
    }

    override fun hideFilters() {
        binding.filterActionButton.visibility = View.GONE
    }

    override fun onDrawerStateChanged(newState: Int) {
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
    }

    override fun onDrawerClosed(drawerView: View) {
        initCurrentScreen()
        if (mainNavigator.isPrograms()) {
            presenter.initFilters()
        }
    }

    override fun onDrawerOpened(drawerView: View) {
    }

    private fun initCurrentScreen() {
        when (binding.navView.checkedItem?.itemId) {
            R.id.sync_manager -> {
                presenter.onClickSyncManager()
                mainNavigator.openSettings()
            }
            R.id.qr_scan -> {
                mainNavigator.openQR()
            }
            R.id.menu_jira -> {
                mainNavigator.openJira()
            }
            R.id.menu_about -> {
                mainNavigator.openAbout()
            }
            R.id.block_button -> {
                analyticsHelper.setEvent(BLOCK_SESSION, CLICK, BLOCK_SESSION)
                onLockClick()
            }
            R.id.logout_button -> {
                analyticsHelper.setEvent(CLOSE_SESSION, CLICK, CLOSE_SESSION)
                presenter.logOut()
            }
            R.id.menu_home -> {
                mainNavigator.openPrograms()
            }
        }

        if (backDropActive && mainNavigator.isPrograms()) {
            showHideFilter()
        }
    }
}
