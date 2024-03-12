package org.dhis2.android.rtsm.data

import org.hisp.dhis.android.core.dataelement.DataElement

object DataElementFactory {

    fun create(uid: String, name: String): DataElement {
        return DataElement.builder()
            .id(55)
            .uid(uid)
            .name(name)
            .displayName(name).build()
    }
}
