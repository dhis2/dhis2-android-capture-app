package org.dhis2.data.forms.dataentry

import com.nhaarman.mockitokotlin2.mock
import org.hisp.dhis.android.core.D2
import org.junit.Before

class ValueStoreTest {

    private lateinit var attrValueStore: ValueStore
    private lateinit var deValueStore: ValueStore
    private lateinit var dvValueStore: ValueStore
    private val d2: D2 = mock()

    @Before
    fun setUp() {
        attrValueStore = ValueStoreImpl(d2, "recordUid", DataEntryStore.EntryMode.ATTR)
        deValueStore = ValueStoreImpl(d2, "recordUid", DataEntryStore.EntryMode.DE)
        dvValueStore = ValueStoreImpl(d2, "recordUid", DataEntryStore.EntryMode.DV)
    }
}