package org.dhis2.usescases.programEventDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.dhis2.maps.layer.basemaps.BaseMapStyle
import org.dhis2.maps.usecases.MapStyleConfiguration

class ProgramEventDetailViewModel(
    private val mapStyleConfig: MapStyleConfiguration
) : ViewModel() {
    private val progress = MutableLiveData(true)
    val writePermission = MutableLiveData(false)
    val eventSyncClicked = MutableLiveData<String?>(null)
    val eventClicked = MutableLiveData<Pair<String, String>?>(null)
    var updateEvent: String? = null
    var recreationActivity: Boolean = false
    enum class EventProgramScreen {
        LIST, MAP, ANALYTICS
    }
    private val _currentScreen = MutableLiveData(EventProgramScreen.LIST)
    val currentScreen: LiveData<EventProgramScreen>
        get() = Transformations.distinctUntilChanged(_currentScreen)

    private val _backdropActive = MutableLiveData<Boolean>()
    val backdropActive: LiveData<Boolean> get() = _backdropActive

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

    fun onRecreationActivity(isRecreating: Boolean) {
        recreationActivity = isRecreating
    }

    fun updateBackdrop(isActive: Boolean) {
        _backdropActive.value = isActive
    }

    fun fetchMapStyles(): List<BaseMapStyle> {
        return mapStyleConfig.fetchMapStyles()
    }
}
