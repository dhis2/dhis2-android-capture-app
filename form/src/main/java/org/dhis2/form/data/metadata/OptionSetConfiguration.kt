package org.dhis2.form.data.metadata

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.option.Option

class OptionSetConfiguration(val d2: D2) {
    fun optionInDataSetByCode(optionSetUid: String, optionCode: String): Option? =
        d2.optionModule().options()
            .byOptionSetUid().eq(optionSetUid)
            .byCode().eq(optionCode)
            .one()
            .blockingGet()

    fun optionInDataSetByName(optionSetUid: String, optionName: String): Option? =
        d2.optionModule().options()
            .byOptionSetUid().eq(optionSetUid)
            .byName().eq(optionName)
            .one()
            .blockingGet()
}
