package org.dhis2.commons.featureconfig.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.dhis2.commons.R
import org.dhis2.commons.databinding.FeatureConfigViewBinding
import org.dhis2.commons.featureconfig.di.FeatureConfigComponentProvider
import javax.inject.Inject

class FeatureConfigView : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: FeatureConfigViewModelFactory

    private val viewModel: FeatureConfigViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (applicationContext as FeatureConfigComponentProvider)
            .provideFeatureConfigActivityComponent()
            ?.inject(this)

        val binding = DataBindingUtil.setContentView<FeatureConfigViewBinding>(
            this,
            R.layout.feature_config_view,
        )

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.recyclerview.apply {
            adapter = FeatureListAdapter(viewModel)
        }

        viewModel.featuresList.observe(
            this,
            Observer {
                (binding.recyclerview.adapter as FeatureListAdapter).submitList(it)
            },
        )
    }
}
