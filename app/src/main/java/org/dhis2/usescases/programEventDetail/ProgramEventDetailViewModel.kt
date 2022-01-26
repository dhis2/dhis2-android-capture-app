package org.dhis2.usescases.programEventDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class ProgramEventDetailViewModel : ViewModel() {
    private val progress = MutableLiveData(true)
    val writePermission = MutableLiveData(false)
    val eventSyncClicked = MutableLiveData<String?>(null)
    val eventClicked = MutableLiveData<Pair<String, String>?>(null)
    var updateEvent: String? = null
    enum class EventProgramScreen {
        LIST, MAP, ANALYTICS
    }
    private val _currentScreen = MutableLiveData(EventProgramScreen.LIST)
    val currentScreen: LiveData<EventProgramScreen>
        get() = Transformations.distinctUntilChanged(_currentScreen)

    fun setProgress(showProgress: Boolean) {
        progress.value = showProgress
    }

    fun progress(): LiveData<Boolean> = progress

    fun showList() {
        _currentScreen.value = EventProgramScreen.LIST
    }

    fun showMap() {
        _currentScreen.value = EventProgramScreen.MAP
    }

    fun showAnalytics() {
        _currentScreen.value = EventProgramScreen.ANALYTICS
    }
}
