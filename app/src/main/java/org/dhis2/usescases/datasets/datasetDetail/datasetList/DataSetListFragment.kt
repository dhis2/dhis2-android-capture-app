package org.dhis2.usescases.datasets.datasetDetail.datasetList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.SyncContext
import org.dhis2.databinding.FragmentDataSetListBinding
import org.dhis2.mobile.aggregates.ui.constants.INTENT_EXTRA_ATTRIBUTE_OPTION_COMBO_UID
import org.dhis2.mobile.aggregates.ui.constants.INTENT_EXTRA_DATA_SET_UID
import org.dhis2.mobile.aggregates.ui.constants.INTENT_EXTRA_ORGANISATION_UNIT_UID
import org.dhis2.mobile.aggregates.ui.constants.INTENT_EXTRA_PERIOD_ID
import org.dhis2.usescases.datasets.dataSetTable.DataSetInstanceActivity
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailModel
import org.dhis2.usescases.datasets.datasetDetail.datasetList.mapper.DatasetCardMapper
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialActivity
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.utils.ActionObserver
import org.dhis2.utils.granularsync.SyncStatusDialog
import javax.inject.Inject

class DataSetListFragment : FragmentGlobalAbstract() {
    private lateinit var dataSetUid: String
    private var accessWriteData: Boolean = false

    private lateinit var binding: FragmentDataSetListBinding
    var adapter: DataSetListAdapter? = null

    private lateinit var activity: DataSetDetailActivity

    @Inject
    lateinit var viewModelFactory: DataSetListViewModelFactory

    @Inject
    lateinit var datasetCardMapper: DatasetCardMapper

    @Inject
    lateinit var featureConfig: FeatureConfigRepository

    private val viewModel: DataSetListViewModel by viewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        activity = requireActivity() as DataSetDetailActivity
        activity.dataSetDetailComponent.plus(DataSetListModule()).inject(this)
        adapter = DataSetListAdapter(viewModel, datasetCardMapper)

        with(viewModel) {
            datasets.observe(viewLifecycleOwner) { setData(it) }
            canWrite.observe(viewLifecycleOwner) { setWritePermission(it) }
            selectedDataset.observe(viewLifecycleOwner, ActionObserver { startDataSet(it) })
            selectedSync.observe(viewLifecycleOwner, ActionObserver { showSyncDialog(it) })
        }
        return FragmentDataSetListBinding
            .inflate(inflater, container, false)
            .apply {
                binding = this
                lifecycleOwner = viewLifecycleOwner
                recycler.adapter = adapter
                addDatasetButton.setOnClickListener { startNewDataSet() }
            }.root
    }

    override fun onResume() {
        super.onResume()
        binding.addDatasetButton.isEnabled = true
        activity.setProgress(true)
        viewModel.updateData()
    }

    private fun setData(datasets: List<DataSetDetailModel>) {
        activity.setProgress(false)
        if (binding.recycler.adapter == null) {
            binding.recycler.adapter = adapter
        }
        if (datasets.isEmpty()) {
            binding.emptyData.visibility = View.VISIBLE
            binding.recycler.visibility = View.GONE
        } else {
            binding.emptyData.visibility = View.GONE
            binding.recycler.visibility = View.VISIBLE
            adapter?.submitList(datasets)
        }
    }

    private fun setWritePermission(canWrite: Boolean) {
        binding.emptyData.text =
            when {
                canWrite -> getString(R.string.dataset_empty_list_can_create)
                else -> getString(R.string.dataset_emtpy_list_can_not_create)
            }
        binding.addDatasetButton.visibility =
            when {
                canWrite -> View.VISIBLE
                else -> View.GONE
            }
    }

    private fun startNewDataSet() {
        binding.addDatasetButton.isEnabled = false
        val bundle = Bundle()
        bundle.putString(Constants.DATA_SET_UID, dataSetUid)
        startActivity(DataSetInitialActivity::class.java, bundle, false, false, null)
    }

    private fun startDataSet(dataSet: DataSetDetailModel) {
        val activityBundle =
            Bundle().apply {
                putString(INTENT_EXTRA_DATA_SET_UID, dataSet.datasetUid)
                putString(INTENT_EXTRA_PERIOD_ID, dataSet.periodId)
                putString(INTENT_EXTRA_ORGANISATION_UNIT_UID, dataSet.orgUnitUid)
                putString(INTENT_EXTRA_ATTRIBUTE_OPTION_COMBO_UID, dataSet.catOptionComboUid)
            }
        startActivity(DataSetInstanceActivity::class.java, activityBundle, false, false, null)
    }

    private fun showSyncDialog(dataSet: DataSetDetailModel) {
        SyncStatusDialog
            .Builder()
            .withContext(this)
            .withSyncContext(
                SyncContext.DataSetInstance(
                    dataSetUid = dataSetUid,
                    periodId = dataSet.periodId,
                    orgUnitUid = dataSet.orgUnitUid,
                    attributeOptionComboUid = dataSet.catOptionComboUid,
                ),
            ).onDismissListener(
                object : OnDismissListener {
                    override fun onDismiss(hasChanged: Boolean) {
                        if (hasChanged) {
                            viewModel.updateData()
                        }
                    }
                },
            ).onNoConnectionListener {
                val contextView = activity.findViewById<View>(R.id.navigationBar)
                Snackbar
                    .make(
                        contextView,
                        R.string.sync_offline_check_connection,
                        Snackbar.LENGTH_SHORT,
                    ).show()
            }.show(FRAGMENT_TAG)
    }

    companion object {
        private const val FRAGMENT_TAG = "SYNC"

        @JvmStatic
        fun newInstance(
            dataSetUid: String,
            accessWriteData: Boolean,
        ): DataSetListFragment =
            DataSetListFragment().apply {
                this.dataSetUid = dataSetUid
                this.accessWriteData = accessWriteData
            }
    }
}
