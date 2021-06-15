package org.dhis2.usescases.featureconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

@Suppress("UNCHECKED_CAST")
class FeatureConfigViewModelFactory @Inject constructor(val repository: FeatureConfigRepository) :
    ViewModelProvider.Factory {


    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FeatureConfigViewModel(repository) as T
    }
}