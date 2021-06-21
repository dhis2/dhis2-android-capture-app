package dhis2.org.analytics.charts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import dhis2.org.R
import dhis2.org.analytics.charts.data.AnalyticGroup
import dhis2.org.databinding.AnalyticsGroupBinding

class GroupAnalyticsFragment : Fragment() {

    private lateinit var binding: AnalyticsGroupBinding
    private val groupViewModel: GroupAnalyticsViewModel by viewModels()
    var disableToolbarElevation: (()-> Unit)? = null

    companion object {
        fun newInstance(): GroupAnalyticsFragment {
            return GroupAnalyticsFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.analytics_group, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupViewModel.chipItems.observe(viewLifecycleOwner, Observer {
            if (it.isEmpty() || it.size < MIN_SIZE_TO_SHOW) {
                binding.analyticChipGroup.visibility = View.GONE
            } else {
                disableToolbarElevation?.invoke()
                addChips(it)
            }
        })
    }

    private fun addChips(list: List<AnalyticGroup>) {
        list.forEach {
            val chip: Chip = layoutInflater.inflate(R.layout.analytics_item, null) as Chip
            chip.apply {
                text = it.name
                isSelected = false
                tag = it.uid
                setOnCheckedChangeListener { buttonView, isChecked ->
                    Log.d("checkedChip","${buttonView.tag} $isChecked")
                }
            }.also { binding.analyticChipGroup.addView(it) }
        }
    }
}