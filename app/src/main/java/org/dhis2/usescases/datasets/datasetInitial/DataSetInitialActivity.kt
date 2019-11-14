package org.dhis2.usescases.datasets.datasetInitial

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar
import java.util.Date
import java.util.HashMap
import java.util.Locale
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.R
import org.dhis2.databinding.ActivityDatasetInitialBinding
import org.dhis2.databinding.ItemCategoryComboBinding
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.utils.Constants
import org.dhis2.utils.DateUtils
import org.dhis2.utils.customviews.CategoryOptionPopUp
import org.dhis2.utils.customviews.OrgUnitDialog
import org.dhis2.utils.customviews.PeriodDialog
import org.dhis2.utils.customviews.PeriodDialogInputPeriod
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.PeriodType

class DataSetInitialActivity : ActivityGlobalAbstract(), DataSetInitialView {

    private lateinit var binding: ActivityDatasetInitialBinding

    @Inject
    lateinit var presenter: DataSetInitialPresenter

    private lateinit var selectedView: View

    private var selectedCatOptionsMap: HashMap<String, CategoryOption?>? = null

    private var selectedOrgUnit: OrganisationUnit? = null

    override var selectedPeriod: Date? = null

    private lateinit var dataSetUid: String

    public override fun onCreate(savedInstanceState: Bundle?) {
        dataSetUid = intent.getStringExtra(Constants.DATA_SET_UID)
        (applicationContext as App).userComponent()?.plus(DataSetInitialModule(this, dataSetUid))
            ?.inject(this)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_initial)
        binding.presenter = presenter
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
    }

    override fun onPause() {
        presenter.onDettach()
        super.onPause()
    }

    override fun setData(dataSetInitialModel: DataSetInitialModel) {
        binding.dataSetModel = dataSetInitialModel
        binding.catComboContainer.removeAllViews()
        selectedCatOptionsMap = HashMap()
        if (dataSetInitialModel.categoryComboName() != "default") {
            dataSetInitialModel.categories.forEach { category ->
                selectedCatOptionsMap?.put(category.uid(), null)
                val categoryComboBinding = ItemCategoryComboBinding.inflate(
                    layoutInflater,
                    binding.catComboContainer,
                    false
                )
                categoryComboBinding.inputLayout.hint = category.displayName()
                categoryComboBinding.inputEditText.setOnClickListener { view ->
                    selectedView = view
                    presenter.onCatOptionClick(category.uid())
                }
                binding.catComboContainer.addView(categoryComboBinding.root)
            }
        } else {
            presenter.onCatOptionClick(dataSetInitialModel.categories[0].uid())
        }
        checkActionVisivbility()
    }

    private fun clearCatOptionCombo() {
        if (binding.dataSetModel!!.categoryComboName() != "default") {
            repeat(binding.catComboContainer!!.childCount) {
                val catView = binding.catComboContainer!!.getChildAt(it)
                (catView.findViewById<View>(R.id.input_editText) as TextInputEditText).setText(null)
            }
            for (categories in binding.dataSetModel!!.categories) {
                selectedCatOptionsMap!![categories.uid()] = null
            }
        }
    }

    /**
     * When changing orgUnit, date must be cleared
     */
    override fun showOrgUnitDialog(orgUnits: List<OrganisationUnit>) {
        val orgUnitDialog = OrgUnitDialog.getInstace()
        orgUnitDialog
            .setMultiSelection(false)
            .setOrgUnits(orgUnits)
            .setProgram(dataSetUid)
            .setTitle(getString(R.string.org_unit))
            .setPossitiveListener {
                if (!orgUnitDialog.selectedOrgUnit.isNullOrEmpty()) {
                    selectedOrgUnit = orgUnitDialog.selectedOrgUnitModel
                    selectedOrgUnit ?: orgUnitDialog.dismiss()
                    binding.dataSetOrgUnitEditText.setText(selectedOrgUnit!!.displayName())
                    binding.dataSetPeriodEditText.setText(null)
                    selectedPeriod = null
                    clearCatOptionCombo()
                }
                checkActionVisivbility()
                orgUnitDialog.dismiss()
            }
            .setNegativeListener { orgUnitDialog.dismiss() }
            .show(supportFragmentManager, OrgUnitDialog::class.java.simpleName)
    }

    override fun showPeriodSelector(
        periodType: PeriodType,
        periods: List<DateRangeInputPeriodModel>,
        openFuturePeriods: Int?
    ) {
        val periodDialog = PeriodDialogInputPeriod()
        periodDialog.setInputPeriod(periods)
            .setOpenFuturePeriods(openFuturePeriods)
            .setPeriod(periodType)
            .setTitle(binding.dataSetPeriodInputLayout.hint!!.toString())
            .setPossitiveListener { selectedDate ->
                val calendar = Calendar.getInstance()
                calendar.time = selectedDate
                this.selectedPeriod = calendar.time
                binding.dataSetPeriodEditText.setText(
                    DateUtils.getInstance().getPeriodUIString(
                        periodType,
                        selectedDate,
                        Locale.getDefault()
                    )
                )
                clearCatOptionCombo()
                checkActionVisivbility()
                periodDialog.dismiss()
            }
            .setNegativeListener { v ->
                this.selectedPeriod = null
                binding.dataSetPeriodEditText.setText(null)
                checkActionVisivbility()
            }
            .show(supportFragmentManager, PeriodDialog::class.java.simpleName)
    }

    override fun showCatComboSelector(catOptionUid: String, catOptions: List<CategoryOption>) {
        if (catOptions.size == 1 && catOptions[0].name() == "default") {
            selectedCatOptionsMap = selectedCatOptionsMap ?: HashMap()
            selectedCatOptionsMap!![catOptionUid] = catOptions[0]
        } else {
            CategoryOptionPopUp.getInstance()
                .setCategoryName((selectedView as TextInputEditText).hint!!.toString())
                .setCatOptions(catOptions)
                .setDate(selectedPeriod)
                .setOnClick { item ->
                    item?.let {
                        selectedCatOptionsMap!![catOptionUid] = it
                    } ?: selectedCatOptionsMap!!.remove(catOptionUid)

                    (selectedView as TextInputEditText).setText(item?.displayName())
                    checkActionVisivbility()
                }
                .show(this, selectedView)
        }
    }

    override fun getSelectedCatOptions(): List<String> {
        return selectedCatOptionsMap?.values?.map { it?.uid() ?: "" } ?: arrayListOf()
    }

    override fun setOrgUnit(organisationUnit: OrganisationUnit) {
        selectedOrgUnit = organisationUnit
        binding.dataSetOrgUnitEditText.setText(selectedOrgUnit!!.displayName())
        binding.dataSetOrgUnitEditText.isEnabled = false
    }

    override fun navigateToDataSetTable(catOptionCombo: String, periodId: String) {
        val bundle = DataSetTableActivity.getBundle(
            dataSetUid,
            selectedOrgUnit!!.uid(),
            selectedOrgUnit!!.name()!!,
            binding.dataSetModel!!.periodType().name,
            DateUtils.getInstance().getPeriodUIString(
                binding.dataSetModel!!.periodType(),
                selectedPeriod,
                Locale.getDefault()
            ),
            periodId,
            catOptionCombo
        )

        startActivity(DataSetTableActivity::class.java, bundle, true, false, null)
    }

    private fun checkActionVisivbility() {
        val visible = when {
            selectedOrgUnit == null -> false
            selectedPeriod == null -> false
            selectedCatOptionsMap?.values?.any { it == null } ?: true -> false
            else -> true
        }

        binding.actionButton.visibility = if (visible) View.VISIBLE else View.GONE
    }
}
