package org.dhis2.usescases.programEventDetail.eventList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        (activity as ProgramEventDetailActivity).component
            ?.plus(EventListModule())
            ?.inject(this)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val workingListViewModel by viewModels<WorkingListViewModel> { workingListViewModelFactory }
                val eventListViewModel by viewModels<EventListViewModel> { eventListViewModelFactory }
                val programEventsViewModel by activityViewModels<ProgramEventDetailViewModel>()
                val cardClicked by eventListViewModel.onEventCardClick.collectAsState(null)
                val syncClicked by eventListViewModel.onSyncClick.collectAsState(null)

                LaunchedEffect(key1 = cardClicked) {
                    cardClicked?.let {
                        programEventsViewModel.eventClicked.value = it
                    }
                }

                LaunchedEffect(key1 = syncClicked) {
                    programEventsViewModel.eventSyncClicked.value = syncClicked
                }

                EventListScreen(
                    eventListViewModel,
                    workingListViewModel,
                )
            }
        }
    }
}
