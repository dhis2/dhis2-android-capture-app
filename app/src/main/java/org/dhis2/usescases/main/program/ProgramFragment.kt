package org.dhis2.usescases.main.program

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.Bindings.Bindings
import org.dhis2.Bindings.clipWithRoundedCorners
import org.dhis2.Bindings.dp
import org.dhis2.R
import org.dhis2.android.rtsm.commons.Constants.INTENT_EXTRA_APP_CONFIG
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.ui.home.HomeActivity
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.orgunitselector.OnOrgUnitSelectionFinished
import org.dhis2.databinding.FragmentProgramBinding
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailActivity
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.utils.Constants
import org.dhis2.utils.HelpManager
import org.dhis2.utils.analytics.SELECT_PROGRAM
import org.dhis2.utils.analytics.TYPE_PROGRAM_SELECTED
import org.dhis2.utils.granularsync.GranularSyncContracts
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.ProgramType
import timber.log.Timber

class ProgramFragment : FragmentGlobalAbstract(), ProgramView, OnOrgUnitSelectionFinished {

    private lateinit var binding: FragmentProgramBinding

    @Inject
    lateinit var presenter: ProgramPresenter

    @Inject
    lateinit var animation: ProgramAnimation

    private var hasToShowProgressLoading = true

    private val getActivityContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            hasToShowProgressLoading = false
        }

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
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_program, container, false)
        ViewCompat.setTransitionName(binding.drawerLayout, "contenttest")
        binding.lifecycleOwner = this
        (binding.drawerLayout.background as GradientDrawable).cornerRadius = 0f
        return binding.apply {
            presenter = this@ProgramFragment.presenter
            drawerLayout.clipWithRoundedCorners(16.dp)
            initList()
        }.also {
            presenter.downloadState().observe(viewLifecycleOwner) {
                presenter.setIsDownloading()
            }
        }.root
    }

    private fun initList() {
        binding.programList.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                val items by presenter.programs().observeAsState(emptyList())
                ProgramList(
                    programs = items,
                    onItemClick = {
                        presenter.onItemClick(it)
                    },
                    onGranularSyncClick = {
                        showSyncDialog(it)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
        animation.initBackdropCorners(
            binding.drawerLayout.background.mutate() as GradientDrawable
        )
    }

    override fun onPause() {
        animation.reverseBackdropCorners(
            binding.drawerLayout.background.mutate() as GradientDrawable
        )
        presenter.dispose()
        super.onPause()
    }

    //endregion

    override fun swapProgramModelData(programs: List<ProgramViewModel>) {
        binding.progressLayout.visibility = View.GONE
        binding.emptyView.visibility = if (programs.isEmpty()) View.VISIBLE else View.GONE
        if (!hasToShowProgressLoading) hasToShowProgressLoading = true
    }

    override fun showFilterProgress() {
        if (hasToShowProgressLoading) {
            binding.progressLayout.visibility = View.VISIBLE
        }
        Bindings.setViewVisibility(
            binding.clearFilter,
            FilterManager.getInstance().totalFilters > 0
        )
    }

    override fun renderError(message: String) {
        if (isAdded && activity != null) {
            AlertDialog.Builder(requireActivity())
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(getString(R.string.error))
                .setMessage(message)
                .show()
        }
    }

    override fun openOrgUnitTreeSelector() {
        OUTreeFragment.newInstance(
            true,
            FilterManager.getInstance().orgUnitFilters.map { it.uid() }.toMutableList()
        ).apply {
            selectionCallback = this@ProgramFragment
        }.show(childFragmentManager, "OUTreeFragment")
    }

    override fun onSelectionFinished(selectedOrgUnits: List<OrganisationUnit>) {
        presenter.setOrgUnitFilters(selectedOrgUnits)
    }

    override fun setTutorial() {
        try {
            if (context != null && isAdded) {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        if (abstractActivity != null) {
                            val stepCondition = SparseBooleanArray()
                            stepCondition.put(
                                7,
                                presenter.programs().value?.size ?: 0 > 0
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
        (activity as MainActivity).newAdapter.notifyDataSetChanged()
    }

    override fun navigateTo(program: ProgramViewModel) {
        val bundle = Bundle()
        val idTag = if (program.programType.isEmpty()) {
            Constants.DATASET_UID
        } else {
            Constants.PROGRAM_UID
        }

        if (!TextUtils.isEmpty(program.type)) {
            bundle.putString(Constants.TRACKED_ENTITY_UID, program.type)
        }

        abstractActivity.analyticsHelper.setEvent(
            TYPE_PROGRAM_SELECTED,
            if (program.programType.isNotEmpty()) {
                program.programType
            } else {
                program.typeName
            },
            SELECT_PROGRAM
        )
        bundle.putString(idTag, program.uid)
        bundle.putString(Constants.DATA_SET_NAME, program.title)
        bundle.putString(
            Constants.ACCESS_DATA,
            program.accessDataWrite.toString()
        )

        val appConfig = AppConfig(
            "F5ijs28K4s8",
            "wBr4wccNBj1",
            "MBczRWvfM46",
            "ypCQAFr1a5l",
            "yfsEseIcEXr",
            "lpGYJoVUudr",
            "ej1YwWaYGmm",
            "I7cmT3iXT0y"
        )

        when (program.programType) {
            ProgramType.WITH_REGISTRATION.name -> {
                Intent(activity, HomeActivity::class.java).apply {
                    putExtras(bundle)
                    putExtra(INTENT_EXTRA_APP_CONFIG, appConfig)
                    getActivityContent.launch(this)
                }
            }
            ProgramType.WITHOUT_REGISTRATION.name -> {
                Intent(activity, ProgramEventDetailActivity::class.java).apply {
                    putExtras(ProgramEventDetailActivity.getBundle(program.uid))
                    getActivityContent.launch(this)
                }
            }
            else -> {
                Intent(activity, DataSetDetailActivity::class.java).apply {
                    putExtras(bundle)
                    getActivityContent.launch(this)
                }
            }
        }
    }

    override fun showSyncDialog(program: ProgramViewModel) {
        val dialog = SyncStatusDialog.Builder()
            .setConflictType(
                if (program.programType.isNotEmpty()) {
                    SyncStatusDialog.ConflictType.PROGRAM
                } else {
                    SyncStatusDialog.ConflictType.DATA_SET
                }
            )
            .setUid(program.uid)
            .onDismissListener(
                object : GranularSyncContracts.OnDismissListener {
                    override fun onDismiss(hasChanged: Boolean) {
                        if (hasChanged) {
                            presenter.updateProgramQueries()
                        }
                    }
                })
            .build()

        dialog.show(abstractActivity.supportFragmentManager, FRAGMENT_TAG)
    }

    fun sharedView() = binding.drawerLayout

    companion object {
        const val FRAGMENT_TAG = "SYNC"
    }
}
