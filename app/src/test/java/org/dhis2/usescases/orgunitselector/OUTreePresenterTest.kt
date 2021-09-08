package org.dhis2.usescases.orgunitselector

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import java.util.UUID
import org.dhis2.commons.orgunitselector.OUTreePresenter
import org.dhis2.commons.orgunitselector.OUTreeRepository
import org.dhis2.commons.orgunitselector.OUTreeView
import org.dhis2.commons.orgunitselector.TreeNode
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.junit.Before
import org.junit.Test

class OUTreePresenterTest {

    private lateinit var presenter: OUTreePresenter
    private val view: OUTreeView = mock()
    private val repository: OUTreeRepository = mock()
    private val scheduler = TrampolineSchedulerProvider()

    @Before
    fun setUp() {
        presenter = OUTreePresenter(view, repository, scheduler, emptyList())
    }

    @Test
    fun `Should set all orgUnits`() {
        val orgUnits = mutableListOf(dummyOrgUnit(1), dummyOrgUnit(2))

        whenever(
            repository.orgUnits()
        ) doReturn Single.just(orgUnits)

        whenever(
            repository.orgUnitHasChildren(orgUnits[0].uid())
        ) doReturn false

        presenter.init()

        verify(view).setOrgUnits(listOf(treeNode(orgUnits[0])))
    }

    @Test
    fun `Should add children orgUnits`() {
        val parent = dummyOrgUnit(1)
        val orgUnits = mutableListOf(dummyOrgUnit(2), dummyOrgUnit(2))

        whenever(
            repository.orgUnits(parent.uid())
        ) doReturn Single.just(orgUnits)

        whenever(
            repository.orgUnitHasChildren(orgUnits[0].uid())
        ) doReturn false

        whenever(
            repository.orgUnitHasChildren(orgUnits[1].uid())
        ) doReturn false

        whenever(
            view.getCurrentList()
        )doReturn listOf(TreeNode(parent))

        presenter.init()
        presenter.ouChildListener.onNext(Pair(0, parent))

        verify(view).setOrgUnits(any())
    }

    @Test
    fun `Should set orgUnits filtered by name with parents`() {
        val name = "name"
        val orgUnit = OrganisationUnit.builder()
            .uid("childUid")
            .path("/parentUid/childUid")
            .level(2)
            .build()
        val parentOrgUnit = OrganisationUnit.builder()
            .uid("parentUid")
            .level(1)
            .build()
        val orgUnits = mutableListOf(orgUnit)

        whenever(
            repository.orgUnits(name = name)
        ) doReturn Single.just(orgUnits)

        whenever(
            repository.orgUnit(parentOrgUnit.uid())
        ) doReturn parentOrgUnit

        whenever(
            repository.orgUnit(orgUnit.uid())
        ) doReturn orgUnit

        whenever(
            repository.orgUnitHasChildren(parentOrgUnit.uid())
        ) doReturn false

        whenever(
            repository.orgUnitHasChildren(orgUnit.uid())
        ) doReturn false

        presenter.init()
        presenter.onSearchListener.onNext(name)

        verify(view).setOrgUnits(
            listOf(treeNode(parentOrgUnit).apply { isOpen = true }, treeNode(orgUnit))
        )
    }

    @Test
    fun `Should dispose of all disposables`() {
        presenter.onDestroy()

        val result = presenter.compositeDisposable.size()

        assert(result == 0)
    }

    private fun dummyOrgUnit(level: Int) =
        OrganisationUnit.builder()
            .uid(UUID.randomUUID().toString())
            .level(level)
            .build()

    private fun treeNode(orgUnit: OrganisationUnit) =
        TreeNode(
            orgUnit,
            isOpen = false,
            hasChild = false,
            isChecked = false,
            level = orgUnit.level()!!
        )
}
