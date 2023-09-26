package org.dhis2.form.data

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.option.Option

class OptionsRepository(
    private val d2: D2,
) {

    fun getOptionByDisplayName(optionSet: String, displayName: String): Option? = d2.optionModule()
        .options().byOptionSetUid().eq(optionSet)
        .byDisplayName().eq(displayName)
        .one().blockingGet()

    fun getOptionByCode(optionSet: String, code: String): Option? = d2.optionModule()
        .options().byOptionSetUid().eq(optionSet)
        .byCode().eq(code)
        .one().blockingGet()
}
