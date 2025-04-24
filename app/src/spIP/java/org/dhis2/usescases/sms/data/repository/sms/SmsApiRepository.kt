package org.dhis2.usescases.sms.data.repository.sms

import org.dhis2.usescases.sms.data.api.OutboundApi
import org.dhis2.usescases.sms.data.model.OutboundRequest
import org.dhis2.usescases.sms.domain.model.sms.Message
import org.dhis2.usescases.sms.domain.repository.sms.SmsRepository
import javax.inject.Inject

class SmsApiRepository @Inject constructor(
  private val outboundApi: OutboundApi
) : SmsRepository {

  /**
   * Sends an SMS message to the specified recipients.
   *
   * @param message The message to be sent.
   * @throws Exception if there is an error sending the message.
   */
  override suspend fun send(
    message: Message
  ) {
    try {
      val request = OutboundRequest(
        message = message.text,
        recipients = message.recipients
      )
      val response = outboundApi.sendSms(request)
      response
//      if (!response.isSuccessful) {
//        throw Exception("Error sending message: ${response.errorBody()?.string()}")
//      }
    } catch (e: Exception) {
      throw Exception("Error sending message: ${e.message}")
    }
  }

}