package org.dhis2.usescases.teiDashboard.ui

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.dhis2.usescases.sms.domain.model.sms.SmsResult
import org.dhis2.usescases.sms.domain.usecase.SendSmsUseCase
import org.dhis2.usescases.teiDashboard.TeiDashboardMenuCustomActionsManager
import javax.inject.Inject

class TeiDashboardMenuCustomActionsManagerImpl @Inject constructor(
  private val context: Context,
  private val dispatcherIO: CoroutineDispatcher = Dispatchers.IO,
  private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main,
  private val sendSmsUseCase: SendSmsUseCase,
) : TeiDashboardMenuCustomActionsManager, CoroutineScope {

  private val job = Job()
  override val coroutineContext = dispatcherIO + job

  /**
   * This method is used to send SMS to the TEI.
   * @param teiUid The UID of the TEI to whom the SMS will be sent.
   */
  override fun sendSms(teiUid: String?) {
    if (teiUid == null) {
      println("TEI UID is null, cannot send SMS")
      return
    }
    launch {
      val result = sendSmsUseCase.invoke(teiUid)
      when (result) {
        is SmsResult.Success -> println("SMS sent successfully")
        is SmsResult.SuccessUsingEn -> println("SMS sent in English: ")
        is SmsResult.TemplateFailure -> println("Failed to get template")
        is SmsResult.SendFailure -> println("Failed to send SMS")
      }
    }
  }

  override fun onDestroy() {
    job.cancel()
  }
}