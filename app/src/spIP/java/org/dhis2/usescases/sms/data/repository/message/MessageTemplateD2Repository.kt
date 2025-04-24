package org.dhis2.usescases.sms.data.repository.message

import org.dhis2.commons.di.dagger.PerServer
import org.dhis2.usescases.sms.data.api.ConstantApi
import org.dhis2.usescases.sms.data.model.MessageTemplate
import org.dhis2.usescases.sms.domain.repository.message.MessageTemplateRepository
import org.hisp.dhis.android.core.D2
import org.dhis2.usescases.sms.domain.types.Maybe
import org.hisp.dhis.android.core.D2Manager
import javax.inject.Inject

class MessageTemplateD2Repository @Inject constructor(
  private val constantApi: ConstantApi
) : MessageTemplateRepository {

  private val d2: D2 = D2Manager.getD2()

  override suspend fun getByLanguage(
    language: String
  ): Maybe<MessageTemplate> {

    val templateConstants = d2.constantModule().constants()
      .byName().eq("CMO_ENROLLMENT_TEMPLATE_$language").blockingGet()

    if (templateConstants.isEmpty()) return Maybe.None

    val templateConstant = templateConstants.first()
    val description = getDescriptionConstant(templateConstant.uid())

    if (description.isEmpty()) return Maybe.None

    val messageTemplate = MessageTemplate(text = description, language = language)
    return Maybe.Some(messageTemplate)
  }

  /**
   * Retrieves the description of a constant by its UID.
   *
   * @param uid The UID of the constant to retrieve.
   * @return The description of the constant, or an empty string if not found.
   */
  private suspend fun getDescriptionConstant(uid: String): String {
    val body = constantApi.getConstant(uid)
    return body?.description ?: ""
  }

}