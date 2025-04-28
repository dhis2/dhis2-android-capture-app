package org.dhis2.usescases.sms.domain.usecase

import org.dhis2.usescases.sms.data.model.MessageTemplate
import org.dhis2.usescases.sms.domain.model.sms.Message
import org.dhis2.usescases.sms.domain.model.sms.SmsResult
import org.dhis2.usescases.sms.domain.repository.message.MessageTemplateRepository
import org.dhis2.usescases.sms.domain.repository.patient.PatientRepository
import org.dhis2.usescases.sms.domain.repository.preferred.PreferredLanguageRepository
import org.dhis2.usescases.sms.domain.repository.sms.SmsRepository
import javax.inject.Inject

const val LANGUAGE_EN = "en"

class SendSmsUseCase @Inject constructor(
  private val patientRepository: PatientRepository,
  private val smsTemplateRepository: MessageTemplateRepository,
  private val preferredLanguageRepository: PreferredLanguageRepository,
  private val smsRepository: SmsRepository
){

  /**
   * Sends an SMS message to a patient.
   *
   * @param uid The unique identifier of the patient.
   * @return The result of the SMS sending operation.
   */
  suspend operator fun invoke(
    uid: String
  ): SmsResult {
    val patient = patientRepository.getByUid(uid)

    val messageTemplate = getMessageTemplate(patient.preferredLanguage)
      ?: return SmsResult.TemplateFailure

    val message = Message(
      text = messageTemplate.text
        .replace("{{fullName}}", patient.name)
        .replace("{{patientNumber}}", patient.number),
      recipients = listOf(cleanupPhoneNumber(patient.phone))
    )

    return smsRepository.send(message).fold(
      onSuccess = {
        if (patient.preferredLanguage != LANGUAGE_EN && messageTemplate.language == LANGUAGE_EN) {
          val language = preferredLanguageRepository.getByCode(patient.preferredLanguage)
          SmsResult.SuccessUsingEn(language.name)
        } else {
          SmsResult.Success
        }
      },
      onFailure = {
        SmsResult.SendFailure
      }
    )
  }

  /**
   * Retrieves the message template for the specified language.
   *
   * @param language The language code for the message template.
   * @return The message template for the specified language, or null if not found.
   */
  private suspend fun getMessageTemplate(language: String): MessageTemplate? {
    smsTemplateRepository.getByLanguage(language).takeIf { it.isSuccess }?.let {
      return it.getOrThrow()
    }
    return smsTemplateRepository.getByLanguage(LANGUAGE_EN).takeIf { it.isSuccess }?.getOrThrow()
  }

  /**
   * Cleans up the phone number by removing non-digit characters.
   *
   * @param phoneNumber The phone number to clean up.
   * @return The cleaned-up phone number.
   */
  private fun cleanupPhoneNumber(phoneNumber: String): String {
    return phoneNumber.replace(Regex("\\D"), "")
  }
}