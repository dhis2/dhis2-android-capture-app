package org.dhis2.data.dhislogic

import javax.inject.Inject
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.event.Event

class DhisCategoryUtils @Inject constructor(val d2: D2) {
    fun getEventCatComboAccess(event: Event): Boolean {
        return if (event.attributeOptionCombo() != null) {
            val optionUid =
                UidsHelper.getUidsList(
                    d2.categoryModule()
                        .categoryOptionCombos().withCategoryOptions()
                        .uid(event.attributeOptionCombo())
                        .blockingGet().categoryOptions()
                )
            val options =
                d2.categoryModule().categoryOptions().byUid().`in`(optionUid).blockingGet()
            var access = true
            val eventDate = event.eventDate()
            for (option in options) {
                if (!option.access().data().write()) access = false
                if (eventDate != null && option.startDate() != null &&
                    eventDate.before(option.startDate())
                ) access =
                    false
                if (eventDate != null && option.endDate() != null &&
                    eventDate.after(option.endDate())
                ) access =
                    false
            }
            access
        } else true
    }
}
