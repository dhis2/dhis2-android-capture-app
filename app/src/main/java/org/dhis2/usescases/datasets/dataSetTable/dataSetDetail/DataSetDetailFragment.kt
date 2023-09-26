package org.dhis2.usescases.datasets.dataSetTable.dataSetDetail

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.Flowable
import org.dhis2.R
import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.databinding.FragmentDatasetDetailBinding
import org.dhis2.ui.MetadataIconData
import org.dhis2.ui.setUpMetadataIcon
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.dataset.DataSetInstance
import org.hisp.dhis.android.core.period.Period
import org.hisp.dhis.android.core.period.PeriodType
import java.util.Date
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
    private lateinit var activity: DataSetTableActivity

    @Inject
    lateinit var presenter: DataSetDetailPresenter

    @Inject
    lateinit var periodUtils: DhisPeriodUtils

    @Inject
    lateinit var colorUtils: ColorUtils

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
        this.activity = context as DataSetTableActivity

        arguments?.let {
            dataSetUid = it.getString(DATASET_UID, "")
            accessWrite = it.getBoolean(DATASET_ACCESS)
        } ?: throw IllegalArgumentException("Arguments can't be null")
        context.dataSetTableComponent?.plus(
            DataSetDetailModule(
                this,
                dataSetUid,
            ),
        )?.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDatasetDetailBinding.inflate(inflater, container, false)
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

    override fun setCatOptComboName(catComboName: String) {
        binding.catCombo.text = catComboName
    }

    override fun setDataSetDetails(
        dataSetInstance: DataSetInstance,
        period: Period,
        isComplete: Boolean,
    ) {
        this.dataSetInstance = dataSetInstance
        binding.apply {
            dataSetName.text = dataSetInstance.dataSetDisplayName()
            if (isComplete) {
                completedDate.visibility = View.VISIBLE
                completedDate.text = String.format(
                    getString(R.string.completed_by),
                    dataSetInstance.completedBy() ?: "?",
                    dataSetInstance.completionDate().toDateSpan(mContext),
                )
                dataSetStatus.setText(R.string.data_set_complete)
            } else {
                dataSetStatus.setText(R.string.data_set_open)
                completedDate.visibility = View.GONE
                dataSetStatus.background = colorUtils.tintDrawableWithColor(
                    dataSetStatus.background,
                    Color.parseColor("#CCFF90"),
                )
            }
            lastUpdatedDate.text =
                String.format(
                    getString(R.string.updated_time),
                    dataSetInstance.lastUpdated().toDateSpan(mContext),
                )

            binding.dataSetPeriod.text = periodUtils
                .getPeriodUIString(
                    period.periodType() ?: PeriodType.Daily,
                    period.startDate() ?: Date(),
                    Locale.getDefault(),
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
        val color = colorUtils.getColorFrom(
            style?.color(),
            colorUtils.getPrimaryColor(
                mContext,
                ColorType.PRIMARY,
            ),
        )
        val imageResource = ResourceManager(mContext, colorUtils).getObjectStyleDrawableResource(
            style?.icon(),
            R.drawable.ic_default_outline,
        )

        binding.composeDataSetIcon.setUpMetadataIcon(
            MetadataIconData(
                programColor = color,
                iconResource = imageResource,
            ),
        )
    }

    override fun observeReopenChanges(): Flowable<Boolean> {
        return activity.observeReopenChanges()!!
    }
}
