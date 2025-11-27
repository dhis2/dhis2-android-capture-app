package org.dhis2.commons.featureconfig.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository

class FeatureConfigViewModelFactory(
    val repository: FeatureConfigRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FeatureConfigViewModel(repository) as T
}
