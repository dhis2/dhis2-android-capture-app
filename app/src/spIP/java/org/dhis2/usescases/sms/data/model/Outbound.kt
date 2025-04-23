package org.dhis2.usescases.sms.data.model

data class OutboundResponse(
  val httpStatus: String,
  val httpStatusCode: Int,
  val status: String,
  val message: String
)

data class OutboundRequest(
  val message: String,
  val recipients: List<String>
)