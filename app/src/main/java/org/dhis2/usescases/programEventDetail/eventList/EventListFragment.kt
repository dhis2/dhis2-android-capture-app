package org.dhis2.usescases.programEventDetail.eventList

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import android.widget.AdapterView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.test.espresso.idling.concurrent.IdlingThreadPoolExecutor
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import org.dhis2.BuildConfig
import javax.inject.Inject
import org.dhis2.R
import org.dhis2.commons.animations.collapse
import org.dhis2.commons.animations.expand
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.filters.workingLists.WorkingListChipGroup
import org.dhis2.commons.filters.workingLists.WorkingListViewModel
import org.dhis2.commons.filters.workingLists.WorkingListViewModelFactory
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.FragmentProgramEventDetailListBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.programEventDetail.ProgramEventDetailActivity
import org.dhis2.usescases.programEventDetail.ProgramEventDetailLiveAdapter
import org.dhis2.usescases.programEventDetail.ProgramEventDetailViewModel
import org.dhis2.usescases.programEventDetail.eventList.ui.mapper.EventCardMapper
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.dhis2.utils.DataElementsAdapter
import org.hisp.dhis.android.core.dataelement.DataElement

class EventListFragment : FragmentGlobalAbstract(), EventListFragmentView {

    lateinit var binding: FragmentProgramEventDetailListBinding
    private var liveAdapter: ProgramEventDetailLiveAdapter? = null
    private val programEventsViewModel: ProgramEventDetailViewModel by activityViewModels()
    private var liveDataList: LiveData<PagedList<EventViewModel>>? = null

    @Inject
    lateinit var presenter: EventListPresenter

    @Inject
    lateinit var colorUtils: ColorUtils

    @Inject
    lateinit var cardMapper: EventCardMapper

    @Inject
    lateinit var workingListViewModelFactory: WorkingListViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        (activity as ProgramEventDetailActivity).component
            ?.plus(EventListModule(this))
            ?.inject(this)
        programEventsViewModel.setProgress(true)

        val bgThreadPoolExecutor = IdlingThreadPoolExecutor(
            "DiffExecutor",
            2,
            2,
            0L,
            TimeUnit.MILLISECONDS,
            LinkedBlockingQueue(),
            Executors.defaultThreadFactory(),
        )

        val config = AsyncDifferConfig.Builder(ProgramEventDetailLiveAdapter.diffCallback)
            .setBackgroundThreadExecutor(bgThreadPoolExecutor)
            .build()

        val program = presenter.program() ?: throw NullPointerException()
        liveAdapter =
            ProgramEventDetailLiveAdapter(
                program,
                programEventsViewModel,
                colorUtils,
                cardMapper,
                config,
            )
        return FragmentProgramEventDetailListBinding.inflate(inflater, container, false)
            .apply {
                binding = this
                recycler.adapter = liveAdapter
                configureWorkingList()

                initializeTextFilter()
            }.root
    }

    override fun onResume() {
        super.onResume()
        programEventsViewModel.setProgress(true)
        presenter.init()
    }

    override fun setLiveData(pagedListLiveData: LiveData<PagedList<EventViewModel>>) {
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

    private fun initializeTextFilter() {
        if (BuildConfig.FLAVOR == "widp"){
            binding.textFilterLayout.filterValueDetail.valueEditText.addTextChangedListener(
                object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        presenter.setTextFilterValue(s.toString())
                    }
                })

            val rotateArrow = { open: Boolean ->
                binding.textFilterLayout.filterValueHeader.filterArrow
                    .animate().scaleY((if (open) -1 else 1).toFloat())
                    .setDuration(200)
                    .start()
            }

            val handleExpandCollapse = {
                if (binding.textFilterLayout.filterValueDetail.root.visibility == View.VISIBLE) {
                    binding.textFilterLayout.filterValueDetail.root.collapse { }
                    rotateArrow(false)
                } else {
                    binding.textFilterLayout.filterValueDetail.root.expand { }
                    rotateArrow(true)
                }
            }

            binding.textFilterLayout.filterValueHeader.root.setOnClickListener {
                handleExpandCollapse()
            }
            binding.textFilterLayout.filterValueHeader.filterArrow.setOnClickListener {
                handleExpandCollapse()
            }
        } else {
            binding.textFilterLayout.filterValueHeader.root.visibility = View.GONE
        }
    }

    override fun setTextTypeDataElementsFilter(textTypeDataElements: List<DataElement>) {
        val dataElementsAdapter = DataElementsAdapter(
            abstracContext,
            R.layout.spinner_layout,
            R.id.spinner_text,
            textTypeDataElements,
            R.color.white_faf
        )

        binding.textFilterLayout.filterValueDetail.dataElementsSpinner.adapter = dataElementsAdapter

        binding.textFilterLayout.filterValueDetail.dataElementsSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val dataElement: DataElement = textTypeDataElements.get(position)

                    presenter.setTextFilterDataElement(dataElement.uid())
                }
            }
    }

}
