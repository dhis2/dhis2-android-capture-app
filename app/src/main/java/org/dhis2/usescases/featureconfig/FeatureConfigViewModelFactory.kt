package org.dhis2.usescases.featureconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class FeatureConfigViewModelFactory : ViewModelProvider.Factory {

    private val repository = FeatureConfigRepositoryImpl()

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FeatureConfigViewModel(repository) as T
    }
}