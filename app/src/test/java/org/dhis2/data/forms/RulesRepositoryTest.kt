package org.dhis2.data.forms

import com.nhaarman.mockitokotlin2.whenever
import org.hisp.dhis.android.core.D2
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito

class RulesRepositoryTest {

    private lateinit var repository: RulesRepository

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Before
    fun setUp() {

        repository = RulesRepository(d2)
    }

    @Test
    fun `Should load supplementary data`() {
        whenever(d2.databaseAdapter().query("SELECT organisationUnitGroup, organisationUnit FROM OrganisationUnitOrganisationUnitGroupLink"))
    }
}