package org.dhis2.usescases.sms.domain.usecase

import org.dhis2.usescases.sms.data.model.MessageTemplate
import org.dhis2.usescases.sms.domain.model.sms.Message
import org.dhis2.usescases.sms.domain.model.sms.SmsResult
import org.dhis2.usescases.sms.domain.repository.message.MessageTemplateRepository
import org.dhis2.usescases.sms.domain.repository.patient.PatientRepository
import org.dhis2.usescases.sms.domain.repository.preferred.PreferredLanguageRepository
import org.dhis2.usescases.sms.domain.repository.sms.SmsRepository
import javax.inject.Inject

private const val LANGUAGE_EN = "en"

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
  suspend fun invoke(
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

    try {
      smsRepository.send(message)

      return if (patient.preferredLanguage != LANGUAGE_EN && messageTemplate.language == LANGUAGE_EN) {
        val language = preferredLanguageRepository.getByCode(patient.preferredLanguage)

        SmsResult.SuccessUsingEn(language.name)
      } else {
        SmsResult.Success
      }
    } catch (e: Exception) {
      return SmsResult.SendFailure
    }

  }

  /**
   * Retrieves the message template for the specified language.
   *
   * @param language The language code for the message template.
   * @return The message template for the specified language, or null if not found.
   */
  private suspend fun getMessageTemplate(
    language: String
  ): MessageTemplate? {
    val messageTemplate = smsTemplateRepository.getByLanguage(language)
    return if (messageTemplate.isSome()) {
      messageTemplate.getOrThrow()
    } else {
      val defaultMessageTemplate = smsTemplateRepository.getByLanguage(LANGUAGE_EN)

      if (defaultMessageTemplate.isSome()) {
        defaultMessageTemplate.getOrThrow()
      } else {
        return null
      }
    }
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