package org.dhis2.usescases.datasets.datasetDetail.datasetList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import javax.inject.Inject
import org.dhis2.R
import org.dhis2.commons.Constants
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.SyncContext
import org.dhis2.databinding.FragmentDataSetListBinding
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailModel
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialActivity
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.utils.ActionObserver
import org.dhis2.utils.granularsync.SyncStatusDialog

class DataSetListFragment : FragmentGlobalAbstract() {

    private lateinit var dataSetUid: String
    private var accessWriteData: Boolean = false

    private lateinit var binding: FragmentDataSetListBinding
    var adapter: DataSetListAdapter? = null

    private lateinit var activity: DataSetDetailActivity

    @Inject
    lateinit var viewModelFactory: DataSetListViewModelFactory

    private val viewModel: DataSetListViewModel by viewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity = requireActivity() as DataSetDetailActivity
        activity.dataSetDetailComponent.plus(DataSetListModule()).inject(this)
        adapter = DataSetListAdapter(viewModel)

        with(viewModel) {
            datasets.observe(viewLifecycleOwner, { setData(it) })
            canWrite.observe(viewLifecycleOwner, { setWritePermission(it) })
            selectedDataset.observe(viewLifecycleOwner, ActionObserver { startDataSet(it) })
            selectedSync.observe(viewLifecycleOwner, ActionObserver { showSyncDialog(it) })
        }
        return FragmentDataSetListBinding.inflate(inflater, container, false)
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
        binding.emptyData.text = when {
            canWrite -> getString(R.string.dataset_empty_list_can_create)
            else -> getString(R.string.dataset_emtpy_list_can_not_create)
        }
        binding.addDatasetButton.visibility = when {
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
        val bundle = Bundle().apply {
            putString(Constants.ORG_UNIT, dataSet.orgUnitUid())
            putString(Constants.ORG_UNIT_NAME, dataSet.nameOrgUnit())
            putString(Constants.PERIOD_TYPE_DATE, dataSet.namePeriod())
            putString(Constants.PERIOD_TYPE, dataSet.periodType())
            putString(Constants.PERIOD_ID, dataSet.periodId())
            putString(Constants.CAT_COMB, dataSet.catOptionComboUid())
            putString(Constants.DATA_SET_UID, dataSetUid)
            putBoolean(Constants.ACCESS_DATA, accessWriteData)
        }
        startActivity(DataSetTableActivity::class.java, bundle, false, false, null)
    }

    private fun showSyncDialog(dataSet: DataSetDetailModel) {
        SyncStatusDialog.Builder()
            .withContext(this)
            .withSyncContext(
                SyncContext.DataSetInstance(
                    dataSetUid = dataSetUid,
                    periodId = dataSet.periodId(),
                    orgUnitUid = dataSet.orgUnitUid(),
                    attributeOptionComboUid = dataSet.catOptionComboUid()
                )
            ).onDismissListener(object : OnDismissListener {
                override fun onDismiss(hasChanged: Boolean) {
                    if (hasChanged) {
                        viewModel.updateData()
                    }
                }
            }).show(FRAGMENT_TAG)
    }

    companion object {
        private const val FRAGMENT_TAG = "SYNC"

        @JvmStatic
        fun newInstance(dataSetUid: String, accessWriteData: Boolean): DataSetListFragment {
            return DataSetListFragment().apply {
                this.dataSetUid = dataSetUid
                this.accessWriteData = accessWriteData
            }
        }
    }
}
