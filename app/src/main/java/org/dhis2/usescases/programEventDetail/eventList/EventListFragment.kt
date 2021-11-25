package org.dhis2.usescases.programEventDetail.eventList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.test.espresso.idling.concurrent.IdlingThreadPoolExecutor
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.dhis2.R
import org.dhis2.databinding.FragmentProgramEventDetailListBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailLiveAdapter
import org.dhis2.usescases.programEventDetail.ProgramEventDetailViewModel
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventViewModel

class EventListFragment : FragmentGlobalAbstract(), EventListFragmentView {

    lateinit var binding: FragmentProgramEventDetailListBinding
    private var liveAdapter: ProgramEventDetailLiveAdapter? = null
    private val programEventsViewModel by lazy {
        ViewModelProviders.of(requireActivity())[ProgramEventDetailViewModel::class.java]
    }

    @Inject
    lateinit var presenter: EventListPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as ProgramEventDetailActivity).component.plus(EventListModule(this)).inject(this)
        programEventsViewModel.setProgress(true)

        val bgThreadPoolExecutor = IdlingThreadPoolExecutor(
            "DiffExecutor",
            2,
            2,
            0L,
            TimeUnit.MILLISECONDS,
            LinkedBlockingQueue(),
            Executors.defaultThreadFactory()
        )

        val config = AsyncDifferConfig.Builder(ProgramEventDetailLiveAdapter.getDiffCallback())
            .setBackgroundThreadExecutor(bgThreadPoolExecutor)
            .build()

        liveAdapter =
            ProgramEventDetailLiveAdapter(presenter.program(), programEventsViewModel, config)
        return FragmentProgramEventDetailListBinding.inflate(inflater, container, false)
            .apply {
                binding = this
                recycler.adapter = liveAdapter
            }.root
    }

    override fun onResume() {
        super.onResume()
        programEventsViewModel.setProgress(true)
        presenter.init()
    }

    override fun setLiveData(pagedListLiveData: LiveData<PagedList<EventViewModel>>) {
        pagedListLiveData.observe(
            this,
            Observer<PagedList<EventViewModel>> { pagedList: PagedList<EventViewModel> ->
                programEventsViewModel.setProgress(false)
                liveAdapter?.submitList(pagedList) {
                    if (binding.recycler.adapter?.itemCount ?: 0 == 0) {
                        binding.emptyTeis.text = getString(R.string.empty_tei_add)
                        binding.emptyTeis.visibility = View.VISIBLE
                        binding.recycler.visibility = View.GONE
                    } else {
                        binding.emptyTeis.visibility = View.GONE
                        binding.recycler.visibility = View.VISIBLE
                    }
                    //   CountingIdlingResourceSingleton.decrement()
                }
            }
        )
    }
}
