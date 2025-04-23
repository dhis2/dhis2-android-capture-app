package org.dhis2.usescases.sms.domain.repository.preferred

import org.dhis2.usescases.sms.domain.model.preffered.PreferredLanguage

interface PreferredLanguageRepository {
  fun getByCode(code: String): PreferredLanguage
}