package org.dhis2.usescases.main.program

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import io.noties.markwon.Markwon
import org.dhis2.App
import org.dhis2.R
import org.dhis2.android.rtsm.commons.Constants.INTENT_EXTRA_APP_CONFIG
import org.dhis2.android.rtsm.data.AppConfig
import org.dhis2.android.rtsm.ui.home.HomeActivity
import org.dhis2.bindings.Bindings
import org.dhis2.bindings.clipWithRoundedCorners
import org.dhis2.bindings.dp
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.SyncContext
import org.dhis2.databinding.FragmentProgramBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.notifications.domain.Notification
import org.dhis2.usescases.notifications.presentation.NotificationsPresenter
import org.dhis2.usescases.notifications.presentation.NotificationsView
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.main.navigateTo
import org.dhis2.usescases.main.toHomeItemData
import org.dhis2.utils.HelpManager
import org.dhis2.utils.analytics.SELECT_PROGRAM
import org.dhis2.utils.analytics.TYPE_PROGRAM_SELECTED
import org.dhis2.utils.granularsync.SyncStatusDialog
import timber.log.Timber
import javax.inject.Inject

class ProgramFragment : FragmentGlobalAbstract(), ProgramView,
    NotificationsView {

    private lateinit var binding: FragmentProgramBinding

    @Inject
    lateinit var presenter: ProgramPresenter

    @Inject
    lateinit var notificationsPresenter: NotificationsPresenter

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
            (it.applicationContext as App).userComponent()?.plus(ProgramModule(this, this))?.inject(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_program, container, false)
        ViewCompat.setTransitionName(binding.drawerLayout, "contenttest")
        binding.lifecycleOwner = viewLifecycleOwner
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
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                val items by presenter.programs().observeAsState(emptyList())
                val state by presenter.downloadState().observeAsState()
                ProgramList(
                    downLoadState = state,
                    programs = items,
                    onItemClick = {
                        presenter.onItemClick(it)
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
        presenter.init()
        animation.initBackdropCorners(
            binding.drawerLayout.background.mutate() as GradientDrawable,
        )

        notificationsPresenter.refresh()
    }

    override fun onPause() {
        animation.reverseBackdropCorners(
            binding.drawerLayout.background.mutate() as GradientDrawable,
        )
        presenter.dispose()
        super.onPause()
    }

    //endregion

    override fun swapProgramModelData(programs: List<ProgramViewModel>) {
        binding.emptyView.visibility = if (programs.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun showFilterProgress() {
        Bindings.setViewVisibility(
            binding.clearFilter,
            FilterManager.getInstance().totalFilters > 0,
        )
    }

    override fun openOrgUnitTreeSelector() {
        OUTreeFragment.Builder()
            .showAsDialog()
            .withPreselectedOrgUnits(
                FilterManager.getInstance().orgUnitFilters.map { it.uid() }.toMutableList(),
            )
            .onSelection { selectedOrgUnits ->
                presenter.setOrgUnitFilters(selectedOrgUnits)
            }
            .build()
            .show(childFragmentManager, "OUTreeFragment")
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
                                presenter.programs().value?.size ?: 0 > 0,
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

    override fun showSyncDialog(program: ProgramViewModel) {
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
                            presenter.updateProgramQueries()
                        }
                    }
                },
            ).show(FRAGMENT_TAG)
    }

    fun sharedView() = binding.drawerLayout

    companion object {
        const val FRAGMENT_TAG = "SYNC"
    }

    override fun renderNotifications(notifications: List<Notification>) {
        notifications.forEach(::showNotification)
    }

    private fun showNotification(notification: Notification) {
        val markwon = Markwon.create(requireActivity())
        val content = markwon.toMarkdown(notification.content)

        AlertDialog.Builder(context, R.style.CustomDialog)
            .setTitle("Notification")
            .setMessage(content)
            .setPositiveButton(getString(R.string.wipe_data_ok)) { dialog, _ ->
                notificationsPresenter.markNotificationAsRead(notification)
            }
            .setCancelable(true)
            .show()
    }
}
