package org.dhis2.usescases.orgunitselector

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.orgunitselector.OUTreeModel
import org.dhis2.commons.orgunitselector.OUTreeRepository
import org.dhis2.commons.orgunitselector.OUTreeViewModel
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class OUTreeViewModelTest {
    private lateinit var viewModel: OUTreeViewModel
    private val repository: OUTreeRepository = mock()
    private lateinit var testingDispatcher: TestDispatcher
    private val dispatchers: DispatcherProvider =
        object : DispatcherProvider {
            override fun io() = testingDispatcher

            override fun ui() = testingDispatcher

            override fun computation() = testingDispatcher
        }

    @Before
    fun setUp() =
        runTest {
            testingDispatcher = StandardTestDispatcher(testScheduler)
            Dispatchers.setMain(testingDispatcher)
        }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Should set initial org units`() =
        runTest {
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

            viewModel = OUTreeViewModel(repository, mutableListOf(), false, OUTreeModel(), dispatchers)

            viewModel.treeNodes.test {
                awaitItem()
                with(awaitItem()) {
                    assertTrue(size == 2)
                    assertTrue(get(0).uid == orgUnits[0].uid())
                    assertTrue(get(1).uid == orgUnits[1].uid())
                }
            }
        }

    @Test
    fun `Should open and close children`() =
        runTest {
            val parentOrgUnit = dummyOrgUnit(1)
            val childOrgUnits =
                listOf(
                    dummyOrgUnit(2, parent = parentOrgUnit),
                    dummyOrgUnit(2, parent = parentOrgUnit),
                )
            val orgUnits = listOf(parentOrgUnit) + childOrgUnits

            whenever(repository.childrenOrgUnits(parentOrgUnit.uid())) doReturn childOrgUnits

            defaultViewModelInit(orgUnits)
            viewModel.treeNodes.test {
                awaitItem()
                assertTrue(awaitItem().size == 3)
                viewModel.onOpenChildren(parentOrgUnit.uid())
                assertTrue(awaitItem().size == 1)
            }
        }

    @Test
    fun `Should search by name`() =
        runTest {
            val parentOrgUnit = dummyOrgUnit(1)
            val childOrgUnits =
                listOf(
                    dummyOrgUnit(2, "ABC", parentOrgUnit),
                    dummyOrgUnit(2, "DEF", parentOrgUnit),
                )
            val orgUnits = listOf(parentOrgUnit) + childOrgUnits
            val searchInput = "ABC"

            whenever(
                repository.orgUnits(null),
            ) doReturn listOf(parentOrgUnit, childOrgUnits[0])

            whenever(
                repository.orgUnits(searchInput),
            ) doReturn listOf(parentOrgUnit, childOrgUnits[0])

            whenever(
                repository.canBeSelected(any()),
            ) doReturnConsecutively listOf(true, true)

            whenever(
                repository.orgUnitHasChildren(any()),
            ) doReturnConsecutively listOf(true, false)

            whenever(
                repository.countSelectedChildren(any(), any()),
            ) doReturnConsecutively listOf(0, 0)

            defaultViewModelInit(orgUnits)

            viewModel.treeNodes.test {
                awaitItem()
                assertTrue(awaitItem().size == 3)
                viewModel.searchByName(searchInput)
                assertTrue(awaitItem().size == 2)
            }
        }

    @Test
    fun `Should only check one item`() =
        runTest {
            val parentOrgUnit = dummyOrgUnit(1)
            val childOrgUnits =
                mutableListOf(
                    dummyOrgUnit(2, "ABC", parentOrgUnit),
                    dummyOrgUnit(2, "DEF", parentOrgUnit),
                )
            val orgUnits = mutableListOf(parentOrgUnit) + childOrgUnits

            defaultViewModelInit(orgUnits, true)

            viewModel.treeNodes.test {
                awaitItem()
                with(awaitItem()) {
                    assertTrue(
                        all { !it.selected },
                    )
                }
                viewModel.onOrgUnitCheckChanged(parentOrgUnit.uid(), true)
                with(awaitItem().filter { it.selected }) {
                    assertTrue(size == 1)
                    assertTrue(first().uid == parentOrgUnit.uid())
                }
                viewModel.onOrgUnitCheckChanged(childOrgUnits[1].uid(), true)
                with(awaitItem().filter { it.selected }) {
                    assertTrue(size == 1)
                    assertTrue(first().uid == childOrgUnits[1].uid())
                }
            }
        }

    @Test
    fun `Should only check multiple items`() =
        runTest {
            val parentOrgUnit = dummyOrgUnit(1)
            val childOrgUnits =
                mutableListOf(
                    dummyOrgUnit(2, "ABC", parentOrgUnit),
                    dummyOrgUnit(2, "DEF", parentOrgUnit),
                )
            val orgUnits = mutableListOf(parentOrgUnit) + childOrgUnits

            defaultViewModelInit(orgUnits)

            viewModel.treeNodes.test {
                awaitItem()
                assertTrue(awaitItem().all { !it.selected })
                viewModel.onOrgUnitCheckChanged(parentOrgUnit.uid(), true)
                awaitItem()
                viewModel.onOrgUnitCheckChanged(childOrgUnits[1].uid(), true)
                with(awaitItem().filter { it.selected }) {
                    assertTrue(size == 2)
                    assertTrue(get(0).uid == parentOrgUnit.uid())
                    assertTrue(get(1).uid == childOrgUnits[1].uid())
                }
            }
        }

    @Test
    fun `Should return selected org units`() =
        runTest {
            val parentOrgUnit = dummyOrgUnit(1)
            val childOrgUnits =
                mutableListOf(
                    dummyOrgUnit(2, "ABC", parentOrgUnit),
                    dummyOrgUnit(2, "DEF", parentOrgUnit),
                )

            defaultViewModelInit(listOf(parentOrgUnit))

            // Mock repository responses
            whenever(repository.orgUnit(childOrgUnits.first().uid())) doReturn childOrgUnits[0]
            whenever(repository.countSelectedChildren(any(), any())) doReturn 0
            whenever(repository.canBeSelected(any())) doReturn true
            whenever(repository.orgUnitHasChildren(any())) doReturn false

            viewModel.finalSelectedOrgUnits.test {
                assertTrue(awaitItem().isEmpty())

                viewModel.onOrgUnitCheckChanged(childOrgUnits[0].uid(), true)
                testScheduler.advanceUntilIdle()
                viewModel.confirmSelection()

                with(awaitItem()) {
                    assertTrue(size == 1)
                    assertTrue(first().uid() == childOrgUnits[0].uid())
                }
                ensureAllEventsConsumed()
            }
        }

    @Test
    fun shoudSearchOnlyIfMoreThanTwoCharacters() =
        runTest {
            val parentOrgUnit = dummyOrgUnit(1)
            defaultViewModelInit(listOf(parentOrgUnit))
            viewModel.searchByName("a")
            testScheduler.advanceUntilIdle()
            verify(repository, times(0)).orgUnits("a")
        }

    @Test
    fun shouldFilterOrgUnitsToHide() =
        runTest {
            val orgUnits =
                listOf(
                    dummyOrgUnit(1, "orgUnit0"),
                    dummyOrgUnit(1, "orgUnit1"),
                    dummyOrgUnit(1, "orgUnit2"),
                )
            defaultViewModelInit(
                orgUnits = orgUnits,
                model =
                    OUTreeModel(
                        hideOrgUnits =
                            listOf(
                                dummyOrgUnit(1, "orgUnit1"),
                            ),
                    ),
            )

            viewModel.treeNodes.test {
                awaitItem()
                with(awaitItem()) {
                    assertTrue(size == 2)
                    assertTrue(get(0).uid == orgUnits[0].uid())
                    assertTrue(get(1).uid == orgUnits[2].uid())
                }
            }
        }

    @Test
    fun shouldClearAllSelection() =
        runTest {
            val parentOrgUnit = dummyOrgUnit(1)
            val childOrgUnits =
                mutableListOf(
                    dummyOrgUnit(2, "ABC", parentOrgUnit),
                    dummyOrgUnit(2, "DEF", parentOrgUnit),
                )
            val orgUnits = mutableListOf(parentOrgUnit) + childOrgUnits

            defaultViewModelInit(orgUnits)

            viewModel.treeNodes.test {
                awaitItem()
                assertTrue(awaitItem().all { !it.selected })
                viewModel.onOrgUnitCheckChanged(parentOrgUnit.uid(), true)
                awaitItem()
                viewModel.onOrgUnitCheckChanged(childOrgUnits[1].uid(), true)
                with(awaitItem().filter { it.selected }) {
                    assertTrue(size == 2)
                    assertTrue(get(0).uid == parentOrgUnit.uid())
                    assertTrue(get(1).uid == childOrgUnits[1].uid())
                }
                viewModel.clearAll()
                assertTrue(awaitItem().none { it.selected })
            }
        }

    private fun defaultViewModelInit(
        orgUnits: List<OrganisationUnit>,
        singleSelection: Boolean = false,
        model: OUTreeModel = OUTreeModel(),
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

        viewModel =
            OUTreeViewModel(
                repository,
                mutableListOf(),
                singleSelection,
                model,
                dispatchers,
            )
    }

    private fun dummyOrgUnit(
        level: Int,
        name: String = "name$level",
        parent: OrganisationUnit? = null,
    ) = OrganisationUnit
        .builder()
        .uid("orgUnitUid$name")
        .displayName(name)
        .level(level)
        .apply {
            parent?.let {
                parent(ObjectWithUid.create(it.uid()))
                    .path("/${it.uid()}/")
            }
        }.build()
}
