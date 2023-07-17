package org.dhis2.usescases.orgunitselector

import java.util.UUID
import org.dhis2.commons.orgunitselector.OURepositoryConfiguration
import org.dhis2.commons.orgunitselector.OUTreeRepository
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class OUTreeRepositoryTest {

    private val ouRepositoryConfiguration: OURepositoryConfiguration = mock()

    @Test
    fun `Should return initial orgUnits`() {
        val orgUnits = listOf(dummyOrgUnit(level = 1))
        val repository = OUTreeRepository(ouRepositoryConfiguration)

        whenever(
            ouRepositoryConfiguration.orgUnitRepository(name = anyOrNull())
        ) doReturn mock()

        whenever(
            ouRepositoryConfiguration.orgUnitRepository(name = anyOrNull())
        ) doReturn orgUnits

        val result = repository.orgUnits()

        assertTrue(result.isNotEmpty())
        assertTrue(result == orgUnits)
    }

    @Test
    fun `Should return all children orgUnits`() {
        val parentUid = UUID.randomUUID().toString()
        val orgUnits = mutableListOf(
            dummyOrgUnit(parents = listOf(parentUid)),
            dummyOrgUnit(parents = listOf(parentUid)),
            dummyOrgUnit(parents = listOf(parentUid))
        )

        val repository = OUTreeRepository(ouRepositoryConfiguration)

        whenever(
            ouRepositoryConfiguration.orgUnitRepository(name = anyOrNull())
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

        val repository = OUTreeRepository(ouRepositoryConfiguration)

        whenever(
            ouRepositoryConfiguration.orgUnitRepository(name = name)
        ) doReturn orgUnits

        val result = repository.orgUnits(name)

        assertTrue(result.isNotEmpty())
        assertTrue(result == orgUnits)
    }

    @Test
    fun `Should return organisation unit`() {
        val orgUnit = dummyOrgUnit()
        val repository = OUTreeRepository(ouRepositoryConfiguration)

        whenever(
            ouRepositoryConfiguration.orgUnitRepository(name = anyOrNull())
        ) doReturn listOf(orgUnit)

        repository.orgUnits()

        assert(repository.orgUnit(orgUnit.uid()) == orgUnit)
    }

    @Test
    fun `Should return if organisation unit has children`() {
        val parentUid = UUID.randomUUID().toString()
        val repository = OUTreeRepository(ouRepositoryConfiguration)
        val orgUnits = mutableListOf(
            dummyOrgUnit(parents = listOf(parentUid)),
            dummyOrgUnit(parents = listOf(parentUid)),
            dummyOrgUnit(parents = listOf(parentUid))
        )
        whenever(
            ouRepositoryConfiguration.orgUnitRepository(name = anyOrNull())
        ) doReturn orgUnits

        repository.orgUnits()

        assert(repository.orgUnitHasChildren(parentUid))
    }

    @Test
    fun `Should return initial orgUnits ordered to display`() {
        val ou1 = dummyOrderOrgUnit(uid = "ou1", level = 1)
        val ou11 = dummyOrderOrgUnit(parents = listOf("ou1"), uid = "ou11", level = 2)
        val ou12 = dummyOrderOrgUnit(parents = listOf("ou1"), uid = "ou12", level = 2)
        val ou13 = dummyOrderOrgUnit(parents = listOf("ou1"), uid = "ou13", level = 2)
        val ou121 = dummyOrderOrgUnit(parents = listOf("ou1", "ou12"), uid = "ou121", level = 3)
        val ou122 = dummyOrderOrgUnit(parents = listOf("ou1", "ou12"), uid = "ou122", level = 3)

        val orgUnits = listOf(ou1, ou11, ou12, ou13, ou121, ou122)

        val orderedOus = listOf(ou1, ou11, ou12, ou121, ou122, ou13)
        val repository = OUTreeRepository(ouRepositoryConfiguration)

        whenever(
            ouRepositoryConfiguration.orgUnitRepository(name = anyOrNull())
        ) doReturn orgUnits

        val result = repository.orgUnits()

        assertTrue(result.isNotEmpty())
        assertTrue(result == orderedOus)
    }

    @Test
    fun `Should return initial orgUnits with parents`() {
        val ou1 = dummyOrderOrgUnit(uid = "ou1", level = 1)
        val ou12 = dummyOrderOrgUnit(parents = listOf("ou1"), uid = "ou12", level = 2)
        val ou121 = dummyOrderOrgUnit(parents = listOf("ou1", "ou12"), uid = "ou121", level = 3)
        val ou122 = dummyOrderOrgUnit(parents = listOf("ou1", "ou12"), uid = "ou122", level = 3)

        val orgUnits = listOf(ou121, ou122)

        val ousWithParents = listOf(ou1, ou12, ou121, ou122)
        val repository = OUTreeRepository(ouRepositoryConfiguration)

        whenever(
            ouRepositoryConfiguration.orgUnitRepository(name = anyOrNull())
        ) doReturn orgUnits
        whenever(
            ouRepositoryConfiguration.orgUnit("ou1")
        ) doReturn ou1
        whenever(
            ouRepositoryConfiguration.orgUnit("ou12")
        ) doReturn ou12

        val result = repository.orgUnits()

        assertTrue(result.isNotEmpty())
        assertTrue(result == ousWithParents)
    }

    private fun dummyOrgUnit(
        parents: List<String> = emptyList(),
        uid: String = UUID.randomUUID().toString(),
        level: Int = 1
    ) = OrganisationUnit.builder()
        .uid(uid)
        .level(level)
        .path((parents + uid).joinToString("/"))
        .build()

    private fun dummyOrderOrgUnit(
        parents: List<String> = emptyList(),
        uid: String = UUID.randomUUID().toString(),
        level: Int = 1
    ) = OrganisationUnit.builder()
        .uid(uid)
        .level(level)
        .path((parents + uid).joinToString("/"))
        .displayNamePath(parents.plus(uid))
        .build()
}
