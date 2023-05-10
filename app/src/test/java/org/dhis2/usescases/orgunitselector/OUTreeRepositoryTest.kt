package org.dhis2.usescases.orgunitselector

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import java.util.UUID
import org.dhis2.commons.orgunitselector.OURepositoryConfiguration
import org.dhis2.commons.orgunitselector.OUTreeRepository
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.junit.Assert.assertTrue
import org.junit.Test

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
        assertTrue(result == orgUnits.map { it.uid() })
    }

    @Test
    fun `Should return all children orgUnits`() {
        val orgUnits = mutableListOf(dummyOrgUnit(), dummyOrgUnit(), dummyOrgUnit())
        val parentUid = UUID.randomUUID().toString()

        val repository = OUTreeRepository(ouRepositoryConfiguration)

        whenever(
            ouRepositoryConfiguration.orgUnitRepository(name = anyOrNull())
        ) doReturn orgUnits

        whenever(
            ouRepositoryConfiguration.childrenOrgUnits(any())
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
        assertTrue(result == orgUnits.map { it.uid() })
    }

    @Test
    fun `Should return organisation unit`() {
        val orgUnit = dummyOrgUnit()
        val repository = OUTreeRepository(ouRepositoryConfiguration)

        whenever(
            ouRepositoryConfiguration.orgUnit(orgUnit.uid())
        ) doReturn orgUnit

        assert(repository.orgUnit(orgUnit.uid()) == orgUnit)
    }

    @Test
    fun `Should return if organisation unit has children`() {
        val parentUid = UUID.randomUUID().toString()
        val repository = OUTreeRepository(ouRepositoryConfiguration)

        whenever(
            ouRepositoryConfiguration.hasChildren(
                parentUid = any(),
                byScope = any()
            )
        ) doReturn true

        assert(repository.orgUnitHasChildren(parentUid))
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
}
