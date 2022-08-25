package org.dhis2.android.rtsm.utils

import org.dhis2.android.rtsm.data.DestinationFactory
import org.dhis2.android.rtsm.data.FacilityFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ParcelUtilsTest {
    @Test
    fun canCreateIdentifiableModelFromFacility() {
        val ou = FacilityFactory.getListOf(1).first()
        val parcel = ParcelUtils.facilityToIdentifiableModelParcel(ou)

        assertEquals(ou.uid(), parcel.uid)
        assertNotNull(parcel.name)
        assertEquals(ou.name(), parcel.name)
        assertEquals(ou.displayName(), parcel.displayName)
    }

    @Test
    fun canCreateIdentifiableModelFromDestinedTo() {
        val option = DestinationFactory.getListOf(1).first()
        val parcel = ParcelUtils.distributedTo_ToIdentifiableModelParcel(option)

        assertEquals(option.uid(), parcel.uid)
        assertNotNull(parcel.name)
        assertEquals(option.name(), parcel.name)
        assertEquals(option.displayName(), parcel.displayName)
    }
}