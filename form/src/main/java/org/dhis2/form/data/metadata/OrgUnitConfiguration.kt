package org.dhis2.form.data.metadata

import org.hisp.dhis.android.core.D2

class OrgUnitConfiguration(val d2: D2) {
    fun orgUnitByUid(uid: String) = d2.organisationUnitModule()
        .organisationUnits()
        .uid(uid)
        .blockingGet()
}
