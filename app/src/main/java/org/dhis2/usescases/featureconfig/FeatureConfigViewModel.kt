package org.dhis2.usescases.featureconfig

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FeatureConfigViewModel(
    private val repository: FeatureConfigRepository
) : ViewModel() {


    private val _featuresList = MutableLiveData<List<Feature>>()
    val featuresList: LiveData<List<Feature>> = _featuresList

    init {
        _featuresList.value = repository.featuresList
    }

    fun didUserTapOnItem(feature: Feature) {
        repository.updateItem(feature)
        _featuresList.value = repository.featuresList
    }
}