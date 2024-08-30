package org.dhis2.usescases.programEventDetail.eventList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import org.dhis2.commons.filters.workingLists.WorkingListViewModel
import org.dhis2.commons.filters.workingLists.WorkingListViewModelFactory
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailViewModel
import org.dhis2.usescases.programEventDetail.eventList.ui.mapper.EventCardMapper
import javax.inject.Inject

class EventListFragment : FragmentGlobalAbstract() {

    @Inject
    lateinit var eventListViewModelFactory: EventListPresenterFactory

    @Inject
    lateinit var workingListViewModelFactory: WorkingListViewModelFactory

    @Inject
    lateinit var cardMapper: EventCardMapper

    val eventListViewModel by viewModels<EventListViewModel> { eventListViewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        (activity as ProgramEventDetailActivity).component
            ?.plus(EventListModule())
            ?.inject(this)

        val programEventsViewModel by activityViewModels<ProgramEventDetailViewModel>()

        eventListViewModel.onSyncClickedListener = { eventUid ->
            eventUid?.let { programEventsViewModel.eventSyncClicked.value = it }
        }

        eventListViewModel.onCardClickedListener = { eventUid, orgUnitUid ->
            programEventsViewModel.eventClicked.value = Pair(eventUid, orgUnitUid)
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val workingListViewModel by viewModels<WorkingListViewModel> { workingListViewModelFactory }
                EventListScreen(
                    eventListViewModel,
                    workingListViewModel,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        programEventsViewModel.setProgress(true)
        presenter.init()
        eventListViewModel.refreshData()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.clear()
    }

    override fun setLiveData(pagedListLiveData: LiveData<PagedList<EventViewModel>>) {
        if (isUserLoggedIn()) {
            liveDataList?.removeObservers(viewLifecycleOwner)
            this.liveDataList = pagedListLiveData
            liveDataList?.observe(viewLifecycleOwner) { pagedList: PagedList<EventViewModel> ->
                programEventsViewModel.setProgress(false)
                liveAdapter?.submitList(pagedList) {
                    if ((binding.recycler.adapter?.itemCount ?: 0) == 0) {
                        binding.emptyTeis.text = getString(R.string.empty_tei_add)
                        binding.emptyTeis.visibility = View.VISIBLE
                        binding.recycler.visibility = View.GONE
                    } else {
                        binding.emptyTeis.visibility = View.GONE
                        binding.recycler.visibility = View.VISIBLE
                    }
                    EventListIdlingResourceSingleton.decrement()
                }
            }
        }
    }

    private fun configureWorkingList() {
        binding.filterLayout.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                val workingListViewModel by viewModels<WorkingListViewModel> { workingListViewModelFactory }
                WorkingListChipGroup(Modifier.padding(top = Spacing.Spacing16), workingListViewModel)
            }
        }
    }
}
