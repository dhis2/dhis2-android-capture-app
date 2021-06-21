package org.dhis2.usescases.featureconfig.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.R
import org.dhis2.databinding.FeatureConfigViewBinding
import org.dhis2.usescases.featureconfig.data.FeatureConfigRepository
import org.dhis2.usescases.featureconfig.di.FeatureConfigModule

class FeatureConfigView : AppCompatActivity() {

    @Inject
    lateinit var repository: FeatureConfigRepository

    private val viewModel: FeatureConfigViewModel by viewModels {
        FeatureConfigViewModelFactory(
            repository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (applicationContext as App).userComponent()?.plus(FeatureConfigModule())?.inject(this)

        val binding = DataBindingUtil.setContentView<FeatureConfigViewBinding>(
            this, R.layout.feature_config_view
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
            }
        )
    }
}
