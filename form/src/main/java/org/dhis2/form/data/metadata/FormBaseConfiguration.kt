package org.dhis2.form.data.metadata

import org.dhis2.commons.bindings.disableCollapsableSectionsInProgram
import org.hisp.dhis.android.core.D2

open class FormBaseConfiguration(private val d2: D2) {
    fun optionGroups(optionGroupUids: List<String>) = d2.optionModule().optionGroups()
        .withOptions()
        .byUid().`in`(optionGroupUids)
        .blockingGet()

    fun disableCollapsableSectionsInProgram(programUid: String) =
        d2.disableCollapsableSectionsInProgram(programUid)

    fun dateFormatConfiguration() =
        d2.systemInfoModule().systemInfo().blockingGet()?.dateFormat()
}
