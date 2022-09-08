package org.dhis2.android.rtsm.ui.main

import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.TransitionManager
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.constraintlayout.widget.ConstraintSet
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.databinding.ActivityMainRtsmBinding
import org.dhis2.android.rtsm.ui.base.BaseActivity
import org.dhis2.android.rtsm.ui.filter.FilterAdapter
import org.dhis2.commons.filters.FilterItem
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainRtsmBinding

    private lateinit var viewModel: MainViewModel

    //@Inject lateinit var newAdapter: FiltersAdapter

    private lateinit var filterAdapter: FilterAdapter

    private var backDropActive = false
    private var elevation = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel =  getViewModel() as MainViewModel

        binding = getViewBinding() as ActivityMainRtsmBinding
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        //facilityTextView = (AutoCompleteTextView) binding.selectedFacilityTextView.getEditText();
        //distributedToTextView = (AutoCompleteTextView) binding.distributedToTextView.getEditText();

        //attachObservers();
        //setupComponents();

        //synchronizeData();

        // Cannot go up the stack
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        backToHome()

        binding.filterActionButton.setOnClickListener {
            initFilters()
            showHideFilter()
        }
    }


    private fun hideFilters() {
        binding.filterActionButton.visibility = View.GONE
    }

    private fun setFilters(filters: List<FilterItem>) {
        //newAdapter.submitList(filters)
        Timber.tag("F_LIST").e("$filters")
    }


    private fun observeSyncState() {
        /*presenter.observeDataSync().observe(this) {
            val currentState = it.firstOrNull()?.state
            if (currentState == WorkInfo.State.RUNNING) {
                setFilterButtonVisibility(false)
                setBottomNavigationVisibility(false)
            } else if (
                currentState == WorkInfo.State.SUCCEEDED ||
                currentState == WorkInfo.State.FAILED ||
                currentState == WorkInfo.State.CANCELLED
            ) {
                setFilterButtonVisibility(true)
                setBottomNavigationVisibility(true)
                presenter.onDataSuccess()
            }
        }*/
    }

    private fun initFilters() {
        viewModel.stockItem.observe(this) {
            if (it != null) {
                filterAdapter = FilterAdapter(it.toMutableList()).apply {
                    binding.filterRecycler.setItemViewCacheSize(itemCount)
                }

                binding.filterRecycler.setHasFixedSize(true)
                binding.filterRecycler.adapter = filterAdapter
            } else {
                Toast.makeText(this, "I am empty...", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showHideFilter() {
        val transition = ChangeBounds()
        transition.duration = 200
        TransitionManager.beginDelayedTransition(binding.backdropLayout, transition)
        backDropActive = !backDropActive
        val initSet = ConstraintSet()
        initSet.clone(binding.backdropLayout)
        if (backDropActive) {
            initSet.connect(
                R.id.fragment_container,
                ConstraintSet.TOP,
                R.id.filterRecycler,
                ConstraintSet.BOTTOM,
                50
            )
        } else {
            initSet.connect(
                R.id.fragment_container,
                ConstraintSet.TOP,
                R.id.toolbar,
                ConstraintSet.BOTTOM,
                0
            )
        }
        initSet.applyTo(binding.backdropLayout)
    }

    @NonNull
    override fun createViewBinding(): ViewDataBinding {
        return DataBindingUtil.setContentView(this, R.layout.activity_main_rtsm)
    }

    @NonNull
    override fun createViewModel(@NonNull disposable:CompositeDisposable): ViewModel {
        return ViewModelProvider(this)[MainViewModel::class.java]
    }

    private fun backToHome() {
        binding.back.setOnClickListener { finish() }
    }
}
