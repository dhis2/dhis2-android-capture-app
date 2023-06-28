package org.dhis2.usescases.datasets.dataSetTable

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import javax.inject.Inject
import kotlinx.coroutines.launch
import org.dhis2.Bindings.dp
import org.dhis2.Bindings.userComponent
import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.commons.dialogs.AlertBottomDialog
import org.dhis2.commons.dialogs.AlertBottomDialog.Companion.instance
import org.dhis2.commons.extensions.closeKeyboard
import org.dhis2.commons.matomo.Labels.Companion.CLICK
import org.dhis2.commons.popupmenu.AppMenuHelper
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.SyncContext
import org.dhis2.databinding.ActivityDatasetTableBinding
import org.dhis2.usescases.datasets.dataSetTable.dataSetDetail.DataSetDetailFragment.Companion.create
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSection
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSectionFragment.Companion.create
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.utils.analytics.SHOW_HELP
import org.dhis2.utils.granularsync.OPEN_ERROR_LOCATION
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.dhis2.utils.granularsync.shouldLaunchSyncDialog
import org.dhis2.utils.validationrules.ValidationResultViolationsAdapter
import org.dhis2.utils.validationrules.Violation

class DataSetTableActivity : ActivityGlobalAbstract(), DataSetTableContract.View {
    private var orgUnitUid: String? = null
    private var catOptCombo: String? = null
    private var dataSetUid: String? = null
    private var periodId: String? = null
    private var accessDataWrite = false

    @Inject
    lateinit var presenter: DataSetTablePresenter
    private lateinit var binding: ActivityDatasetTableBinding
    var isBackPressed = false
        private set
    var dataSetTableComponent: DataSetTableComponent? = null
        private set
    private var behavior: BottomSheetBehavior<View>? = null
    private var reopenProcessor: FlowableProcessor<Boolean>? = null
    private var isKeyboardOpened = false
    override fun onCreate(savedInstanceState: Bundle?) {
        orgUnitUid = intent.getStringExtra(Constants.ORG_UNIT)
        periodId = intent.getStringExtra(Constants.PERIOD_ID)
        catOptCombo = intent.getStringExtra(Constants.CAT_COMB)
        dataSetUid = intent.getStringExtra(Constants.DATA_SET_UID)
        accessDataWrite = intent.getBooleanExtra(Constants.ACCESS_DATA, true)
        reopenProcessor = PublishProcessor.create()
        val openErrorLocation = intent.getBooleanExtra(OPEN_ERROR_LOCATION, false)
        dataSetTableComponent = userComponent()?.plus(
            DataSetTableModule(
                this,
                dataSetUid,
                periodId,
                orgUnitUid,
                catOptCombo,
                openErrorLocation
            )
        )
        dataSetTableComponent!!.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_table)
        binding.presenter = presenter
        binding.BSLayout.bottomSheetLayout.visibility = View.GONE

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                presenter.dataSetScreenState.collect { screenState ->
                    setSections(screenState.sections, screenState.initialSectionToOpenUid)
                    screenState.renderDetails?.let { renderDetails(it) }
                }
            }
        }

        binding.navigationView.selectItemAt(1)
        binding.navigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.navigation_details -> {
                    binding.syncButton.visibility = View.VISIBLE
                    binding.tabLayout.visibility = View.GONE
                    openDetails()
                }
                R.id.navigation_data_entry -> {
                    binding.syncButton.visibility = View.GONE
                    binding.tabLayout.visibility = View.VISIBLE
                    presenter.dataSetScreenState.value.firstSectionUid()?.let {
                        openSection(it)
                    }
                }
            }
            true
        }
        binding.syncButton.setOnClickListener { showGranularSync() }
        binding.saveButton.setOnClickListener {
            if (currentFocus != null) {
                val currentFocus = currentFocus
                currentFocus!!.clearFocus()
                currentFocus.closeKeyboard()
            }
            presenter.handleSaveClick()
        }
        if (intent.shouldLaunchSyncDialog()) {
            showGranularSync()
        }
    }

    private fun openDetails() {
        val fragment = create(dataSetUid!!, accessDataWrite)
        supportFragmentManager.beginTransaction()
            .replace(R.id.content, fragment)
            .commitAllowingStateLoss()
    }

    private fun openSection(sectionUid: String) {
        val fragment = create(
            sectionUid,
            accessDataWrite,
            dataSetUid!!,
            orgUnitUid!!,
            periodId!!,
            catOptCombo!!
        )
        supportFragmentManager.beginTransaction()
            .replace(R.id.content, fragment)
            .commitAllowingStateLoss()
    }

    private fun showGranularSync() {
        presenter.onClickSyncStatus()
        SyncStatusDialog.Builder()
            .withContext(this)
            .withSyncContext(
                SyncContext.DataSetInstance(
                    dataSetUid!!,
                    periodId!!,
                    orgUnitUid!!,
                    catOptCombo!!
                )
            )
            .onDismissListener(object : OnDismissListener {
                override fun onDismiss(hasChanged: Boolean) {
                    if (hasChanged) presenter.updateData()
                }
            })
            .show(DATAVALUE_SYNC)
    }

    override fun startInputEdition() {
        isKeyboardOpened = true
        binding.navigationView.visibility = View.GONE
        binding.saveButton.hide()
        if (binding.BSLayout.bottomSheetLayout.visibility == View.VISIBLE) {
            if (behavior != null && behavior!!.state == BottomSheetBehavior.STATE_EXPANDED) {
                behavior!!.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            closeBottomSheet()
        }
    }

    override fun finishInputEdition() {
        isKeyboardOpened = false
        binding.navigationView.visibility = View.VISIBLE
        binding.saveButton.show()
        if (behavior != null) {
            showBottomSheet()
        }
    }

    override fun setSections(sections: List<DataSetSection>, sectionToOpenUid: String?) {
        val sectionToOpen = sectionToOpenUid ?: sections.firstOrNull()?.uid
        sectionToOpen?.let { sectionUid ->
            binding.tabLayout.removeAllTabs()
            for (section in sections) {
                val tab = binding.tabLayout.newTab()
                tab.text = section.title()
                tab.tag = section.uid
                binding.tabLayout.addTab(tab)
                if (sectionUid == section.uid) tab.select()
            }
            binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val currentFocus = currentFocus
                    currentFocus?.closeKeyboard()
                    openSection(tab.tag as String)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    // unused
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                    // unused
                }
            })
            openSection(sectionUid)
        }
    }

    override fun accessDataWrite(): Boolean {
        return accessDataWrite
    }

    override fun getDataSetUid(): String {
        return dataSetUid!!
    }

    override fun renderDetails(dataSetRenderDetails: DataSetRenderDetails) {
        binding.dataSetName.text = dataSetRenderDetails.title()
        binding.dataSetSubtitle.text = dataSetRenderDetails.subtitle()
    }

    override fun back() {
        if (currentFocus == null || isBackPressed) {
            super.back()
        } else {
            isBackPressed = true
            binding.root.requestFocus()
            back()
        }
    }

    override fun showMandatoryMessage(isMandatoryFields: Boolean) {
        val message = if (isMandatoryFields) {
            getString(R.string.field_mandatory_v2)
        } else {
            getString(R.string.field_required)
        }
        instance
            .setTitle(getString(R.string.saved))
            .setMessage(message)
            .setPositiveButton(getString(R.string.button_ok)) { }
            .show(supportFragmentManager, AlertBottomDialog::class.java.simpleName)
    }

    override fun showValidationRuleDialog() {
        instance
            .setTitle(getString(R.string.saved))
            .setMessage(getString(R.string.run_validation_rules))
            .setPositiveButton(getString(R.string.yes)) {
                presenter.runValidationRules()
            }
            .setNegativeButton(getString(R.string.no)) {
                if (presenter.isComplete()) {
                    finish()
                } else {
                    showSuccessValidationDialog()
                }
            }
            .show(supportFragmentManager, AlertBottomDialog::class.java.simpleName)
    }

    override fun showSuccessValidationDialog() {
        instance
            .setTitle(getString(R.string.validation_success_title))
            .setMessage(getString(R.string.mark_dataset_complete))
            .setPositiveButton(getString(R.string.yes)) {
                presenter.completeDataSet()
            }
            .setNegativeButton(getString(R.string.no)) {
                finish()
            }
            .show(supportFragmentManager, AlertBottomDialog::class.java.simpleName)
    }

    override fun savedAndCompleteMessage() {
        Toast.makeText(this, R.string.dataset_saved_completed, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun showErrorsValidationDialog(violations: List<Violation>) {
        configureShapeDrawable()
        binding.BSLayout.dotsIndicator.visibility =
            if (violations.size > 1) View.VISIBLE else View.INVISIBLE
        initValidationErrorsDialog()
        binding.BSLayout.setErrorCount(violations.size)
        binding.BSLayout.title.text =
            resources.getQuantityText(R.plurals.error_message, violations.size)
        binding.BSLayout.violationsViewPager.adapter =
            ValidationResultViolationsAdapter(this, violations)
        binding.BSLayout.dotsIndicator.setViewPager(binding.BSLayout.violationsViewPager)
        behavior = BottomSheetBehavior.from(binding.BSLayout.bottomSheetLayout)
        behavior!!.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        animateArrowDown()
                        binding.saveButton.animate()
                            .translationY(0f)
                            .start()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        animateArrowUp()
                        binding.saveButton.animate()
                            .translationY(-48.dp.toFloat())
                            .start()
                    }
                    BottomSheetBehavior.STATE_DRAGGING,
                    BottomSheetBehavior.STATE_HALF_EXPANDED,
                    BottomSheetBehavior.STATE_HIDDEN,
                    BottomSheetBehavior.STATE_SETTLING -> {}
                    else -> {}
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                /*UnUsed*/
            }

            private fun animateArrowDown() {
                binding.BSLayout.collapseExpand.animate()
                    .scaleY(-1f).setDuration(200)
                    .start()
            }

            private fun animateArrowUp() {
                binding.BSLayout.collapseExpand.animate()
                    .scaleY(1f).setDuration(200)
                    .start()
            }
        })
    }

    override fun showCompleteToast() {
        Snackbar.make(
            binding.content,
            R.string.dataset_completed,
            BaseTransientBottomBar.LENGTH_SHORT
        )
            .show()
        finish()
    }

    override fun collapseExpandBottom() {
        if (behavior!!.state == BottomSheetBehavior.STATE_EXPANDED) {
            behavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED)
        } else if (behavior!!.state == BottomSheetBehavior.STATE_COLLAPSED) {
            if (isKeyboardOpened) {
                hideKeyboard()
                Handler(Looper.getMainLooper()).postDelayed(
                    { behavior!!.setState(BottomSheetBehavior.STATE_EXPANDED) },
                    100
                )
            } else {
                behavior!!.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun closeBottomSheet() {
        binding.BSLayout.bottomSheetLayout.visibility = View.GONE
    }

    private fun showBottomSheet() {
        binding.BSLayout.bottomSheetLayout.visibility = View.VISIBLE
    }

    override fun completeBottomSheet() {
        closeBottomSheet()
        presenter.completeDataSet()
    }

    override fun isErrorBottomSheetShowing(): Boolean {
        return binding.BSLayout.bottomSheetLayout.visibility == View.VISIBLE
    }

    override fun selectOpenedSection(sectionIndexToOpen: Int) {
        binding.tabLayout.getTabAt(sectionIndexToOpen)!!.select()
    }

    private fun configureShapeDrawable() {
        val cornerSize = resources.getDimensionPixelSize(R.dimen.rounded_16)
        val appearanceModel = ShapeAppearanceModel().toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, cornerSize.toFloat())
            .setTopRightCorner(CornerFamily.ROUNDED, cornerSize.toFloat())
            .build()
        val elevation = resources.getDimensionPixelSize(R.dimen.elevation)
        val shapeDrawable = MaterialShapeDrawable(appearanceModel)
        val color = ResourcesCompat.getColor(resources, R.color.white, null)
        shapeDrawable.fillColor = ColorStateList.valueOf(color)
        binding.BSLayout.bottomSheetLayout.background = shapeDrawable
        binding.BSLayout.bottomSheetLayout.elevation = elevation.toFloat()
    }

    private fun initValidationErrorsDialog() {
        binding.BSLayout.bottomSheetLayout.translationY = 48.dp.toFloat()
        binding.BSLayout.bottomSheetLayout.visibility = View.VISIBLE
        binding.BSLayout.bottomSheetLayout.animate()
            .setDuration(500)
            .setInterpolator(OvershootInterpolator())
            .translationY(0f)
            .start()
        binding.saveButton.animate()
            .translationY(-48.dp.toFloat())
            .start()
    }

    override fun showMoreOptions(view: View) {
        AppMenuHelper.Builder()
            .menu(this, R.menu.dataset_menu)
            .anchor(view)
            .onMenuInflated { popupMenu: PopupMenu ->
                popupMenu.menu.findItem(R.id.reopen).isVisible = presenter.isComplete()
            }
            .onMenuItemClicked { itemId: Int ->
                if (itemId == R.id.showHelp) {
                    analyticsHelper().setEvent(SHOW_HELP, CLICK, SHOW_HELP)
                    showTutorial(true)
                } else if (itemId == R.id.reopen) {
                    showReopenDialog()
                }
                true
            }
            .build()
            .show()
    }

    private fun showReopenDialog() {
        instance
            .setTitle(getString(R.string.are_you_sure))
            .setMessage(getString(R.string.reopen_question))
            .setPositiveButton(getString(R.string.yes)) {
                presenter.reopenDataSet()
            }
            .setNegativeButton(getString(R.string.no)) { }
            .show(supportFragmentManager, AlertBottomDialog::class.java.simpleName)
    }

    override fun displayReopenedMessage(done: Boolean) {
        if (done) {
            Toast.makeText(this, R.string.action_done, Toast.LENGTH_SHORT).show()
            reopenProcessor!!.onNext(true)
        }
    }

    override fun showInternalValidationError() {
        instance
            .setTitle(getString(R.string.saved))
            .setMessage(getString(R.string.validation_internal_error_datasets))
            .setPositiveButton(getString(R.string.button_ok)) {
                presenter.reopenDataSet()
            }
            .show(supportFragmentManager, AlertBottomDialog::class.java.simpleName)
    }

    override fun saveAndFinish() {
        Toast.makeText(
            this,
            if (presenter.isComplete()) R.string.data_set_quality_check_done else R.string.save,
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }

    fun observeReopenChanges(): FlowableProcessor<Boolean>? {
        return reopenProcessor
    }

    companion object {
        private const val DATAVALUE_SYNC = "DATAVALUE_SYNC"

        @JvmStatic
        fun getBundle(
            dataSetUid: String,
            orgUnitUid: String,
            periodId: String,
            catOptCombo: String
        ): Bundle {
            val bundle = Bundle()
            bundle.putString(Constants.DATA_SET_UID, dataSetUid)
            bundle.putString(Constants.ORG_UNIT, orgUnitUid)
            bundle.putString(Constants.PERIOD_ID, periodId)
            bundle.putString(Constants.CAT_COMB, catOptCombo)
            return bundle
        }

        fun intent(
            context: Context,
            dataSetUid: String,
            orgUnitUid: String,
            periodId: String,
            catOptCombo: String
        ): Intent {
            val intent = Intent(context, DataSetTableActivity::class.java)
            intent.putExtras(getBundle(dataSetUid, orgUnitUid, periodId, catOptCombo))
            return intent
        }
    }
}
