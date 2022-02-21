package org.dhis2.usescases.searchTrackEntity.listView

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import javax.inject.Inject
import org.dhis2.commons.animations.hideWithTranslation
import org.dhis2.commons.animations.showWithTranslation
import org.dhis2.commons.bindings.clipWithRoundedCorners
import org.dhis2.databinding.FragmentSearchListBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.searchTrackEntity.CreateNewButton
import org.dhis2.usescases.searchTrackEntity.SearchButton
import org.dhis2.usescases.searchTrackEntity.SearchList
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEContractsModule
import org.dhis2.usescases.searchTrackEntity.SearchTEIViewModel
import org.dhis2.usescases.searchTrackEntity.SearchTeiViewModelFactory
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiLiveAdapter

const val ARG_FROM_RELATIONSHIP = "ARG_FROM_RELATIONSHIP"

class SearchTEList : FragmentGlobalAbstract() {

    @Inject
    lateinit var viewModelFactory: SearchTeiViewModelFactory

    @Inject
    lateinit var presenter: SearchTEContractsModule.Presenter

    private val viewModel by activityViewModels<SearchTEIViewModel> { viewModelFactory }

    private val initialLoadingAdapter by lazy {
        SearchListResultAdapter { }
    }

    private val liveAdapter by lazy {
        SearchTeiLiveAdapter(fromRelationship, presenter, childFragmentManager)
    }

    private val globalAdapter by lazy {
        SearchTeiLiveAdapter(fromRelationship, presenter, childFragmentManager)
    }

    private val resultAdapter by lazy {
        SearchListResultAdapter {
            displayLoadingData()
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSearchListBinding.inflate(inflater, container, false)
        binding.openSearchButton.setContent {
            MdcTheme {
                SearchButton {
                    viewModel.setSearchScreen()
                }
            }
        }
        binding.createButton.setContent {
            MdcTheme {
                CreateNewButton {
                    presenter.onEnrollClick()
                }
            }
        }
        binding.scrollView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    binding.openSearchButton.hideWithTranslation(verticalTranslation = true)
                    viewModel.screenState.value
                        .takeIf {
                            it is SearchList && it.canDisplayCreateButton()
                        }
                        ?.let {
                            binding.createButton.hideWithTranslation(horizontalTranslation = true)
                        }
                } else if (dy < 0) {
                    binding.openSearchButton.showWithTranslation(verticalTranslation = true)
                    viewModel.screenState.value
                        .takeIf {
                            it is SearchList && it.canDisplayCreateButton()
                        }
                        ?.let {
                            binding.createButton.showWithTranslation(horizontalTranslation = true)
                        }
                }
            }
        })
        binding.scrollView.adapter = listAdapter
        binding.content.clipWithRoundedCorners()
        observeScreenState {
            binding.createButton.visibility = if (it.canDisplayCreateButton()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        observeNewData()
        return binding.root
    }

    private fun observeScreenState(onStateChanged: (SearchList) -> Unit) {
        viewModel.screenState.observe(viewLifecycleOwner) {
            if (it is SearchList) {
                onStateChanged(it)
            }
        }
    }

    private fun observeNewData() {
        viewModel.refreshData.observe(viewLifecycleOwner) {
            displayLoadingData()
            initData()
        }
    }

    private fun initData() {
        viewModel.fetchListResults()?.let {
            it.removeObservers(viewLifecycleOwner)
            it.observe(viewLifecycleOwner) { results ->
                liveAdapter.submitList(results) {
                    onDataLoaded(liveAdapter.itemCount > 0)
                }
            }
        }
    }

    private fun initGlobalData() {
        viewModel.fetchGlobalResults()?.let {
            it.removeObservers(viewLifecycleOwner)
            it.observe(viewLifecycleOwner) { results ->
                globalAdapter.submitList(results) {
                    onDataLoaded(liveAdapter.itemCount > 0, globalAdapter.itemCount > 0)
                }
            }
        }
    }

    private fun displayLoadingData() {
        if (listAdapter.itemCount == 0) {
            initialLoadingAdapter.submitList(
                listOf(SearchResult(SearchResult.SearchResultType.LOADING))
            )
        } else {
            resultAdapter.submitList(listOf(SearchResult(SearchResult.SearchResultType.LOADING)))
        }
    }

    private fun onDataLoaded(hasProgramResults: Boolean, hasGlobalResults: Boolean? = null) {
        val resultData = mutableListOf<SearchResult>().apply {
            add(
                if (hasGlobalResults == null) {
                    SearchResult(SearchResult.SearchResultType.SEARCH_OUTSIDE)
                } else if (hasProgramResults || hasGlobalResults == true) {
                    SearchResult(SearchResult.SearchResultType.NO_MORE_RESULTS)
                } else {
                    SearchResult(SearchResult.SearchResultType.NO_RESULTS)
                }
            )
        }
        initialLoadingAdapter.submitList(emptyList())
        resultAdapter.submitList(resultData)
    }
}
