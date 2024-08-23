package org.dhis2.usescases.searchTrackEntity.listView

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.dhis2.bindings.dp
import org.dhis2.commons.dialogs.imagedetail.ImageDetailActivity
import org.dhis2.commons.filters.workingLists.WorkingListViewModel
import org.dhis2.commons.filters.workingLists.WorkingListViewModelFactory
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.FragmentSearchListBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEIViewModel
import org.dhis2.usescases.searchTrackEntity.SearchTeiViewModelFactory
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiLiveAdapter
import org.dhis2.usescases.searchTrackEntity.ui.CreateNewButton
import org.dhis2.usescases.searchTrackEntity.ui.FullSearchButtonAndWorkingList
import org.dhis2.usescases.searchTrackEntity.ui.mapper.TEICardMapper
import org.dhis2.utils.isLandscape
import timber.log.Timber
import javax.inject.Inject

const val ARG_FROM_RELATIONSHIP = "ARG_FROM_RELATIONSHIP"
private const val DIRECTION_DOWN = 1

class SearchTEList : FragmentGlobalAbstract() {

    @Inject
    lateinit var viewModelFactory: SearchTeiViewModelFactory

    @Inject
    lateinit var workingListViewModelFactory: WorkingListViewModelFactory

    @Inject
    lateinit var colorUtils: ColorUtils

    @Inject
    lateinit var teiCardMapper: TEICardMapper

    private val viewModel by activityViewModels<SearchTEIViewModel> { viewModelFactory }

    private val workingListViewModel by viewModels<WorkingListViewModel> { workingListViewModelFactory }

    private val initialLoadingAdapter by lazy {
        SearchListResultAdapter { }
    }

    private lateinit var recycler: RecyclerView

    private val liveAdapter by lazy {
        SearchTeiLiveAdapter(
            fromRelationship,
            colorUtils,
            cardMapper = teiCardMapper,
            onAddRelationship = viewModel::onAddRelationship,
            onSyncIconClick = viewModel::onSyncIconClick,
            onDownloadTei = viewModel::onDownloadTei,
            onTeiClick = viewModel::onTeiClick,
            onImageClick = ::displayImageDetail,
        )
    }

    private val globalAdapter by lazy {
        SearchTeiLiveAdapter(
            fromRelationship,
            colorUtils,
            cardMapper = teiCardMapper,
            onAddRelationship = viewModel::onAddRelationship,
            onSyncIconClick = viewModel::onSyncIconClick,
            onDownloadTei = viewModel::onDownloadTei,
            onTeiClick = viewModel::onTeiClick,
            onImageClick = ::displayImageDetail,
        )
    }

    private val resultAdapter by lazy {
        SearchListResultAdapter {
            initGlobalData()
        }
    }

    private val listAdapter by lazy {
        ConcatAdapter(initialLoadingAdapter, liveAdapter, globalAdapter, resultAdapter)
    }

    private val fromRelationship by lazy {
        arguments?.getBoolean(ARG_FROM_RELATIONSHIP) ?: false
    }

    companion object {
        fun get(fromRelationships: Boolean): SearchTEList {
            return SearchTEList().apply {
                arguments = bundleArguments(fromRelationships)
            }
        }
    }

    private fun bundleArguments(fromRelationships: Boolean): Bundle {
        return Bundle().apply {
            putBoolean(ARG_FROM_RELATIONSHIP, fromRelationships)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context as SearchTEActivity).searchComponent.plus(
            SearchTEListModule(),
        ).inject(this)
    }

    @ExperimentalAnimationApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentSearchListBinding.inflate(inflater, container, false).apply {
            configureList(scrollView)
            configureOpenSearchButton(openSearchButton)
            configureCreateButton(createButton)
        }.root.also {
            observeNewData()
        }
    }

    private fun configureList(scrollView: RecyclerView) {
        scrollView.apply {
            adapter = listAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!recyclerView.canScrollVertically(DIRECTION_DOWN)) {
                        viewModel.isScrollingDown.value = false
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        viewModel.isScrollingDown.value = true
                    } else if (dy < 0) {
                        viewModel.isScrollingDown.value = false
                    }
                }
            })
        }.also {
            recycler = it
        }
    }

    @ExperimentalAnimationApi
    private fun configureOpenSearchButton(openSearchButton: ComposeView) {
        openSearchButton.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                val teTypeName by viewModel.teTypeName.observeAsState()
                val curreProgram by viewModel.currentProgram.observeAsState()
                val programsToBlockDirectEnrollment by viewModel.programsToBlockDirectEnrollment.observeAsState()
                val shouldShowCreateButton = !programsToBlockDirectEnrollment?.contains(curreProgram)!!

                Timber.tag("PROGRAMS_CURRENT_HERE").d(shouldShowCreateButton.toString())
                if (!teTypeName.isNullOrBlank()) {
                    val isFilterOpened by viewModel.filtersOpened.observeAsState(false)
                    val createButtonVisibility by viewModel
                        .createButtonScrollVisibility.observeAsState(true)
                    val queryData = remember(viewModel.uiState) {
                        viewModel.uiState.searchedItems
                    }

                    FullSearchButtonAndWorkingList(
                        teTypeName = teTypeName!!,
                        modifier = Modifier,
                        createButtonVisible = createButtonVisibility,
                        closeFilterVisibility = isFilterOpened,
                        isLandscape = isLandscape(),
                        queryData = queryData,
                        onSearchClick = { viewModel.setSearchScreen() },
                        onEnrollClick = { viewModel.onEnrollClick() },
                        onCloseFilters = { viewModel.onFiltersClick(isLandscape()) },
                        onClearSearchQuery = {
                            viewModel.clearQueryData()
                            viewModel.clearFocus()
                        },
                        workingListViewModel = workingListViewModel,
                        shouldShowCreateButton = shouldShowCreateButton
                    )
                }
            }
        }
    }

    @ExperimentalAnimationApi
    private fun configureCreateButton(createButton: ComposeView) {
        createButton.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                val isScrollingDown by viewModel.isScrollingDown.observeAsState(false)
                val createButtonVisibility by viewModel
                    .createButtonScrollVisibility.observeAsState(true)
                val filtersOpened by viewModel.filtersOpened.observeAsState(false)
                val teTypeName by viewModel.teTypeName.observeAsState()
                val hasQueryData = remember(viewModel.uiState) {
                    viewModel.queryData.isNotEmpty()
                }

                updateLayoutParams<CoordinatorLayout.LayoutParams> {
                    val bottomMargin = if (viewModel.isBottomNavigationBarVisible()) {
                        56.dp
                    } else {
                        16.dp
                    }
                    setMargins(0, 0, 0, bottomMargin)
                }

                val orientation = LocalConfiguration.current.orientation
                if ((hasQueryData || orientation == Configuration.ORIENTATION_LANDSCAPE) && createButtonVisibility && !filtersOpened && !teTypeName.isNullOrBlank()) {
                    CreateNewButton(
                        modifier = Modifier,
                        extended = !isScrollingDown,
                        onClick = viewModel::onEnrollClick,
                        teTypeName = teTypeName!!,
                    )
                }
            }
        }
    }

    private fun displayImageDetail(imagePath: String) {
        val intent = ImageDetailActivity.intent(
            context = requireContext(),
            title = null,
            imagePath = imagePath,
        )

        startActivity(intent)
    }

    private fun observeNewData() {
        viewModel.refreshData.observe(viewLifecycleOwner) {
            restoreAdapters()
            initData()
        }

        viewModel.dataResult.observe(viewLifecycleOwner) {
            initLoading(emptyList())
            it.firstOrNull()?.let { searchResult ->
                if (searchResult.shouldClearProgramData()) {
                    liveAdapter.refresh()
                }
                if (searchResult.shouldClearGlobalData()) {
                    globalAdapter.refresh()
                }
                if (searchResult.type == SearchResult.SearchResultType.TOO_MANY_RESULTS) {
                    listAdapter.removeAdapter(liveAdapter)
                }
                displayResult(it)
                updateRecycler()
            }
        }

        liveAdapter.addLoadStateListener { state ->
            if (state.append == LoadState.Loading) {
                displayResult(
                    listOf(SearchResult(SearchResult.SearchResultType.LOADING)),
                )
            } else {
                displayResult(null)
            }
        }
    }

    private fun updateRecycler() {
        recycler.setPaddingRelative(
            0,
            0,
            0,
            when {
                listAdapter.itemCount > 1 -> 160.dp
                else -> 0.dp
            },
        )
    }

    private fun restoreAdapters() {
        if (!listAdapter.adapters.contains(liveAdapter)) {
            listAdapter.addAdapter(1, liveAdapter)
        }
        initLoading(null)
        liveAdapter.refresh()
        if (!viewModel.filtersApplyOnGlobalSearch()) {
            globalAdapter.refresh()
        } else if (globalAdapter.itemCount > 0) {
            initGlobalData()
        }
        displayResult(null)
    }

    private fun initData() {
        displayLoadingData()

        viewModel.fetchListResults {
            lifecycleScope.launch {
                it?.takeIf { view != null }?.collectLatest {
                    liveAdapter.addOnPagesUpdatedListener {
                        onInitDataLoaded()
                    }
                    liveAdapter.submitData(lifecycle, it)
                } ?: onInitDataLoaded()
            }
        }
    }

    private fun onInitDataLoaded() {
        viewModel.onDataLoaded(
            programResultCount = liveAdapter.itemCount,
            globalResultCount = if (globalAdapter.itemCount > 0) {
                globalAdapter.itemCount
            } else {
                null
            },
            onlineErrorCode = liveAdapter.snapshot().items.lastOrNull()?.onlineErrorCode,
        )
    }

    private fun onGlobalDataLoaded() {
        viewModel.onDataLoaded(
            programResultCount = liveAdapter.itemCount,
            globalResultCount = globalAdapter.itemCount,
        )
    }

    private fun initGlobalData() {
        displayLoadingData()
        viewModel.viewModelScope.launch {
            viewModel.fetchGlobalResults()?.collectLatest {
                globalAdapter.addOnPagesUpdatedListener {
                    onGlobalDataLoaded()
                }
                globalAdapter.submitData(it)
            }
        }
    }

    private fun displayLoadingData() {
        if (listAdapter.itemCount == 0) {
            initLoading(
                listOf(SearchResult(SearchResult.SearchResultType.LOADING)),
            )
        } else {
            displayResult(
                listOf(SearchResult(SearchResult.SearchResultType.LOADING)),
            )
        }
    }

    private fun initLoading(result: List<SearchResult>?) {
        recycler.post {
            initialLoadingAdapter.submitList(result)
        }
    }

    private fun displayResult(result: List<SearchResult>?) {
        recycler.post {
            resultAdapter.submitList(result)
        }
    }
}
