package org.dhis2.usescases.programEventDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProgramEventDetailViewModel : ViewModel() {
    private val progress = MutableLiveData<Boolean>(true)
    val writePermission = MutableLiveData<Boolean>()
    val eventSyncClicked = MutableLiveData<String?>(null)
    val eventClicked = MutableLiveData<Pair<String, String>?>(null)
    var updateEvent: String? = null

    fun setProgress(showProgress: Boolean) {
        progress.value = showProgress
    }

    fun progress(): LiveData<Boolean> = progress
}
