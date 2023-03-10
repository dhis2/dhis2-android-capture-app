package org.dhis2.usescases.orgunitselector

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import java.util.UUID
import org.dhis2.commons.orgunitselector.OUTreeRepository
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

class OUTreeRepositoryTest {

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Test
    fun `Should return initial orgUnits`() {
        val orgUnits = listOf(dummyOrgUnit(), dummyOrgUnit(), dummyOrgUnit())
        val repository = OUTreeRepository(d2, OrgUnitSelectorScope.UserSearchScope())

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .blockingGet()
        ) doReturn orgUnits

        val result = repository.orgUnits()

        assertTrue(result.isNotEmpty())
        assertTrue(result == orgUnits.map { it.uid() })
    }

    @Test
    fun `Should return all orgUnits of dataCapture`() {
        val orgUnits = mutableListOf(dummyOrgUnit(), dummyOrgUnit(), dummyOrgUnit())
        val repository = OUTreeRepository(d2, OrgUnitSelectorScope.ProgramCaptureScope("uid"))
        whenever(
            d2.organisationUnitModule().organisationUnits()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byProgramUids(listOf("uid"))
                .blockingGet()
        ) doReturn orgUnits

        val result = repository.orgUnits()

        assertTrue(result.isNotEmpty())
        assertTrue(result == orgUnits.map { it.uid() })
    }

    @Test
    fun `Should return all children orgUnits`() {
        val orgUnits = mutableListOf(dummyOrgUnit(), dummyOrgUnit(), dummyOrgUnit())
        val parentUid = UUID.randomUUID().toString()

        val repository = OUTreeRepository(d2, OrgUnitSelectorScope.UserSearchScope())

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .blockingGet()
        ) doReturn orgUnits

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byParentUid()
        ) doReturn mock()

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byParentUid().eq(parentUid)
        ) doReturn mock()

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byParentUid().eq(parentUid)
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
        ) doReturn mock()

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byParentUid().eq(parentUid)
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .blockingGet()
        ) doReturn orgUnits

        repository.orgUnits()
        val result = repository.childrenOrgUnits(parentUid)

        assertTrue(result.isNotEmpty())
        assertTrue(result == orgUnits)
    }

    @Test
    fun `Should return all orgUnits that contains name`() {
        val orgUnits = mutableListOf(dummyOrgUnit(), dummyOrgUnit(), dummyOrgUnit())
        val name = "name"

        val repository = OUTreeRepository(d2, OrgUnitSelectorScope.UserSearchScope())

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .byDisplayName().like("%$name%")
                .blockingGet()
        ) doReturn orgUnits

        val result = repository.orgUnits(name)

        assertTrue(result.isNotEmpty())
        assertTrue(result == orgUnits.map { it.uid() })
    }

    @Test
    fun `Should return organisation unit`() {
        val orgUnit = dummyOrgUnit()
        val repository = OUTreeRepository(d2, OrgUnitSelectorScope.UserSearchScope())

        whenever(
            d2.organisationUnitModule().organisationUnits().uid(orgUnit.uid()).blockingGet()
        ) doReturn orgUnit

        assert(repository.orgUnit(orgUnit.uid()) == orgUnit)
    }

    @Test
    fun `Should return if organisation unit has children`() {
        val parentUid = UUID.randomUUID().toString()
        val repository = OUTreeRepository(d2, OrgUnitSelectorScope.UserSearchScope())

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byParentUid()
        ) doReturn mock()

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byParentUid().eq(parentUid)
        ) doReturn mock()

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byParentUid().eq(parentUid)
                .blockingIsEmpty()
        ) doReturn false

        assert(repository.orgUnitHasChildren(parentUid))
    }

    private fun dummyOrgUnit() =
        OrganisationUnit.builder()
            .uid(UUID.randomUUID().toString())
            .build()
}
