package org.dhis2.usescases.datasets.dataSetTable.dataSetDetail

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.dhis2.Bindings.Bindings
import org.dhis2.Bindings.toDateSpan
import org.dhis2.R
import org.dhis2.databinding.FragmentDatasetDetailBinding
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.utils.ColorUtils
import org.dhis2.utils.DateUtils
import org.dhis2.utils.granularsync.GranularSyncContracts
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.dataset.DataSetInstance
import org.hisp.dhis.android.core.period.Period
import java.util.Locale
import javax.inject.Inject

const val DATASET_UID = "DATASET_UID"
const val DATASET_ACCESS = "DATASET_ACCESS"

class DataSetDetailFragment private constructor() : FragmentGlobalAbstract(), DataSetDetailView {

    private var dataSetInstance: DataSetInstance? = null
    private lateinit var dataSetUid: String
    private var accessWrite: Boolean = false
    private lateinit var binding: FragmentDatasetDetailBinding
    private lateinit var mContext: Context

    @Inject
    lateinit var presenter: DataSetDetailPresenter

    companion object {
        @JvmStatic
        fun create(dataSetUid: String, accessWrite: Boolean): DataSetDetailFragment {
            return DataSetDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(DATASET_UID, dataSetUid)
                    putBoolean(DATASET_ACCESS, accessWrite)
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context
        arguments?.let {
            dataSetUid = it.getString(DATASET_UID, "")
            accessWrite = it.getBoolean(DATASET_ACCESS)
        } ?: throw IllegalArgumentException("Arguments can't be null")
        (context as DataSetTableActivity).dataSetTableComponent.plus(
            DataSetDetailModule(
                this,
                dataSetUid
            )
        ).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDatasetDetailBinding.inflate(inflater, container, false)
        binding.syncStatus.setOnClickListener { openSyncDialog() }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
    }

    override fun onPause() {
        super.onPause()
        presenter.detach()
    }

    override fun openSyncDialog() {
        dataSetInstance?.let {
            val syncDialog = SyncStatusDialog.Builder()
                .setConflictType(SyncStatusDialog.ConflictType.DATA_VALUES)
                .setUid(dataSetUid)
                .setPeriodId(it.period())
                .setOrgUnit(it.organisationUnitUid())
                .setAttributeOptionCombo(it.attributeOptionComboUid())
                .onDismissListener(
                    object : GranularSyncContracts.OnDismissListener {
                        override fun onDismiss(hasChanged: Boolean) {
                            if (hasChanged) {
                                presenter.updateData()
                            }
                        }
                    })
                .build()
            syncDialog.show(abstractActivity.supportFragmentManager, syncDialog.dialogTag)
        }
    }

    override fun setCatOptComboName(catComboName: String) {
        binding.catCombo.text = catComboName
    }

    override fun setDataSetDetails(
        dataSetInstance: DataSetInstance,
        period: Period
    ) {
        this.dataSetInstance = dataSetInstance;
        binding.apply {
            dataSetName.text = dataSetInstance.dataSetDisplayName()
            if (dataSetInstance.completed()) {
                completedDate.visibility = View.VISIBLE
                completedDate.text = String.format(
                    "%s %s",
                    getString(R.string.data_set_closed),
                    dataSetInstance.completionDate().toDateSpan(mContext)
                )
                dataSetStatus.setText(R.string.data_set_closed)
            } else {
                dataSetStatus.setText(R.string.data_set_open)
                completedDate.visibility = View.GONE
                dataSetStatus.background = ColorUtils.tintDrawableWithColor(
                    dataSetStatus.background,
                    Color.parseColor("#CCFF90")
                )
            }
            Bindings.setStateIcon(binding.syncStatus, dataSetInstance.state())
            binding.dataSetPeriod.text = DateUtils.getInstance()
                .getPeriodUIString(
                    period.periodType(),
                    period.startDate(),
                    Locale.getDefault()
                )
            binding.dataSetOrgUnit.text = dataSetInstance.organisationUnitDisplayName()
            binding.dataSetCatCombo.text = dataSetInstance.attributeOptionComboDisplayName()
        }
    }

    override fun hideCatOptCombo() {
        binding.catCombo.visibility = View.GONE
        binding.dataSetCatCombo.visibility = View.GONE
    }

    override fun setStyle(style: ObjectStyle?) {
        val color = ColorUtils.getColorFrom(
            style?.color(),
            ColorUtils.getPrimaryColor(
                mContext,
                ColorUtils.ColorType.PRIMARY
            )
        )
        binding.dataSetIcon.background = ColorUtils.tintDrawableWithColor(
            binding.dataSetIcon.background,
            color
        )
        binding.dataSetIcon.setImageResource(
            ResourceManager(mContext).getObjectStyleDrawableResource(
                style?.icon(),
                R.drawable.ic_program_default
            )
        )
        binding.dataSetIcon.setColorFilter(ColorUtils.getContrastColor(color))
    }


}