package org.dhis2.usescases.sms.domain.repository.message

import org.dhis2.usescases.sms.data.model.MessageTemplate

interface MessageTemplateRepository {
  /**
   * Get the message template by language.
   *
   * @param language The language code.
   * @return A Maybe containing the message template if found, or None if not found.
   */
  suspend fun getByLanguage(language: String): Result<MessageTemplate>
}