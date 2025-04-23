package org.dhis2.usescases.sms.DI

import io.ktor.client.HttpClient
import org.dhis2.usescases.sms.data.api.ConstantApi
import org.dhis2.usescases.sms.data.api.ConstantApiImpl
import org.dhis2.usescases.sms.data.api.OutboundApi
import org.dhis2.usescases.sms.data.api.OutboundApiImpl
import org.dhis2.usescases.sms.data.repository.message.MessageTemplateD2Repository
import org.dhis2.usescases.sms.data.repository.patient.PatientD2Repository
import org.dhis2.usescases.sms.data.repository.preferred.PreferredLanguageD2Repository
import org.dhis2.usescases.sms.data.repository.sms.SmsApiRepository
import org.dhis2.usescases.sms.domain.usecase.SendSmsUseCase
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.arch.api.HttpServiceClient

object ServiceLocator {

  fun sms(): SendSmsUseCase {

    // Create the KTOR HttpClient instance
    val client = HttpServiceClient(
      client = HttpClient()
    )

    val d2 = D2Manager.getD2()

    val constantApi: ConstantApi = ConstantApiImpl(client)
    val outboundApi: OutboundApi = OutboundApiImpl(client)

    val patientRepository = PatientD2Repository(d2)
    val messageTemplate = MessageTemplateD2Repository(d2, constantApi)
    val preferredLanguageRepository = PreferredLanguageD2Repository(d2)
    val smsRepository = SmsApiRepository(outboundApi)

    return SendSmsUseCase(
      patientRepository,
      messageTemplate,
      preferredLanguageRepository,
      smsRepository
    )
  }
}