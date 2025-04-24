package org.dhis2.usescases.sms.data.repository.preferred

import org.dhis2.commons.di.dagger.PerServer
import org.dhis2.usescases.sms.domain.model.preffered.PreferredLanguage
import org.dhis2.usescases.sms.domain.repository.preferred.PreferredLanguageRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.option.Option
import javax.inject.Inject

const val optionSet = "Bvg8UG9v84X"

class PreferredLanguageD2Repository @Inject constructor() : PreferredLanguageRepository {

  private val d2: D2 = D2Manager.getD2()

  /**
   * Retrieves a PreferredLanguage object by its code.
   *
   * @param code The code of the preferred language to retrieve.
   * @return The PreferredLanguage object corresponding to the provided code.
   * @throws IllegalArgumentException if no option is found with the given code.
   */
  override fun getByCode(
    code: String
  ): PreferredLanguage {
    val options = d2.optionModule().options()
      .byOptionSetUid().eq(optionSet)
      .byCode().eq(code)
      .blockingGet()

    val option = options.firstOrNull()
      ?: throw IllegalArgumentException("No Option found with code: $code")

    return buildPreferredLanguage(option, code)
  }

  /**
   * Constructs a PreferredLanguage object from the provided Option object.
   *
   * @param option The Option object containing language data.
   * @param code The code of the preferred language.
   * @return A PreferredLanguage object populated with data from the Option.
   */
  private fun buildPreferredLanguage(
    option: Option,
    code: String
  ) = PreferredLanguage(
    uid = option.uid(),
    code = code,
    name = option.displayName() ?: code
  )

}