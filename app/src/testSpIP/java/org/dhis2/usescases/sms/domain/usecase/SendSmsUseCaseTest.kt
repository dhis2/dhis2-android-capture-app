package org.dhis2.usescases.sms.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.dhis2.usescases.sms.data.model.MessageTemplate
import org.dhis2.usescases.sms.data.model.OutboundResponse
import org.dhis2.usescases.sms.domain.model.patient.Patient
import org.dhis2.usescases.sms.domain.model.sms.Message
import org.dhis2.usescases.sms.domain.model.sms.SmsResult
import org.dhis2.usescases.sms.domain.repository.message.MessageTemplateRepository
import org.dhis2.usescases.sms.domain.repository.patient.PatientRepository
import org.dhis2.usescases.sms.domain.repository.preferred.PreferredLanguageRepository
import org.dhis2.usescases.sms.domain.repository.sms.SmsRepository
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class SendSmsUseCaseTest {

  private val patientRepository: PatientRepository = mock()
  private val smsTemplateRepository: MessageTemplateRepository = mock()
  private val preferredLanguageRepository: PreferredLanguageRepository = mock()
  private val smsRepository: SmsRepository = mock()
  private lateinit var sendSmsUseCase : SendSmsUseCase

  @Before
  fun setUp() {
    sendSmsUseCase = SendSmsUseCase(
      patientRepository,
      smsTemplateRepository,
      preferredLanguageRepository,
      smsRepository
    )
  }

  @Test
  fun `WHEN Patient has preferred Language THEN send SMS Successfully`() = runTest {
    val uid = "12345"
    val patient = Patient(
      uid = uid,
      number = "123456789",
      name = "John Doe",
      phone = "123456789",
      preferredLanguage = "es"
    )
    val messageTemplate = MessageTemplate("Hola {{fullName}}", LANGUAGE_EN)
    val message = Message("Hola John Doe", listOf("123456789"))
    val outboundResponse = OutboundResponse(
      httpStatus= "httpStatus",
      httpStatusCode = 100,
      status = "status",
      message = "message"
    )

    whenever(patientRepository.getByUid(uid)).thenReturn(patient)
    whenever(smsTemplateRepository.getByLanguage(LANGUAGE_EN)).thenReturn(Result.success(messageTemplate))
    whenever(smsRepository.send(message)).thenReturn(Result.success(outboundResponse))

    val result = sendSmsUseCase.invoke(uid)

    assert(result is SmsResult.Success)
  }


}