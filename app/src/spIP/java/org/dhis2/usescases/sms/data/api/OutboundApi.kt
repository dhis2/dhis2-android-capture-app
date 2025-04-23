package org.dhis2.usescases.sms.data.api


import org.dhis2.usescases.sms.data.model.OutboundRequest
import org.dhis2.usescases.sms.data.model.OutboundResponse
import org.hisp.dhis.android.core.arch.api.HttpServiceClient

interface OutboundApi {
  suspend fun sendSms(request: OutboundRequest): OutboundResponse
}

class OutboundApiImpl(
  private val client: HttpServiceClient
) : OutboundApi{

  override suspend fun sendSms(request: OutboundRequest): OutboundResponse {
    return client.post {
      url("sms/outbound")
      body(request)
    }
  }

}