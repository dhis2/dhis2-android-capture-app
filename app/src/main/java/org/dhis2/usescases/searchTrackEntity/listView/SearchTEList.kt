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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.paging.PagedList
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import javax.inject.Inject
import org.dhis2.Bindings.dp
import org.dhis2.commons.dialogs.imagedetail.ImageDetailBottomDialog
import org.dhis2.databinding.FragmentSearchListBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEIViewModel
import org.dhis2.usescases.searchTrackEntity.SearchTeiViewModelFactory
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiLiveAdapter
import org.dhis2.usescases.searchTrackEntity.ui.CreateNewButton
import org.dhis2.usescases.searchTrackEntity.ui.FullSearchButton
import org.dhis2.utils.isLandscape

const val ARG_FROM_RELATIONSHIP = "ARG_FROM_RELATIONSHIP"
private const val DIRECTION_DOWN = 1

class SearchTEList : FragmentGlobalAbstract() {

    @Inject
    lateinit var viewModelFactory: SearchTeiViewModelFactory

    private val viewModel by activityViewModels<SearchTEIViewModel> { viewModelFactory }

    private val initialLoadingAdapter by lazy {
        SearchListResultAdapter { }
    }

    private lateinit var recycler: RecyclerView

    private val liveAdapter by lazy {
        SearchTeiLiveAdapter(
            fromRelationship,
            onAddRelationship = viewModel::onAddRelationship,
            onSyncIconClick = viewModel::onSyncIconClick,
            onDownloadTei = viewModel::onDownloadTei,
            onTeiClick = viewModel::onTeiClick,
            onImageClick = ::displayImageDetail
        )
    }

    private val globalAdapter by lazy {
        SearchTeiLiveAdapter(
            fromRelationship,
            onAddRelationship = viewModel::onAddRelationship,
            onSyncIconClick = viewModel::onSyncIconClick,
            onDownloadTei = viewModel::onDownloadTei,
            onTeiClick = viewModel::onTeiClick,
            onImageClick = ::displayImageDetail
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
            SearchTEListModule()
        ).inject(this)
    }

    @ExperimentalAnimationApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
            updateLayoutParams<ConstraintLayout.LayoutParams> {
                val paddingTop = if (isLandscape()) {
                    0.dp
                } else {
                    80.dp
                }
                setPaddingRelative(0.dp, paddingTop, 0.dp, 160.dp)
            }
            adapter = listAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!recyclerView.canScrollVertically(DIRECTION_DOWN)) {
                        viewModel.isScrollingDown.value = false
                    }
                }

                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int
                ) {
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
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                if (LocalConfiguration.current.orientation ==
                    Configuration.ORIENTATION_PORTRAIT
                ) {
                    val isScrollingDown by viewModel.isScrollingDown.observeAsState(false)
                    val isFilterOpened by viewModel.filtersOpened.observeAsState(false)
                    FullSearchButton(
                        modifier = Modifier,
                        visible = !isScrollingDown,
                        closeFilterVisibility = isFilterOpened,
                        isLandscape = isLandscape(),
                        onClick = { viewModel.setSearchScreen() },
                        onCloseFilters = { viewModel.onFiltersClick(isLandscape()) }
                    )
                }
            }
        }
    }

    @ExperimentalAnimationApi
    private fun configureCreateButton(createButton: ComposeView) {
        createButton.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                val isScrollingDown by viewModel.isScrollingDown.observeAsState(false)
                val createButtonVisibility by viewModel
                    .createButtonScrollVisibility.observeAsState(true)
                val filtersOpened by viewModel.filtersOpened.observeAsState(false)
                updateLayoutParams<ConstraintLayout.LayoutParams> {
                    val bottomMargin = if (viewModel.isBottomNavigationBarVisible()) {
                        56.dp
                    } else {
                        16.dp
                    }
                    setMargins(0, 0, 0, bottomMargin)
                }
                if (createButtonVisibility && !filtersOpened) {
                    CreateNewButton(
                        modifier = Modifier,
                        extended = !isScrollingDown,
                        onClick = viewModel::onEnrollClick
                    )
                }
            }
        }
    }

    private fun displayImageDetail(imagePath: String) {
        ImageDetailBottomDialog(null, File(imagePath))
            .show(childFragmentManager, ImageDetailBottomDialog.TAG)
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
                    liveAdapter.clearList()
                }
                if (searchResult.shouldClearGlobalData()) {
                    globalAdapter.clearList()
                }
            }
            displayResult(it)
            updateRecycler()
            recycler.post {
                recycler.smoothScrollToPosition(0)
            }
        }
    }

    private fun updateRecycler() {
        recycler.setPaddingRelative(
            0,
            when {
                !isLandscape() && listAdapter.itemCount > 1 -> 80.dp
                !isLandscape() && liveAdapter.itemCount == 0 &&
                    resultAdapter.itemCount == 1 -> 80.dp
                else -> 0.dp
            },
            0,
            when {
                listAdapter.itemCount > 1 -> 160.dp
                else -> 0.dp
            }
        )
    }

    private fun restoreAdapters() {
        initLoading(null)
        liveAdapter.clearList()
        if (!viewModel.filtersApplyOnGlobalSearch()) {
            globalAdapter.clearList()
        } else if (globalAdapter.itemCount > 0) {
            initGlobalData()
        }
        displayResult(null)
    }

    private val initResultCallback = object : PagedList.Callback() {
        override fun onChanged(position: Int, count: Int) {
        }

        override fun onInserted(position: Int, count: Int) {
            onInitDataLoaded()
        }

        override fun onRemoved(position: Int, count: Int) {
        }
    }

    private val globalResultCallback = object : PagedList.Callback() {
        override fun onChanged(position: Int, count: Int) {
        }

        override fun onInserted(position: Int, count: Int) {
            onGlobalDataLoaded()
        }

        override fun onRemoved(position: Int, count: Int) {
        }
    }

    private fun initData() {
        displayLoadingData()
        viewModel.fetchListResults {
            it?.takeIf { view != null }?.apply {
                removeObservers(viewLifecycleOwner)
                observe(viewLifecycleOwner) { results ->
                    liveAdapter.submitList(results) {
                        onInitDataLoaded()
                    }
                    results.addWeakCallback(results.snapshot(), initResultCallback)
                }
            } ?: onInitDataLoaded()
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
            isLandscape = isLandscape(),
            onlineErrorCode = liveAdapter.currentList?.lastOrNull()?.onlineErrorCode
        )
    }

    private fun onGlobalDataLoaded() {
        viewModel.onDataLoaded(
            programResultCount = liveAdapter.itemCount,
            globalResultCount = globalAdapter.itemCount,
            isLandscape = isLandscape()
        )
    }

    private fun initGlobalData() {
        displayLoadingData()
        viewModel.fetchGlobalResults()?.let {
            it.removeObservers(viewLifecycleOwner)
            it.observe(viewLifecycleOwner) { results ->
                globalAdapter.submitList(results) {
                    onGlobalDataLoaded()
                }
                results.addWeakCallback(results.snapshot(), globalResultCallback)
            }
        }
    }

    private fun displayLoadingData() {
        if (listAdapter.itemCount == 0) {
            initLoading(
                listOf(SearchResult(SearchResult.SearchResultType.LOADING))
            )
        } else {
            displayResult(
                listOf(SearchResult(SearchResult.SearchResultType.LOADING))
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
