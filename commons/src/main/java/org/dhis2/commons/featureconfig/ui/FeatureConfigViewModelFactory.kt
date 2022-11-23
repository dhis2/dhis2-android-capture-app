package org.dhis2.commons.featureconfig.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository

@Suppress("UNCHECKED_CAST")
class FeatureConfigViewModelFactory(val repository: FeatureConfigRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FeatureConfigViewModel(repository) as T
    }
}
