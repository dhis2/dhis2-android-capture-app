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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.activityViewModels
import androidx.paging.PagedList
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import javax.inject.Inject
import org.dhis2.databinding.FragmentSearchListBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.searchTrackEntity.CreateNewButton
import org.dhis2.usescases.searchTrackEntity.FullSearchButton
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEIViewModel
import org.dhis2.usescases.searchTrackEntity.SearchTeiViewModelFactory
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiLiveAdapter
import org.dhis2.utils.customviews.ImageDetailBottomDialog
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
            scrollView.apply {
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
            }
            openSearchButton.apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
                setContent {
                    if (LocalConfiguration.current.orientation ==
                        Configuration.ORIENTATION_PORTRAIT
                    ) {
                        val isScrollingDown by viewModel.isScrollingDown.observeAsState(false)
                        FullSearchButton(
                            modifier = Modifier,
                            visible = !isScrollingDown,
                            onClick = { viewModel.setSearchScreen(isLandscape()) }
                        )
                    }
                }
            }
            createButton.apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
                )
                setContent {
                    val isScrollingDown by viewModel.isScrollingDown.observeAsState(false)
                    val createButtonVisibility by viewModel
                        .createButtonScrollVisibility.observeAsState(true)
                    if (createButtonVisibility) {
                        CreateNewButton(
                            modifier = Modifier,
                            extended = !isScrollingDown,
                            onClick = viewModel::onEnrollClick
                        )
                    }
                }
            }
        }.root.also {
            observeNewData()
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
            initialLoadingAdapter.submitList(emptyList())
            it.firstOrNull()?.let { searchResult ->
                if (searchResult.shouldClearProgramData()) {
                    liveAdapter.clearList()
                }
                if (searchResult.shouldClearGlobalData()) {
                    globalAdapter.clearList()
                }
            }
            resultAdapter.submitList(it)
        }
    }

    private fun restoreAdapters() {
        initialLoadingAdapter.submitList(null)
        liveAdapter.clearList()
        globalAdapter.clearList()
        resultAdapter.submitList(null)
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
            it?.apply {
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
            isLandscape = isLandscape()
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
            initialLoadingAdapter.submitList(
                listOf(SearchResult(SearchResult.SearchResultType.LOADING))
            )
        } else {
            resultAdapter.submitList(
                listOf(SearchResult(SearchResult.SearchResultType.LOADING))
            )
        }
    }
}
