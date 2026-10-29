package org.dhis2.usescases.about

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.extensions.launchUseCase
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.usescases.about.domain.GetAboutInfo
import org.dhis2.usescases.about.model.AboutLink
import org.dhis2.usescases.about.ui.state.AboutUiEvent
import org.dhis2.usescases.about.ui.state.AboutUiState
import timber.log.Timber

class AboutViewModel(
    private val getAboutInfo: GetAboutInfo,
    private val networkStatusProvider: NetworkStatusProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AboutUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<AboutUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadAboutInfo()
    }

    private fun loadAboutInfo() =
        launchUseCase {
            getAboutInfo()
                .onSuccess { aboutInfo -> _uiState.update { it.copy(info = aboutInfo) } }
                .onFailure { Timber.e(it) }
        }

    fun onLinkClicked(link: AboutLink) =
        launchUseCase {
            val event =
                if (link.isExternal && !isOnline()) {
                    AboutUiEvent.ShowNoConnection
                } else {
                    AboutUiEvent.OpenLink(link)
                }
            _uiEvent.send(event)
        }

    private suspend fun isOnline(): Boolean = networkStatusProvider.connectionStatus.firstOrNull() == true
}
