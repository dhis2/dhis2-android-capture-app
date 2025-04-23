package org.dhis2.usescases.sms.domain.model.sms

data class Message(
  val text: String,
  val recipients: List<String>
)
