package org.dhis2.usescases.sms.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.dhis2.usescases.sms.domain.model.sms.SmsResult.SendFailure
import org.dhis2.usescases.sms.domain.model.sms.SmsResult.Success
import org.dhis2.usescases.sms.domain.model.sms.SmsResult.SuccessUsingEn
import org.dhis2.usescases.sms.domain.model.sms.SmsResult.TemplateFailure
import org.dhis2.usescases.sms.domain.usecase.SendSmsUseCase
import org.dhis2.usescases.sms.presentation.contentprovider.SpipSmsContentResourcesProvider
import org.dhis2.usescases.teiDashboard.TeiDashboardResultUiState
import javax.inject.Inject

@HiltViewModel
class SendSmsViewModel @Inject constructor(
  private val sendSmsUseCase: SendSmsUseCase,
) : ViewModel() {

  private val _sendSmsState =
    MutableStateFlow<TeiDashboardResultUiState<String>>(TeiDashboardResultUiState.Loading)

  val sendSmsState: StateFlow<TeiDashboardResultUiState<String>> = _sendSmsState

  fun sendSms(teiUid: String) {
    viewModelScope.launch {
      _sendSmsState.value = when (sendSmsUseCase.invoke(teiUid)) {
        SendFailure -> TeiDashboardResultUiState.Error("Failed to send SMS")
        Success -> TeiDashboardResultUiState.Success("SMS sent successfully")
        TemplateFailure -> TeiDashboardResultUiState.Error("Failed to send SMS")
        is SuccessUsingEn -> TeiDashboardResultUiState.Success("SMS sent successfully in English")
      }
    }
  }

}