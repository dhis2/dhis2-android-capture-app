package org.dhis2.android.rtsm.data

import org.hisp.dhis.android.core.dataelement.DataElement

object DataElementFactory {
    fun create(
        uid: String,
        name: String,
    ): DataElement =
        DataElement
            .builder()
            .uid(uid)
            .name(name)
            .displayName(name)
            .build()
}
