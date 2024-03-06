package org.dhis2.usescases.programEventDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.maps.layer.basemaps.BaseMapStyle
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class ProgramEventDetailViewModel(
    private val mapStyleConfig: MapStyleConfiguration,
    val eventRepository: ProgramEventDetailRepository,
    val dispatcher: DispatcherProvider,
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
        get() = _currentScreen.distinctUntilChanged()

    private val _backdropActive = MutableLiveData<Boolean>()
    val backdropActive: LiveData<Boolean> get() = _backdropActive

    private val _shouldNavigateToEventDetails: MutableSharedFlow<String> = MutableSharedFlow(
        replay = Int.MAX_VALUE,
    )
    val shouldNavigateToEventDetails: SharedFlow<String>
        get() = _shouldNavigateToEventDetails
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

    fun isEditable(eventUid: String): Boolean {
        return eventRepository.isEventEditable(eventUid)
    }

    fun displayOrganisationUnit(programUid: String): Boolean {
        return eventRepository.displayOrganisationUnit(programUid)
    }

    fun onOrgUnitForNewEventSelected(orgUnit: OrganisationUnit) {
        viewModelScope.launch(dispatcher.io()) {
            val result = async {
                eventRepository.createEvent(orgUnit.uid())
            }

            val eventUID = result.await()
            _shouldNavigateToEventDetails.emit(eventUID)
        }
    }
}
