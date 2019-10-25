package org.dhis2.usescases.main.program

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import org.dhis2.App
import org.dhis2.R
import org.dhis2.databinding.FragmentProgramBinding
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.orgunitselector.OUTreeActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.utils.Constants
import org.dhis2.utils.HelpManager
import org.dhis2.utils.analytics.SELECT_PROGRAM
import org.dhis2.utils.analytics.TYPE_PROGRAM_SELECTED
import org.dhis2.utils.filters.FilterManager
import org.dhis2.utils.granularsync.GranularSyncContracts
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.hisp.dhis.android.core.program.ProgramType
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by ppajuelo on 18/10/2017.f
 */

class ProgramFragment : FragmentGlobalAbstract(), ProgramView {

    private lateinit var binding: FragmentProgramBinding

    @Inject
    lateinit var presenter: ProgramPresenter
    @Inject
    lateinit var adapter: ProgramModelAdapter

    // -------------------------------------------
    //region LIFECYCLE

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            (it.applicationContext as App).userComponent()?.plus(ProgramModule(this))?.inject(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_program, container, false)

        return binding.apply {
            presenter = this@ProgramFragment.presenter
            programRecycler.adapter = adapter
            programRecycler.addItemDecoration(
                DividerItemDecoration(
                    context,
                    DividerItemDecoration.VERTICAL
                )
            )
        }.root
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
    }

    override fun onPause() {
        super.onPause()
        presenter.dispose()
    }

    //endregion

    override fun swapProgramModelData(programs: List<ProgramViewModel>) {
        binding.progressLayout.visibility = View.GONE
        binding.emptyView.visibility = if (programs.isEmpty()) View.VISIBLE else View.GONE
        (binding.programRecycler.adapter as ProgramModelAdapter).setData(programs)
    }

    override fun showFilterProgress() {
        binding.progressLayout.visibility = View.VISIBLE
    }

    override fun renderError(message: String) {
        if (isAdded && activity != null) {
            AlertDialog.Builder(activity!!)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(getString(R.string.error))
                .setMessage(message)
                .show()
        }
    }

    override fun openOrgUnitTreeSelector() {
        val ouTreeIntent = Intent(context, OUTreeActivity::class.java)
        (context as MainActivity).startActivityForResult(ouTreeIntent, FilterManager.OU_TREE)
    }

    override fun setTutorial() {
        try {
            if (context != null && isAdded) {
                Handler().postDelayed(
                    {
                        if (abstractActivity != null) {
                            val stepCondition = SparseBooleanArray()
                            stepCondition.put(
                                7,
                                binding.programRecycler.adapter!!.itemCount > 0
                            )
                            HelpManager.getInstance().show(
                                abstractActivity,
                                HelpManager.TutorialName.PROGRAM_FRAGMENT,
                                stepCondition
                            )
                        }
                    },
                    500
                )
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun openFilter(open: Boolean) {
        binding.filter.visibility = if (open) View.VISIBLE else View.GONE
    }

    override fun showHideFilter() {
        (activity as MainActivity).showHideFilter()
    }

    override fun clearFilters() {
        (activity as MainActivity).adapter?.notifyDataSetChanged()
    }

    override fun navigateTo(program: ProgramViewModel) {
        val bundle = Bundle()
        val idTag = if (program.typeName() == "DataSets") {
            "DATASET_UID"
        } else {
            "PROGRAM_UID"
        }

        if (!TextUtils.isEmpty(program.type())) {
            bundle.putString("TRACKED_ENTITY_UID", program.type())
        }

        abstractActivity.analyticsHelper.setEvent(
            TYPE_PROGRAM_SELECTED,
            if (program.programType().isNotEmpty()) {
                program.programType()
            } else {
                program.typeName()
            },
            SELECT_PROGRAM
        )
        bundle.putString(idTag, program.id())
        bundle.putString(Constants.DATA_SET_NAME, program.title())
        bundle.putString(
            Constants.ACCESS_DATA,
            java.lang.Boolean.toString(program.accessDataWrite())
        )

        when (program.programType()) {
            ProgramType.WITH_REGISTRATION.name ->
                startActivity(SearchTEActivity::class.java, bundle, false, false, null)
            ProgramType.WITHOUT_REGISTRATION.name ->
                startActivity(
                    ProgramEventDetailActivity::class.java,
                    ProgramEventDetailActivity.getBundle(program.id()),
                    false, false, null
                )
            else -> startActivity(DataSetDetailActivity::class.java, bundle, false, false, null)
        }
    }

    override fun showSyncDialog(program: ProgramViewModel) {
        val dialog = SyncStatusDialog.Builder()
            .setConflictType(
                if (program.typeName() != "DataSets") {
                    SyncStatusDialog.ConflictType.PROGRAM
                } else {
                    SyncStatusDialog.ConflictType.DATA_SET
                }
            )
            .setUid(program.id())
            .onDismissListener(
                object : GranularSyncContracts.OnDismissListener {
                    override fun onDismiss(hasChanged: Boolean) {
                        if (hasChanged) {
                            presenter.updateProgramQueries()
                        }
                    }
                })
            .build()

        dialog.show(abstractActivity.supportFragmentManager, dialog.dialogTag)
    }
}
