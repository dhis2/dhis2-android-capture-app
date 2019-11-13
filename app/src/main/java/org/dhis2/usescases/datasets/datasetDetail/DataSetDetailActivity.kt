/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.dhis2.usescases.datasets.datasetDetail

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.R
import org.dhis2.data.tuples.Pair
import org.dhis2.databinding.ActivityDatasetDetailBinding
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.orgunitselector.OUTreeActivity
import org.dhis2.utils.Constants
import org.dhis2.utils.DateUtils
import org.dhis2.utils.filters.FilterManager
import org.dhis2.utils.filters.FiltersAdapter
import org.dhis2.utils.granularsync.GranularSyncContracts
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo

class DataSetDetailActivity : ActivityGlobalAbstract(), DataSetDetailView {

    @Inject
    lateinit var presenter: DataSetDetailPresenter
    @Inject
    lateinit var filterManager: FilterManager

    private lateinit var binding: ActivityDatasetDetailBinding
    private lateinit var adapter: DataSetDetailAdapter
    private lateinit var filtersAdapter: FiltersAdapter

    private lateinit var dataSetUid: String
    private var accessDataWrite: Boolean = false
    private var backDropActive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        dataSetUid = intent.getStringExtra(Constants.DATASET_UID)
        (applicationContext as App).userComponent()!!.plus(
            DataSetDetailModule(this, dataSetUid)
        ).inject(this)
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dataset_detail)
        accessDataWrite = intent.getStringExtra(Constants.ACCESS_DATA)?.toBoolean() ?: false
        adapter = DataSetDetailAdapter(presenter)
        filtersAdapter = FiltersAdapter()

        binding.apply {
            name = intent.getStringExtra(Constants.DATA_SET_NAME)
            presenter = this@DataSetDetailActivity.presenter
            filterLayout.adapter = filtersAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
        binding.apply {
            addDatasetButton.isEnabled = true
            totalFilters = filterManager.totalFilters
        }
        filtersAdapter.notifyDataSetChanged()
    }

    override fun onPause() {
        presenter.onDettach()
        super.onPause()
    }

    override fun setData(dataSets: List<DataSetDetailModel>) {
        binding.programProgress.visibility = View.GONE
        if (binding.recycler.adapter == null) {
            binding.apply {
                recycler.adapter = adapter
                recycler.addItemDecoration(
                    DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                )
            }
        }
        if (dataSets.isEmpty()) {
            binding.emptyTeis.visibility = View.VISIBLE
            binding.recycler.visibility = View.GONE
        } else {
            binding.emptyTeis.visibility = View.GONE
            binding.recycler.visibility = View.VISIBLE
            adapter.setDataSets(dataSets)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FilterManager.OU_TREE && resultCode == Activity.RESULT_OK) {
            filtersAdapter.notifyDataSetChanged()
            updateFilters(filterManager.totalFilters)
        }
        super.onActivityResult(requestCode, resultCode, data)
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
                R.id.eventsLayout,
                ConstraintSet.TOP,
                R.id.filterLayout,
                ConstraintSet.BOTTOM,
                50
            )
        } else {
            initSet.connect(
                R.id.eventsLayout,
                ConstraintSet.TOP,
                R.id.backdropGuideTop,
                ConstraintSet.BOTTOM,
                0
            )
        }
        initSet.applyTo(binding.backdropLayout)

        binding.filterOpen.visibility = if (backDropActive) View.VISIBLE else View.GONE
    }

    override fun clearFilters() {
        filtersAdapter.notifyDataSetChanged()
    }

    override fun updateFilters(totalFilters: Int) {
        binding.totalFilters = totalFilters
        binding.executePendingBindings()
    }

    override fun openOrgUnitTreeSelector() {
        val ouTreeIntent = Intent(this, OUTreeActivity::class.java)
        startActivityForResult(ouTreeIntent, FilterManager.OU_TREE)
    }

    override fun showPeriodRequest(periodRequest: FilterManager.PeriodRequest) {
        if (periodRequest == FilterManager.PeriodRequest.FROM_TO) {
            DateUtils.getInstance().showFromToSelector(this) {
                filterManager.addPeriod(it)
            }
        } else {
            DateUtils.getInstance().showPeriodDialog(
                this, { datePeriods -> filterManager.addPeriod(datePeriods) },
                true
            )
        }
    }

    override fun setCatOptionComboFilter(
        categoryOptionCombos: Pair<CategoryCombo, List<CategoryOptionCombo>>
    ) {
        filtersAdapter.addCatOptCombFilter(categoryOptionCombos)
    }

    @SuppressLint("RestrictedApi")
    override fun setWritePermission(canWrite: Boolean) {
        binding.addDatasetButton.visibility = if (canWrite) View.VISIBLE else View.GONE
    }

    override fun startNewDataSet() {
        binding.addDatasetButton.isEnabled = false
        val bundle = Bundle()
        bundle.putString(Constants.DATA_SET_UID, dataSetUid)
        startActivity(DataSetInitialActivity::class.java, bundle, false, false, null)
    }

    override fun openDataSet(dataSet: DataSetDetailModel) {
        val bundle = Bundle()
        bundle.putString(Constants.ORG_UNIT, dataSet.orgUnitUid())
        bundle.putString(Constants.ORG_UNIT_NAME, dataSet.nameOrgUnit())
        bundle.putString(Constants.PERIOD_ID, dataSet.periodId())
        bundle.putString(Constants.PERIOD_TYPE, dataSet.periodType())
        bundle.putString(Constants.PERIOD_TYPE_DATE, dataSet.namePeriod())
        bundle.putString(Constants.CAT_COMB, dataSet.catOptionComboUid())
        bundle.putString(Constants.DATA_SET_UID, dataSetUid)
        bundle.putBoolean(Constants.ACCESS_DATA, accessDataWrite)
        startActivity(DataSetTableActivity::class.java, bundle, false, false, null)
    }

    override fun showSyncDialog(dataSet: DataSetDetailModel) {
        val dialog = SyncStatusDialog.Builder()
            .setConflictType(SyncStatusDialog.ConflictType.DATA_VALUES)
            .setUid(dataSetUid)
            .setOrgUnit(dataSet.orgUnitUid())
            .setAttributeOptionCombo(dataSet.catOptionComboUid())
            .setPeriodId(dataSet.periodId())
            .onDismissListener(object : GranularSyncContracts.OnDismissListener {
                override fun onDismiss(hasChanged: Boolean) {
                    if (hasChanged) {
                        presenter.updateFilters()
                    }
                }
            })
            .build()

        dialog.show(supportFragmentManager, dialog.dialogTag)
    }
}
