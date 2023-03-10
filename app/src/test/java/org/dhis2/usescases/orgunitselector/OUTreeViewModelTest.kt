package org.dhis2.usescases.orgunitselector

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doReturnConsecutively
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import java.util.UUID
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
    fun `Should set top level org unit`() {
        val orgUnits = mutableListOf(dummyOrgUnit(1), dummyOrgUnit(2))

        whenever(
            repository.orgUnits()
        ) doReturn Single.just(orgUnits)

        whenever(
            repository.orgUnitHasChildren(orgUnits[0].uid())
        ) doReturn false

        whenever(
            repository.countSelectedChildren(orgUnits[0].uid(), emptyList())
        ) doReturn 0

        whenever(
            repository.orgUnits(orgUnits[0].uid())
        ) doReturn Single.just(mutableListOf())

        viewModel = OUTreeViewModel(repository, mutableListOf(), false, dispatchers)
        testingDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.treeNodes.value.size == 1)
        assertTrue(viewModel.treeNodes.value[0].uid == orgUnits[0].uid())
    }

    @Test
    fun `Should open and close children`() {
        val orgUnits = mutableListOf(dummyOrgUnit(1))
        val childOrgUnits = mutableListOf(dummyOrgUnit(2), dummyOrgUnit(2))
        val parentOrgUnit = orgUnits[0].uid()
        defaultViewModelInit(orgUnits, childOrgUnits)

        assertTrue(viewModel.treeNodes.value.size == 3)
        viewModel.onOpenChildren(parentOrgUnit)
        testingDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.treeNodes.value.size == 1)
    }

    @Test
    fun `Should search by name`() {
        val parentOrgUnit = dummyOrgUnit(1)
        val orgUnits = mutableListOf(parentOrgUnit)
        val childOrgUnits = mutableListOf(
            dummyOrgUnit(2, "ABC", parentOrgUnit),
            dummyOrgUnit(2, "DEF", parentOrgUnit)
        )
        val searchInput = "ABC"
        defaultViewModelInit(orgUnits, childOrgUnits)

        whenever(
            repository.orgUnits(searchInput)
        ) doReturn Single.just(mutableListOf(childOrgUnits[0]))

        whenever(
            repository.orgUnit(parentOrgUnit.uid())
        ) doReturn parentOrgUnit
        whenever(
            repository.orgUnit(childOrgUnits[0].uid())
        ) doReturn childOrgUnits[0]
        whenever(
            repository.orgUnitHasChildren(childOrgUnits[0].uid())
        ) doReturn false
        whenever(
            repository.countSelectedChildren(
                childOrgUnits[0].uid(),
                emptyList()
            )
        ) doReturn 0

        viewModel.searchByName(searchInput)
        testingDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.treeNodes.value.size == 2)
    }

    @Test
    fun `Should only check one item`() {
        val parentOrgUnit = dummyOrgUnit(1)
        val orgUnits = mutableListOf(parentOrgUnit)
        val childOrgUnits = mutableListOf(
            dummyOrgUnit(2, "ABC", parentOrgUnit),
            dummyOrgUnit(2, "DEF", parentOrgUnit)
        )
        defaultViewModelInit(orgUnits, childOrgUnits, true)

        assertTrue(
            viewModel.treeNodes.value.all { !it.selected }
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
        val orgUnits = mutableListOf(parentOrgUnit)
        val childOrgUnits = mutableListOf(
            dummyOrgUnit(2, "ABC", parentOrgUnit),
            dummyOrgUnit(2, "DEF", parentOrgUnit)
        )
        defaultViewModelInit(orgUnits, childOrgUnits)

        assertTrue(
            viewModel.treeNodes.value.all { !it.selected }
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
            dummyOrgUnit(2, "DEF", parentOrgUnit)
        )
        defaultViewModelInit(orgUnits, childOrgUnits)

        viewModel.onOrgUnitCheckChanged(childOrgUnits[0].uid(), true)
        testingDispatcher.scheduler.advanceUntilIdle()
        whenever(
            repository.orgUnit(childOrgUnits[0].uid())
        ) doReturn childOrgUnits[0]
        val result = viewModel.getOrgUnits()
        assertTrue(result.size == 1)
        assertTrue(result.first().uid() == childOrgUnits[0].uid())
    }

    private fun defaultViewModelInit(
        orgUnits: MutableList<OrganisationUnit>,
        childOrgUnits: MutableList<OrganisationUnit>,
        singleSelection: Boolean = false
    ) {
        whenever(
            repository.orgUnits(anyOrNull(), anyOrNull())
        ) doReturnConsecutively listOf(
            Single.just(orgUnits),
            Single.just(childOrgUnits)
        )

        whenever(
            repository.orgUnitHasChildren(any())
        ) doReturnConsecutively listOf(true, false, false)

        whenever(
            repository.countSelectedChildren(orgUnits[0].uid(), emptyList())
        ) doReturnConsecutively listOf(0, 0, 0)

        viewModel = OUTreeViewModel(repository, mutableListOf(), singleSelection, dispatchers)
        testingDispatcher.scheduler.advanceUntilIdle()
    }

    private fun dummyOrgUnit(
        level: Int,
        name: String = "name$level",
        parent: OrganisationUnit? = null
    ) =
        OrganisationUnit.builder()
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
