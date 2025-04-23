package org.dhis2.usescases.sms.data.api

import io.ktor.client.call.body
import org.dhis2.usescases.sms.data.model.D2Constant
import org.hisp.dhis.android.core.arch.api.HttpServiceClient

interface ConstantApi {
  /**
   * Fetches a constant by its ID.
   *
   * @param id The ID of the constant to fetch.
   * @param fields The fields to include in the response. Default is "id,name,description".
   * @return The [D2Constant] object representing the constant.
   */
  suspend fun getConstant(
    id: String,
    fields: String = "id,name,description"
  ): D2Constant?
}

class ConstantApiImpl(
  private val client: HttpServiceClient
) : ConstantApi{

  override suspend fun getConstant(
    id: String,
    fields: String
  ): D2Constant? {
    return client.get {
      url("\"constants/$id")
      parameters {
        attribute("fields", fields)
      }
    }
  }
}