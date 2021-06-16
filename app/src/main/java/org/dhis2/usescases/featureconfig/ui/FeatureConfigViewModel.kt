package org.dhis2.usescases.featureconfig.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.dhis2.usescases.featureconfig.data.FeatureConfigRepository
import org.dhis2.usescases.featureconfig.model.FeatureState

class FeatureConfigViewModel constructor(
    private val repository: FeatureConfigRepository
) : ViewModel() {

    private val _featuresList = MutableLiveData<List<FeatureState>>()
    val featuresList: LiveData<List<FeatureState>> = _featuresList

    init {
        _featuresList.value = repository.featuresList
    }

    fun didUserTapOnItem(featureState: FeatureState) {
        repository.updateItem(featureState)
        _featuresList.value = repository.featuresList
    }
}
