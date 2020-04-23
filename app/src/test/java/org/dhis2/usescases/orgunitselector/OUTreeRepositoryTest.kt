package org.dhis2.usescases.orgunitselector

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import java.util.UUID
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class OUTreeRepositoryTest {

    private lateinit var repository: OUTreeRepository
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Before
    fun setUp() {
        repository = OUTreeRepository(d2)
    }

    @Test
    fun `Should return all root orgUnits`() {
        val orgUnits = mutableListOf(dummyOrgUnit(), dummyOrgUnit(), dummyOrgUnit())

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .byRootOrganisationUnit(true)
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                .blockingCount()
        ) doReturn orgUnits.size

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .byRootOrganisationUnit(true)
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                .get()
        ) doReturn Single.just(orgUnits)

        val testObserver = repository.orgUnits().test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(orgUnits)

        testObserver.dispose()
    }

    @Test
    fun `Should return all orgUnits of dataCapture`() {
        val orgUnits = mutableListOf(dummyOrgUnit(), dummyOrgUnit(), dummyOrgUnit())

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .byRootOrganisationUnit(true)
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                .blockingCount()
        ) doReturn 0

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .byRootOrganisationUnit(true)
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .get()
        ) doReturn Single.just(orgUnits)

        val testObserver = repository.orgUnits().test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(orgUnits)

        testObserver.dispose()
    }

    @Test
    fun `Should return all children orgUnits`() {
        val orgUnits = mutableListOf(dummyOrgUnit(), dummyOrgUnit(), dummyOrgUnit())
        val parentUid = UUID.randomUUID().toString()

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .byParentUid()
        ) doReturn mock()

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .byParentUid().eq(parentUid)
        ) doReturn mock()

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .byParentUid().eq(parentUid)
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
        ) doReturn mock()

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .byParentUid().eq(parentUid)
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                .blockingCount()
        ) doReturn orgUnits.size

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .byParentUid().eq(parentUid)
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                .get()
        ) doReturn Single.just(orgUnits)

        val testObserver = repository.orgUnits(parentUid).test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(orgUnits)

        testObserver.dispose()
    }

    @Test
    fun `Should return all orgUnits that contains name`() {
        val orgUnits = mutableListOf(dummyOrgUnit(), dummyOrgUnit(), dummyOrgUnit())
        val name = "name"

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .byDisplayName().like("%$name%")
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                .blockingCount()
        ) doReturn orgUnits.size

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .byDisplayName().like("%$name%")
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_TEI_SEARCH)
                .get()
        ) doReturn Single.just(orgUnits)

        val testObserver = repository.orgUnits(name = name).test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(orgUnits)

        testObserver.dispose()
    }

    @Test
    fun `Should return organisation unit`() {
        val orgUnit = dummyOrgUnit()

        whenever(
            d2.organisationUnitModule().organisationUnits().uid(orgUnit.uid()).blockingGet()
        ) doReturn orgUnit

        assert(repository.orgUnit(orgUnit.uid()) == orgUnit)
    }

    @Test
    fun `Should return if organisation unit has children`() {
        val parentUid = UUID.randomUUID().toString()

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
