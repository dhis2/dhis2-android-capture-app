package org.dhis2.usescases.main.program

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import org.dhis2.App
import org.dhis2.R
import org.dhis2.android.rtsm.commons.Constants.INTENT_EXTRA_APP_CONFIG
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.ui.home.HomeActivity
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.SyncContext
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.main.navigateTo
import org.dhis2.usescases.main.toHomeItemData
import org.dhis2.utils.HelpManager
import org.dhis2.utils.analytics.SELECT_PROGRAM
import org.dhis2.utils.analytics.TYPE_PROGRAM_SELECTED
import org.dhis2.utils.granularsync.SyncStatusDialog
import timber.log.Timber
import javax.inject.Inject

class ProgramFragment : FragmentGlobalAbstract(), ProgramView {

    @Inject
    lateinit var programViewModelFactory: ProgramViewModelFactory

    val programViewModel: ProgramViewModel by viewModels {
        programViewModelFactory
    }

    @Inject
    lateinit var animation: ProgramAnimation

    private val getActivityContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
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
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                val items by programViewModel.programs.observeAsState()
                val state by programViewModel.downloadState().observeAsState()
                val downloadState by programViewModel.downloadState().observeAsState()
                LaunchedEffect(downloadState) {
                    programViewModel.setIsDownloading()
                }
                ProgramList(
                    downLoadState = state,
                    programs = items,
                    onItemClick = {
                        programViewModel.onItemClick(it)
                    },
                    onGranularSyncClick = {
                        showSyncDialog(it)
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        programViewModel.init()
    }

    //endregion

    override fun setTutorial() {
        try {
            if (context != null && isAdded) {
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        if (abstractActivity != null) {
                            val stepCondition = SparseBooleanArray()
                            stepCondition.put(
                                7,
                                (programViewModel.programs.value?.size ?: 0) > 0,
                            )
                            HelpManager.getInstance().show(
                                abstractActivity,
                                HelpManager.TutorialName.PROGRAM_FRAGMENT,
                                stepCondition,
                            )
                        }
                    },
                    500,
                )
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun navigateTo(program: ProgramUiModel) {
        abstractActivity.analyticsHelper.setEvent(
            TYPE_PROGRAM_SELECTED,
            program.programType.ifEmpty { program.typeName },
            SELECT_PROGRAM,
        )

        getActivityContent.navigateTo(
            requireContext(),
            program.toHomeItemData(),
        )
    }

    override fun navigateToStockManagement(config: AppConfig) {
        Intent(activity, HomeActivity::class.java).apply {
            putExtra(INTENT_EXTRA_APP_CONFIG, config)
            getActivityContent.launch(this)
        }
    }

    override fun showSyncDialog(program: ProgramUiModel) {
        SyncStatusDialog.Builder()
            .withContext(this)
            .withSyncContext(
                when (program.programType) {
                    "WITH_REGISTRATION" -> SyncContext.GlobalTrackerProgram(program.uid)
                    "WITHOUT_REGISTRATION" -> SyncContext.GlobalEventProgram(program.uid)
                    else -> SyncContext.GlobalDataSet(program.uid)
                },
            )
            .onDismissListener(
                object : OnDismissListener {
                    override fun onDismiss(hasChanged: Boolean) {
                        if (hasChanged) {
                            programViewModel.updateProgramQueries()
                        }
                    }
                },
            )
            .onNoConnectionListener {
                val contextView = activity?.findViewById<View>(R.id.navigationBar)
                Snackbar.make(
                    contextView!!,
                    R.string.sync_offline_check_connection,
                    Snackbar.LENGTH_SHORT,
                ).show()
            }
            .show(FRAGMENT_TAG)
    }

    companion object {
        const val FRAGMENT_TAG = "SYNC"
    }
}
