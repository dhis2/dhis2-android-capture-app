package org.dhis2.community.common

import org.hisp.dhis.android.core.D2


class NamespaceQuery(private val d2: D2) {

    fun getCommunityNamespace() {
        return
//        return d2.dataStoreModule().dataStore().byNamespace().eq("DFFF").get().flatMap { it.get(0).value() }
    }
}