package org.dhis2.usescases.programEventDetail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Map
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.maps.layer.basemaps.BaseMapStyle
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.model.SnackbarMessage
import org.dhis2.tracker.NavigationBarUIState
import org.dhis2.tracker.events.CreateEventUseCase
import org.dhis2.utils.customviews.navigationbar.NavigationPage
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBarItem

class ProgramEventDetailViewModel(
    private val mapStyleConfig: MapStyleConfiguration,
    val eventRepository: ProgramEventDetailRepository,
    val dispatcher: DispatcherProvider,
    val createEventUseCase: CreateEventUseCase,
    private val pageConfigurator: NavigationPageConfigurator,
    private val resourceManager: ResourceManager,
) : ViewModel() {
    private val progress = MutableLiveData(false)
    val writePermission = MutableLiveData(false)
    val eventSyncClicked = MutableLiveData<String?>(null)
    val eventClicked = MutableLiveData<Pair<String, String>?>(null)
    var updateEvent: String? = null
    var recreationActivity: Boolean = false

    enum class EventProgramScreen {
        LIST,
        MAP,
        ANALYTICS,
    }

    private val _currentScreen = MutableLiveData(EventProgramScreen.LIST)
    val currentScreen: LiveData<EventProgramScreen>
        get() = _currentScreen.distinctUntilChanged()

    private val _backdropActive = MutableLiveData<Boolean>()
    val backdropActive: LiveData<Boolean> get() = _backdropActive

    private val _shouldNavigateToEventDetails: MutableSharedFlow<String> =
        MutableSharedFlow(
            replay = Int.MAX_VALUE,
        )
    val shouldNavigateToEventDetails: SharedFlow<String>
        get() = _shouldNavigateToEventDetails

    private val _navigationBarUIState = mutableStateOf(NavigationBarUIState<NavigationPage>())
    val navigationBarUIState: State<NavigationBarUIState<NavigationPage>> = _navigationBarUIState

    private val _snackbarMessage = MutableSharedFlow<SnackbarMessage>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    init {
        viewModelScope.launch { loadBottomBarItems() }
    }

    private fun loadBottomBarItems() {
        val navItems = mutableListOf<NavigationBarItem<NavigationPage>>()

        if (pageConfigurator.displayListView()) {
            navItems.add(
                NavigationBarItem(
                    id = NavigationPage.LIST_VIEW,
                    icon = Icons.AutoMirrored.Outlined.List,
                    selectedIcon = Icons.AutoMirrored.Filled.List,
                    label = resourceManager.getString(R.string.navigation_list_view),
                ),
            )
        }

        if (pageConfigurator.displayMapView()) {
            navItems.add(
                NavigationBarItem(
                    id = NavigationPage.MAP_VIEW,
                    icon = Icons.Outlined.Map,
                    selectedIcon = Icons.Filled.Map,
                    label = resourceManager.getString(R.string.navigation_map_view),
                ),
            )
        }

        if (pageConfigurator.displayAnalytics()) {
            navItems.add(
                NavigationBarItem(
                    id = NavigationPage.ANALYTICS,
                    icon = Icons.Outlined.BarChart,
                    selectedIcon = Icons.Filled.BarChart,
                    label = resourceManager.getString(R.string.navigation_charts),
                ),
            )
        }

        _navigationBarUIState.value =
            _navigationBarUIState.value.copy(
                items = navItems,
                selectedItem = navItems.firstOrNull()?.id,
            )

        if (_navigationBarUIState.value.selectedItem != null) {
            onNavigationPageChanged(
                navigationBarUIState.value.items
                    .first()
                    .id,
            )
        }
    }

    fun onNavigationPageChanged(page: NavigationPage) {
        _navigationBarUIState.value = _navigationBarUIState.value.copy(selectedItem = page)
    }

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

    fun fetchMapStyles(): List<BaseMapStyle> = mapStyleConfig.fetchMapStyles()

    fun isEditable(eventUid: String): Boolean = eventRepository.isEventEditable(eventUid)

    fun onOrgUnitForNewEventSelected(
        orgUnitUid: String,
        programUid: String,
        programStageUid: String,
    ) {
        viewModelScope.launch(dispatcher.io()) {
            createEventUseCase(
                programUid = programUid,
                orgUnitUid = orgUnitUid,
                programStageUid = programStageUid,
                enrollmentUid = null,
            ).getOrNull()?.let { eventUid ->
                _shouldNavigateToEventDetails.emit(eventUid)
            }
        }
    }

    fun displayMessage(msg: String) {
        viewModelScope.launch(dispatcher.io()) {
            _snackbarMessage.emit(SnackbarMessage(message = msg))
        }
    }
}
