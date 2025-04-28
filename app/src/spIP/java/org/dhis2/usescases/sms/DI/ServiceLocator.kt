package org.dhis2.usescases.sms.DI

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

object ServiceLocator {

  fun provideSendSmsUseCase(): SendSmsUseCase {

    val d2 = D2Manager.getD2()
    val httpClient = d2.httpServiceClient()

    val outboundApi: OutboundApi = OutboundApiImpl(httpClient)
    val constantApi: ConstantApi = ConstantApiImpl(httpClient)

    val patientRepository = PatientD2Repository(d2)
    val messageTemplate = MessageTemplateD2Repository(constantApi,d2)
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