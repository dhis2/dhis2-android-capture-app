package org.dhis2.usescases.main

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableInt
import com.android.dbexporterlibrary.ExporterListener
import org.dhis2.Bindings.app
import org.dhis2.R
import org.dhis2.data.prefs.Preference
import org.dhis2.databinding.ActivityMainBinding
import org.dhis2.usescases.about.AboutFragment
import org.dhis2.usescases.development.DevelopmentActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.jira.JiraFragment
import org.dhis2.usescases.main.program.ProgramFragment
import org.dhis2.usescases.qrReader.QrReaderFragment
import org.dhis2.usescases.settings.SyncManagerFragment
import org.dhis2.usescases.teiDashboard.nfc_data.NfcDataWriteActivity
import org.dhis2.utils.Constants
import org.dhis2.utils.DateUtils
import org.dhis2.utils.analytics.BLOCK_SESSION
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.CLOSE_SESSION
import org.dhis2.utils.filters.FilterManager
import org.dhis2.utils.filters.FiltersAdapter
import org.dhis2.utils.session.PIN_DIALOG_TAG
import org.dhis2.utils.session.PinDialog
import javax.inject.Inject

private const val FRAGMENT = "Fragment"
private const val PERMISSION_REQUEST = 1987

class MainActivity : ActivityGlobalAbstract(), MainView, ExporterListener {
    private lateinit var binding: ActivityMainBinding
    @Inject
    lateinit var presenter: MainPresenter

    private var programFragment: ProgramFragment? = null

    var activeFragment: FragmentGlobalAbstract? = null

    private var currentFragment = ObservableInt(R.id.menu_home)
    private var isPinLayoutVisible = false

    private var fragId: Int = 0
    private var prefs: SharedPreferences? = null
    private var backDropActive = false
    var adapter: FiltersAdapter? = null
        private set

    // -------------------------------------
    //region LIFECYCLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app().userComponent()?.plus(MainModule(this))!!.inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.presenter = presenter
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

        prefs = abstracContext.getSharedPreferences(
            Constants.SHARE_PREFS, Context.MODE_PRIVATE
        )

        adapter = FiltersAdapter(FiltersAdapter.ProgramType.ALL)
        binding.filterLayout.adapter = adapter

        binding.moreOptions.setOnLongClickListener {
            startActivity(DevelopmentActivity::class.java, null, false, false, null)
            false
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

        if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                PERMISSION_REQUEST
            )
        }
        binding.totalFilters = FilterManager.getInstance().totalFilters
        adapter!!.notifyDataSetChanged()
    }

    override fun onPause() {
        presenter.onDetach()
        super.onPause()
    }

    //endregion

    /*User info methods*/

    override fun renderUsername(username: String) {
        binding.userName = username
        (binding.navView.getHeaderView(0).findViewById<View>(R.id.user_info) as TextView)
            .text = username
        binding.executePendingBindings()
    }

    /*End of user info methods*/

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
                R.id.filterLayout,
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
                    this,
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
            fragId != R.id.menu_home -> changeFragment(R.id.menu_home)
            isPinLayoutVisible -> {
                isPinLayoutVisible = false
                /*startActivity(Intent(this@MainActivity, MainActivity::class.java))
                finish()*/
            }
            else -> super.onBackPressed()
        }
    }

    override fun changeFragment(id: Int) {
        fragId = id
        binding.navView.setCheckedItem(id)
        activeFragment = null
        var tag: String? = null

        when (id) {
            R.id.sync_manager -> {
                activeFragment = SyncManagerFragment()
                tag = getString(R.string.SYNC_MANAGER)
                binding.filter.visibility = View.GONE
            }
            R.id.qr_scan -> {
                activeFragment = QrReaderFragment()
                tag = getString(R.string.QR_SCANNER)
                binding.filter.visibility = View.GONE
            }
            R.id.nfc_scan -> {
                val intentNfc = Intent(this, NfcDataWriteActivity::class.java)
                startActivity(intentNfc)
            }
            R.id.menu_jira -> {
                activeFragment = JiraFragment()
                tag = getString(R.string.jira_report)
                binding.filter.visibility = View.GONE
            }
            R.id.menu_about -> {
                activeFragment = AboutFragment()
                tag = getString(R.string.about)
                binding.filter.visibility = View.GONE
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
                binding.filter.visibility = View.VISIBLE
            }
            else -> {
                activeFragment = ProgramFragment()
                programFragment = activeFragment as ProgramFragment?
                tag = getString(R.string.done_task)
                binding.filter.visibility = View.VISIBLE
            }
        }

        if (activeFragment != null) {
            currentFragment.set(id)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, activeFragment!!, tag).commitAllowingStateLoss()
            binding.title.text = tag
        }
        binding.mainDrawerLayout.closeDrawers()

        if (backDropActive && activeFragment !is ProgramFragment) {
            showHideFilter()
        }
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

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FilterManager.OU_TREE && resultCode == Activity.RESULT_OK) {
            adapter!!.notifyDataSetChanged()
            updateFilters(FilterManager.getInstance().totalFilters)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun fail(message: String, exception: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun success(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }
}
