package org.dhis2.usescases.sms.domain.model.sms


sealed class SmsResult {
  data object Success : SmsResult()
  data class SuccessUsingEn(val preferredLanguage: String) : SmsResult()
  data object TemplateFailure : SmsResult()
  data object SendFailure : SmsResult()
}