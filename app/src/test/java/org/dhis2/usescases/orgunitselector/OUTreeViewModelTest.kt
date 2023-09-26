package org.dhis2.usescases.orgunitselector

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.orgunitselector.OUTreeRepository
import org.dhis2.commons.orgunitselector.OUTreeViewModel
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

@ExperimentalCoroutinesApi
class OUTreeViewModelTest {

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: OUTreeViewModel
    private val repository: OUTreeRepository = mock()
    private val testingDispatcher = StandardTestDispatcher()
    private val dispatchers: DispatcherProvider = mock {
        on { io() } doReturn testingDispatcher
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Should set initial org units`() {
        val orgUnit1 = dummyOrgUnit(1)
        val orgUnit2 = dummyOrgUnit(2, parent = orgUnit1)
        val orgUnits = listOf(orgUnit1, orgUnit2)

        whenever(
            repository.orgUnits(),
        ) doReturn orgUnits

        whenever(
            repository.orgUnit(any()),
        ) doReturnConsecutively orgUnits

        whenever(
            repository.canBeSelected(any()),
        ) doReturnConsecutively listOf(true, true)

        whenever(
            repository.orgUnitHasChildren(any()),
        ) doReturnConsecutively listOf(true, false)

        whenever(
            repository.countSelectedChildren(any(), any()),
        ) doReturnConsecutively listOf(0, 0)

        viewModel = OUTreeViewModel(repository, mutableListOf(), false, dispatchers)
        testingDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.treeNodes.value.size == 2)
        assertTrue(viewModel.treeNodes.value[0].uid == orgUnits[0].uid())
        assertTrue(viewModel.treeNodes.value[1].uid == orgUnits[1].uid())
    }

    @Test
    fun `Should open and close children`() {
        val parentOrgUnit = dummyOrgUnit(1)
        val childOrgUnits = listOf(
            dummyOrgUnit(2, parent = parentOrgUnit),
            dummyOrgUnit(2, parent = parentOrgUnit),
        )
        val orgUnits = listOf(parentOrgUnit) + childOrgUnits
        defaultViewModelInit(orgUnits)
        assertTrue(viewModel.treeNodes.value.size == 3)

        whenever(repository.childrenOrgUnits(parentOrgUnit.uid())) doReturn childOrgUnits

        viewModel.onOpenChildren(parentOrgUnit.uid())
        testingDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.treeNodes.value.size == 1)
    }

    @Test
    fun `Should search by name`() {
        val parentOrgUnit = dummyOrgUnit(1)
        val childOrgUnits = listOf(
            dummyOrgUnit(2, "ABC", parentOrgUnit),
            dummyOrgUnit(2, "DEF", parentOrgUnit),
        )
        val orgUnits = listOf(parentOrgUnit) + childOrgUnits
        val searchInput = "ABC"

        defaultViewModelInit(orgUnits)

        whenever(
            repository.orgUnits(searchInput),
        ) doReturn listOf(parentOrgUnit, childOrgUnits[0])

        whenever(
            repository.orgUnit(childOrgUnits[0].uid()),
        ) doReturnConsecutively listOf(parentOrgUnit, childOrgUnits[0])

        whenever(
            repository.canBeSelected(any()),
        ) doReturnConsecutively listOf(true, true)

        whenever(
            repository.orgUnitHasChildren(any()),
        ) doReturnConsecutively listOf(true, false)

        whenever(
            repository.countSelectedChildren(any(), any()),
        ) doReturnConsecutively listOf(0, 0)

        viewModel.searchByName(searchInput)
        testingDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.treeNodes.value.size == 2)
    }

    @Test
    fun `Should only check one item`() {
        val parentOrgUnit = dummyOrgUnit(1)
        val childOrgUnits = mutableListOf(
            dummyOrgUnit(2, "ABC", parentOrgUnit),
            dummyOrgUnit(2, "DEF", parentOrgUnit),
        )
        val orgUnits = mutableListOf(parentOrgUnit) + childOrgUnits

        defaultViewModelInit(orgUnits, true)

        assertTrue(
            viewModel.treeNodes.value.all { !it.selected },
        )

        viewModel.onOrgUnitCheckChanged(parentOrgUnit.uid(), true)
        testingDispatcher.scheduler.advanceUntilIdle()
        viewModel.treeNodes.value.filter { it.selected }.let {
            assertTrue(it.size == 1)
            assertTrue(it[0].uid == parentOrgUnit.uid())
        }

        viewModel.onOrgUnitCheckChanged(childOrgUnits[1].uid(), true)
        testingDispatcher.scheduler.advanceUntilIdle()
        viewModel.treeNodes.value.filter { it.selected }.let {
            assertTrue(it.size == 1)
            assertTrue(it[0].uid == childOrgUnits[1].uid())
        }
    }

    @Test
    fun `Should only check multiple items`() {
        val parentOrgUnit = dummyOrgUnit(1)
        val childOrgUnits = mutableListOf(
            dummyOrgUnit(2, "ABC", parentOrgUnit),
            dummyOrgUnit(2, "DEF", parentOrgUnit),
        )
        val orgUnits = mutableListOf(parentOrgUnit) + childOrgUnits

        defaultViewModelInit(orgUnits)

        assertTrue(
            viewModel.treeNodes.value.all { !it.selected },
        )

        viewModel.onOrgUnitCheckChanged(parentOrgUnit.uid(), true)
        testingDispatcher.scheduler.advanceUntilIdle()
        viewModel.onOrgUnitCheckChanged(childOrgUnits[1].uid(), true)
        testingDispatcher.scheduler.advanceUntilIdle()
        viewModel.treeNodes.value.filter { it.selected }.let {
            assertTrue(it.size == 2)
            assertTrue(it[0].uid == parentOrgUnit.uid())
            assertTrue(it[1].uid == childOrgUnits[1].uid())
        }
    }

    @Test
    fun `Should return selected org units`() {
        val parentOrgUnit = dummyOrgUnit(1)
        val orgUnits = mutableListOf(parentOrgUnit)
        val childOrgUnits = mutableListOf(
            dummyOrgUnit(2, "ABC", parentOrgUnit),
            dummyOrgUnit(2, "DEF", parentOrgUnit),
        )
        defaultViewModelInit(orgUnits)

        viewModel.onOrgUnitCheckChanged(childOrgUnits[0].uid(), true)
        testingDispatcher.scheduler.advanceUntilIdle()
        whenever(
            repository.orgUnit(childOrgUnits[0].uid()),
        ) doReturn childOrgUnits[0]
        val result = viewModel.getOrgUnits()
        assertTrue(result.size == 1)
        assertTrue(result.first().uid() == childOrgUnits[0].uid())
    }

    private fun defaultViewModelInit(
        orgUnits: List<OrganisationUnit>,
        singleSelection: Boolean = false,
    ) {
        whenever(
            repository.orgUnits(),
        ) doReturn orgUnits

        whenever(
            repository.orgUnit(any()),
        ) doReturnConsecutively orgUnits

        whenever(
            repository.canBeSelected(any()),
        ) doReturnConsecutively listOf(true, true)

        whenever(
            repository.orgUnitHasChildren(any()),
        ) doReturnConsecutively listOf(true, false)

        whenever(
            repository.countSelectedChildren(any(), any()),
        ) doReturnConsecutively listOf(0, 0)

        viewModel = OUTreeViewModel(repository, mutableListOf(), singleSelection, dispatchers)
        testingDispatcher.scheduler.advanceUntilIdle()
    }

    private fun dummyOrgUnit(
        level: Int,
        name: String = "name$level",
        parent: OrganisationUnit? = null,
    ) = OrganisationUnit.builder()
        .uid(UUID.randomUUID().toString())
        .displayName(name)
        .level(level)
        .apply {
            parent?.let {
                parent(ObjectWithUid.create(it.uid()))
                    .path("/${it.uid()}/")
            }
        }
        .build()
}
