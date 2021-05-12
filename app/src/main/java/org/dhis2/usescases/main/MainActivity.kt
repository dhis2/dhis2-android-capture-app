package org.dhis2.usescases.main

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.view.View.GONE
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableInt
import androidx.drawerlayout.widget.DrawerLayout
import javax.inject.Inject
import org.dhis2.Bindings.app
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.data.prefs.Preference
import org.dhis2.databinding.ActivityMainBinding
import org.dhis2.usescases.about.AboutFragment
import org.dhis2.usescases.development.DevelopmentActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.jira.JiraFragment
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.main.program.ProgramFragment
import org.dhis2.usescases.qrReader.QrReaderFragment
import org.dhis2.usescases.settings.SyncManagerFragment
import org.dhis2.usescases.teiDashboard.nfcdata.NfcDataWriteActivity
import org.dhis2.utils.Constants
import org.dhis2.utils.DateUtils
import org.dhis2.utils.analytics.BLOCK_SESSION
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.CLOSE_SESSION
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

    private var programFragment: ProgramFragment? = null

    var activeFragment: FragmentGlobalAbstract? = null

    private var currentFragment = ObservableInt(R.id.menu_home)
    private var isPinLayoutVisible = false

    private var fragId: Int = 0
    private var prefs: SharedPreferences? = null
    private var backDropActive = false

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

        if (savedInstanceState != null) {
            val frag = savedInstanceState.getInt(FRAGMENT)
            currentFragment.set(frag)
            binding.currentFragment = currentFragment
            changeFragment(frag)
        } else {
            binding.currentFragment = currentFragment
            changeFragment(R.id.menu_home)
        }
        initCurrentScreen()

        binding.mainDrawerLayout.addDrawerListener(this)

        prefs = abstracContext.getSharedPreferences(
            Constants.SHARE_PREFS, Context.MODE_PRIVATE
        )

        binding.filterRecycler.adapter = newAdapter

        binding.navigationBar.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_tasks -> {}
                R.id.navigation_programs -> {}
                R.id.navigation_analytics -> {}
                else -> {}
            }
            true
        }

        // TODO: remove to display BottomNavigationView
        binding.fragmentContainer.setPadding(0, 0, 0, 0)
        binding.navigationBar.visibility = View.GONE
        // end

        if (BuildConfig.DEBUG) {
            binding.menu.setOnLongClickListener {
                startActivity(DevelopmentActivity::class.java, null, false, false, null)
                false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(FRAGMENT, fragId)
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
        programFragment!!.openFilter(backDropActive)
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
            fragId != R.id.menu_home -> presenter.onNavigateBackToHome()
            isPinLayoutVisible -> isPinLayoutVisible = false
            else -> super.onBackPressed()
        }
    }

    override fun goToHome() {
        changeFragment(R.id.menu_home)
        initCurrentScreen()
    }

    override fun changeFragment(id: Int) {
        fragId = id
        binding.navView.setCheckedItem(id)
        activeFragment = null

        binding.mainDrawerLayout.closeDrawers()
    }

    override fun updateFilters(totalFilters: Int) {
        binding.totalFilters = totalFilters
    }

    override fun showPeriodRequest(periodRequest: FilterManager.PeriodRequest) {
        if (periodRequest == FilterManager.PeriodRequest.FROM_TO) {
            DateUtils.getInstance()
                .showFromToSelector(this) { FilterManager.getInstance().addPeriod(it) }
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

    override fun showTutorial(shaked: Boolean) {
        when (fragId) {
            R.id.menu_home -> (activeFragment as ProgramFragment).setTutorial()
            R.id.sync_manager -> (activeFragment as SyncManagerFragment).showTutorial()
            else -> showToast(getString(R.string.no_intructions))
        }
    }

    override fun setFilters(filters: List<FilterItem>) {
        newAdapter.submitList(filters)
    }

    override fun hideFilters() {
        binding.filterActionButton.visibility = GONE
    }

    override fun onDrawerStateChanged(newState: Int) {
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
    }

    override fun onDrawerClosed(drawerView: View) {
        if (currentFragment.get() != fragId) {
            initCurrentScreen()
            if (fragId == R.id.menu_home) {
                presenter.initFilters()
            }
        }
    }

    override fun onDrawerOpened(drawerView: View) {
    }

    private fun initCurrentScreen() {
        var tag: String? = null
        when (fragId) {
            R.id.sync_manager -> {
                presenter.onClickSyncManager()
                activeFragment = SyncManagerFragment()
                tag = getString(R.string.SYNC_MANAGER)
                binding.filterActionButton.visibility = View.GONE
            }
            R.id.qr_scan -> {
                activeFragment = QrReaderFragment()
                tag = getString(R.string.QR_SCANNER)
                binding.filterActionButton.visibility = View.GONE
            }
            R.id.nfc_scan -> {
                val intentNfc = Intent(this, NfcDataWriteActivity::class.java)
                startActivity(intentNfc)
            }
            R.id.menu_jira -> {
                activeFragment = JiraFragment()
                tag = getString(R.string.jira_report)
                binding.filterActionButton.visibility = View.GONE
            }
            R.id.menu_about -> {
                activeFragment = AboutFragment()
                tag = getString(R.string.about)
                binding.filterActionButton.visibility = View.GONE
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
                activeFragment = ProgramFragment()
                programFragment = activeFragment as ProgramFragment?
                tag = getString(R.string.done_task)
                binding.filterActionButton.visibility = View.VISIBLE
            }
            else -> {
                activeFragment = ProgramFragment()
                programFragment = activeFragment as ProgramFragment?
                tag = getString(R.string.done_task)
                binding.filterActionButton.visibility = View.VISIBLE
            }
        }

        if (activeFragment != null) {
            currentFragment.set(fragId)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(
                R.anim.fragment_enter_right,
                R.anim.fragment_exit_left,
                R.anim.fragment_enter_left,
                R.anim.fragment_exit_right
            )

            transaction.replace(R.id.fragment_container, activeFragment!!, tag)
                .commitAllowingStateLoss()
            binding.title.text = tag
        }

        if (backDropActive && activeFragment !is ProgramFragment) {
            showHideFilter()
        }
    }
}
