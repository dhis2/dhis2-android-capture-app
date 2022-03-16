package org.dhis2.form.ui.provider

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

class DisplayNameProviderImplTest {

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val displayNameProvider = DisplayNameProviderImpl(d2)

    @Test
    fun `Should return null if value is null`() {
        val result =
            displayNameProvider.provideDisplayName(ValueType.ORGANISATION_UNIT, null)
        assertTrue(result == null)
    }

    @Test
    fun `Should return org unit name`() {
        val testingOrgUnitUid = "orgUnitUid"
        val testingOrgUnitName = "orgUnitName"
        whenever(
            d2.organisationUnitModule()
                .organisationUnits()
                .uid(testingOrgUnitUid)
                .blockingGet()
        ) doReturn OrganisationUnit.builder()
            .uid(testingOrgUnitUid)
            .displayName(testingOrgUnitName)
            .build()
        val result =
            displayNameProvider.provideDisplayName(ValueType.ORGANISATION_UNIT, testingOrgUnitUid)
        assertTrue(result == testingOrgUnitName)
    }

    @Test
    fun `Should return value if org unit is not found`() {
        val testingOrgUnitUid = "orgUnitUid"

        whenever(
            d2.organisationUnitModule()
                .organisationUnits()
                .uid(testingOrgUnitUid)
                .blockingGet()
        ) doReturn null
        val result =
            displayNameProvider.provideDisplayName(ValueType.ORGANISATION_UNIT, testingOrgUnitUid)
        assertTrue(result == testingOrgUnitUid)
    }
}
