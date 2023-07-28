package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import java.util.UUID
import org.dhis2.data.dhislogic.AUTH_DATAVALUE_ADD
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.dataapproval.DataApproval
import org.hisp.dhis.android.core.dataapproval.DataApprovalState
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.dataelement.DataElementOperand
import org.hisp.dhis.android.core.dataset.DataInputPeriod
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.DataSetElement
import org.hisp.dhis.android.core.dataset.Section
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.Period
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class DataValueRepositoryTest {

    private lateinit var repository: DataValueRepository
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val dataSetUid = "dataSetUid"
    private val sectionUid = "sectionUid"
    private val orgUnitUid = "orgUnitUid"
    private val periodId = "periodId"
    private val attrOptionCombo = "attrOptionCombo"

    @Before
    fun setUp() {
        repository = DataValueRepository(
            d2,
            dataSetUid,
            sectionUid,
            orgUnitUid,
            periodId,
            attrOptionCombo
        )
    }

    @Test
    fun `Should return period`() {
        whenever(
            d2.periodModule().periods().byPeriodId().eq(periodId)
        ) doReturn mock()
        whenever(
            d2.periodModule().periods().byPeriodId().eq(periodId).one()
        ) doReturn mock()
        whenever(
            d2.periodModule().periods().byPeriodId().eq(periodId).one().get()
        ) doReturn Single.just(dummyPeriod(periodId))

        val testObserver = repository.getPeriod().test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue { period ->
            period.periodId() == periodId
        }

        testObserver.dispose()
    }

    @Test
    fun `Should return dataInputPeriod`() {
        val dataInputPeriods = listOf(
            dummyDataInputPeriod(periodId),
            dummyDataInputPeriod()
        )

        whenever(
            d2.dataSetModule().dataSets()
                .withDataInputPeriods()
                .uid(dataSetUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSets()
                .withDataInputPeriods()
                .uid(dataSetUid)
                .blockingGet()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSets()
                .withDataInputPeriods()
                .uid(dataSetUid)
                .blockingGet()
                .dataInputPeriods()
        ) doReturn dataInputPeriods

        val result = repository.getDataInputPeriod()

        assertTrue(result == dataInputPeriods.first())
    }

    @Test
    fun `Should return catOptions when there are no section and dataSetElement have catOptions`() {
        val dataSetElements = listOf(dummyDataSetElement(), dummyDataSetElement())
        val dataElements = dataSetElements.map {
            DataElement.builder().uid(it.dataElement().uid()).build()
        }
        whenever(
            d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid).blockingGet()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid).blockingGet()
                .dataSetElements()
        ) doReturn dataSetElements

        whenever(
            d2.dataSetModule().sections().withDataElements()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections().withDataElements()
                .byDataSetUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections().withDataElements()
                .byDataSetUid().eq(dataSetUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections().withDataElements()
                .byDataSetUid().eq(dataSetUid)
                .uid(sectionUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections().withDataElements()
                .byDataSetUid().eq(dataSetUid)
                .uid(sectionUid)
                .blockingGet()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections().withDataElements()
                .byDataSetUid().eq(dataSetUid)
                .uid(sectionUid)
                .blockingGet()
                .dataElements()
        ) doReturn dataElements

        val categoryCombosUids = dataSetElements.map { it.categoryCombo()?.uid() }
        val categoryCombos = dataSetElements.map {
            CategoryCombo.builder().uid(it.categoryCombo()?.uid()).build()
        }

        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids)
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .get()
        ) doReturn Single.just(categoryCombos)

        val testObserver = repository.getCatCombo().test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(categoryCombos)

        testObserver.dispose()
    }

    @Test
    fun `Should return catOptions when no section and dataSetElement without catOptions`() {
        val dataSetElements = listOf(dummyDataSetElementWithNoCatCombo())
        val categoryCombos = listOf(dummyCategoryCombo())
        val categoryCombosUids = categoryCombos.map { it.uid() }
        val dataElements = dataSetElements.map {
            DataElement.builder().uid(it.dataElement().uid()).build()
        }
        whenever(
            d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid).blockingGet()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid).blockingGet()
                .dataSetElements()
        ) doReturn dataSetElements

        whenever(
            d2.dataElementModule()
                .dataElements()
                .uid(dataSetElements.first().dataElement().uid())
        ) doReturn mock()
        whenever(
            d2.dataElementModule()
                .dataElements()
                .uid(dataSetElements.first().dataElement().uid())
                .blockingGet()
        ) doReturn mock()
        whenever(
            d2.dataElementModule()
                .dataElements()
                .uid(dataSetElements.first().dataElement().uid())
                .blockingGet()
                .categoryComboUid()
        ) doReturn categoryCombosUids.first()

        whenever(
            d2.dataSetModule().sections().withDataElements()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections().withDataElements()
                .byDataSetUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections().withDataElements()
                .byDataSetUid().eq(dataSetUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections().withDataElements()
                .byDataSetUid().eq(dataSetUid)
                .uid(sectionUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections().withDataElements()
                .byDataSetUid().eq(dataSetUid)
                .uid(sectionUid)
                .blockingGet()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections().withDataElements()
                .byDataSetUid().eq(dataSetUid)
                .uid(sectionUid)
                .blockingGet()
                .dataElements()
        ) doReturn dataElements
        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids)
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .get()
        ) doReturn Single.just(categoryCombos)

        val testObserver = repository.getCatCombo().test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(categoryCombos)

        testObserver.dispose()
    }

    @Test
    fun `Should return catOptions for a section and dataSetElement have catOptions`() {
        val dataSetElements = listOf(dummyDataSetElement(), dummyDataSetElement())
        val dataElements = dataSetElements.map {
            DataElement.builder().uid(it.dataElement().uid()).build()
        }

        whenever(
            d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid).blockingGet()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid).blockingGet()
                .dataSetElements()
        ) doReturn dataSetElements

        whenever(
            d2.dataSetModule().sections().withDataElements()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections().withDataElements()
                .byDataSetUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections().withDataElements()
                .byDataSetUid().eq(dataSetUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections().withDataElements()
                .byDataSetUid().eq(dataSetUid)
                .uid(sectionUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections().withDataElements()
                .byDataSetUid().eq(dataSetUid)
                .uid(sectionUid)
                .blockingGet()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections().withDataElements()
                .byDataSetUid().eq(dataSetUid)
                .uid(sectionUid)
                .blockingGet()
                .dataElements()
        ) doReturn dataElements

        val categoryCombosUids = dataSetElements.map { it.categoryCombo()?.uid() }
        val categoryCombos = dataSetElements.map {
            CategoryCombo.builder().uid(it.categoryCombo()?.uid()).build()
        }

        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids)
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
                .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .get()
        ) doReturn Single.just(categoryCombos)

        val testObserver = repository.getCatCombo().test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(categoryCombos)

        testObserver.dispose()
    }

    @Test
    fun `Should return dataSet`() {
        val dataSet = dummyDataSet()
        whenever(
            d2.dataSetModule().dataSets().uid(dataSetUid).get()
        ) doReturn Single.just(dataSet)

        val testObserver = repository.getDataSet().test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(dataSet)

        testObserver.dispose()
    }

    @Test
    fun `Should return empty list for greyFields without section`() {
        val section = dummySection()
        whenever(
            d2.dataSetModule()
                .sections().withGreyedFields().byDataSetUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule()
                .sections().withGreyedFields().byDataSetUid().eq(dataSetUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule()
                .sections().withGreyedFields().byDataSetUid().eq(dataSetUid).uid(sectionUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule()
                .sections().withGreyedFields().byDataSetUid().eq(dataSetUid).uid(sectionUid).get()
        ) doReturn Single.just(section)
        val testObserver = repository.getGreyFields().test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(section.greyedFields())

        testObserver.dispose()
    }

    @Test
    fun `Should return greyFields for section`() {
        val sectionName = "NO_SECTION"

        val repositoryNoSection = DataValueRepository(
            d2,
            dataSetUid,
            sectionName,
            orgUnitUid,
            periodId,
            attrOptionCombo
        )

        val testObserver = repositoryNoSection.getGreyFields().test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(listOf())

        testObserver.dispose()
    }

    @Test
    fun `Should return dataSet is approved`() {
        val orgUnit = "orgUnit"
        val period = "period"
        val attributeOptionCombo = "attributeOptionCombo"
        val approvalStates = listOf(
            DataApprovalState.APPROVED_ELSEWHERE,
            DataApprovalState.APPROVED_ABOVE,
            DataApprovalState.APPROVED_HERE,
            DataApprovalState.ACCEPTED_ELSEWHERE,
            DataApprovalState.ACCEPTED_HERE
        )

        whenever(
            d2.dataSetModule().dataApprovals()
                .byOrganisationUnitUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataApprovals()
                .byOrganisationUnitUid().eq(orgUnit)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataApprovals()
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriodId()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataApprovals()
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriodId().eq(period)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataApprovals()
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriodId().eq(period)
                .byAttributeOptionComboUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataApprovals()
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriodId().eq(period)
                .byAttributeOptionComboUid().eq(attributeOptionCombo)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataApprovals()
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriodId().eq(period)
                .byAttributeOptionComboUid().eq(attributeOptionCombo)
                .one()
        ) doReturn mock()

        approvalStates.forEach { dataApprovalState ->
            val dataApproval = DataApproval.builder()
                .state(dataApprovalState)
                .organisationUnit(orgUnit)
                .period(period)
                .attributeOptionCombo(attributeOptionCombo)
                .workflow("workflow")
                .build()

            whenever(
                d2.dataSetModule().dataApprovals()
                    .byOrganisationUnitUid().eq(orgUnit)
                    .byPeriodId().eq(period)
                    .byAttributeOptionComboUid().eq(attributeOptionCombo)
                    .one().blockingGet()
            ) doReturn dataApproval

            val testObserver = repository.isApproval().test()

            testObserver.assertNoErrors()
            testObserver.assertValue(true)

            testObserver.dispose()
        }
    }

    @Test
    fun `Should return dataSet is not approved`() {
        val orgUnit = "orgUnit"
        val period = "period"
        val attributeOptionCombo = "attributeOptionCombo"

        whenever(
            d2.dataSetModule().dataApprovals()
                .byOrganisationUnitUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataApprovals()
                .byOrganisationUnitUid().eq(orgUnit)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataApprovals()
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriodId()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataApprovals()
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriodId().eq(period)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataApprovals()
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriodId().eq(period)
                .byAttributeOptionComboUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataApprovals()
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriodId().eq(period)
                .byAttributeOptionComboUid().eq(attributeOptionCombo)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataApprovals()
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriodId().eq(period)
                .byAttributeOptionComboUid().eq(attributeOptionCombo)
                .one()
        ) doReturn mock()

        val approvalStates = listOf(
            DataApprovalState.APPROVED_ELSEWHERE,
            DataApprovalState.APPROVED_ABOVE,
            DataApprovalState.APPROVED_HERE,
            DataApprovalState.ACCEPTED_ELSEWHERE,
            DataApprovalState.ACCEPTED_HERE
        )
        val unapprovedStates = DataApprovalState.values().filter { !approvalStates.contains(it) }

        unapprovedStates.forEach { dataApprovalState ->
            val dataApproval = DataApproval.builder()
                .state(dataApprovalState)
                .organisationUnit(orgUnit)
                .period(period)
                .attributeOptionCombo(attributeOptionCombo)
                .workflow("workflow")
                .build()

            whenever(
                d2.dataSetModule().dataApprovals()
                    .byOrganisationUnitUid().eq(orgUnit)
                    .byPeriodId().eq(period)
                    .byAttributeOptionComboUid().eq(attributeOptionCombo)
                    .one().blockingGet()
            ) doReturn dataApproval

            val testObserver = repository.isApproval().test()

            testObserver.assertNoErrors()
            testObserver.assertValue(false)

            testObserver.dispose()
        }
    }

    @Test
    fun `Should return dataSet and orgUnit have write permission`() {
        val dataSet = dummyDataSet()
        val categoryOption = CategoryOption.builder()
            .uid(UUID.randomUUID().toString())
            .access(Access.create(true, true, DataAccess.create(true, true)))
            .build()
        val categoryOptionCombo = CategoryOptionCombo.builder()
            .uid(UUID.randomUUID().toString())
            .categoryOptions(listOf(categoryOption))
            .build()

        whenever(
            d2.dataSetModule().dataSets().uid(dataSetUid).get()
        ) doReturn Single.just(dataSet)

        whenever(
            d2.categoryModule()
                .categoryOptionCombos().withCategoryOptions()
        ) doReturn mock()
        whenever(
            d2.categoryModule()
                .categoryOptionCombos().withCategoryOptions()
                .byCategoryComboUid()
        ) doReturn mock()
        whenever(
            d2.categoryModule()
                .categoryOptionCombos().withCategoryOptions()
                .byCategoryComboUid().eq(dataSet.categoryCombo()?.uid())
        ) doReturn mock()
        whenever(
            d2.categoryModule()
                .categoryOptionCombos().withCategoryOptions()
                .byCategoryComboUid().eq(dataSet.categoryCombo()?.uid())
                .get()
        ) doReturn Single.just(listOf(categoryOptionCombo))
        whenever(
            d2.userModule().authorities()
                .byName()
        ) doReturn mock()
        whenever(
            d2.userModule().authorities()
                .byName().eq(AUTH_DATAVALUE_ADD)
        ) doReturn mock()
        whenever(
            d2.userModule().authorities()
                .byName().eq(AUTH_DATAVALUE_ADD)
                .blockingIsEmpty()
        ) doReturn false

        val testObserver = repository.canWriteAny().test()

        testObserver.assertNoErrors()
        testObserver.assertValue(true)

        testObserver.dispose()
    }

    @Test
    fun `Should return catCombo don't have write permission`() {
        val dataSet = dummyDataSet()
        val categoryOption = CategoryOption.builder()
            .uid(UUID.randomUUID().toString())
            .access(Access.create(false, false, DataAccess.create(false, false)))
            .build()
        val categoryOptionCombo = CategoryOptionCombo.builder()
            .uid(UUID.randomUUID().toString())
            .categoryOptions(listOf(categoryOption))
            .build()

        whenever(
            d2.dataSetModule().dataSets().uid(dataSetUid).get()
        ) doReturn Single.just(dataSet)

        whenever(
            d2.categoryModule()
                .categoryOptionCombos().withCategoryOptions()
        ) doReturn mock()
        whenever(
            d2.categoryModule()
                .categoryOptionCombos().withCategoryOptions()
                .byCategoryComboUid()
        ) doReturn mock()
        whenever(
            d2.categoryModule()
                .categoryOptionCombos().withCategoryOptions()
                .byCategoryComboUid().eq(dataSet.categoryCombo()?.uid())
        ) doReturn mock()
        whenever(
            d2.categoryModule()
                .categoryOptionCombos().withCategoryOptions()
                .byCategoryComboUid().eq(dataSet.categoryCombo()?.uid())
                .get()
        ) doReturn Single.just(listOf(categoryOptionCombo))
        whenever(
            d2.userModule().authorities()
                .byName()
        ) doReturn mock()
        whenever(
            d2.userModule().authorities()
                .byName().eq(AUTH_DATAVALUE_ADD)
        ) doReturn mock()
        whenever(
            d2.userModule().authorities()
                .byName().eq(AUTH_DATAVALUE_ADD)
                .blockingIsEmpty()
        ) doReturn false

        val testObserver = repository.canWriteAny().test()

        testObserver.assertNoErrors()
        testObserver.assertValue(false)

        testObserver.dispose()
    }

    @Test
    fun `Should return orgUnit don't have write permission`() {
        val dataSet = dummyDataSet()
        val categoryOption = CategoryOption.builder()
            .uid(UUID.randomUUID().toString())
            .access(Access.create(false, false, DataAccess.create(false, false)))
            .build()
        val categoryOptionCombo = CategoryOptionCombo.builder()
            .uid(UUID.randomUUID().toString())
            .categoryOptions(listOf(categoryOption))
            .build()

        whenever(
            d2.dataSetModule().dataSets().uid(dataSetUid).get()
        ) doReturn Single.just(dataSet)

        whenever(
            d2.categoryModule()
                .categoryOptionCombos().withCategoryOptions()
        ) doReturn mock()
        whenever(
            d2.categoryModule()
                .categoryOptionCombos().withCategoryOptions()
                .byCategoryComboUid()
        ) doReturn mock()
        whenever(
            d2.categoryModule()
                .categoryOptionCombos().withCategoryOptions()
                .byCategoryComboUid().eq(dataSet.categoryCombo()?.uid())
        ) doReturn mock()
        whenever(
            d2.categoryModule()
                .categoryOptionCombos().withCategoryOptions()
                .byCategoryComboUid().eq(dataSet.categoryCombo()?.uid())
                .get()
        ) doReturn Single.just(listOf(categoryOptionCombo))

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byDataSetUids(listOf(dataSetUid))
                .byOrganisationUnitScope(
                    OrganisationUnit.Scope.SCOPE_DATA_CAPTURE
                ).blockingGet()
        ) doReturn listOf(OrganisationUnit.builder().uid(UUID.randomUUID().toString()).build())
        whenever(
            d2.userModule().authorities()
                .byName()
        ) doReturn mock()
        whenever(
            d2.userModule().authorities()
                .byName().eq(AUTH_DATAVALUE_ADD)
        ) doReturn mock()
        whenever(
            d2.userModule().authorities()
                .byName().eq(AUTH_DATAVALUE_ADD)
                .blockingIsEmpty()
        ) doReturn false

        val testObserver = repository.canWriteAny().test()

        testObserver.assertNoErrors()
        testObserver.assertValue(false)

        testObserver.dispose()
    }

    @Test
    fun `Should return user don't have data value authority`() {
        val dataSet = dummyDataSet()
        val categoryOption = CategoryOption.builder()
            .uid(UUID.randomUUID().toString())
            .access(Access.create(false, false, DataAccess.create(false, false)))
            .build()
        val categoryOptionCombo = CategoryOptionCombo.builder()
            .uid(UUID.randomUUID().toString())
            .categoryOptions(listOf(categoryOption))
            .build()

        whenever(
            d2.dataSetModule().dataSets().uid(dataSetUid).get()
        ) doReturn Single.just(dataSet)

        whenever(
            d2.categoryModule()
                .categoryOptionCombos().withCategoryOptions()
        ) doReturn mock()
        whenever(
            d2.categoryModule()
                .categoryOptionCombos().withCategoryOptions()
                .byCategoryComboUid()
        ) doReturn mock()
        whenever(
            d2.categoryModule()
                .categoryOptionCombos().withCategoryOptions()
                .byCategoryComboUid().eq(dataSet.categoryCombo()?.uid())
        ) doReturn mock()
        whenever(
            d2.categoryModule()
                .categoryOptionCombos().withCategoryOptions()
                .byCategoryComboUid().eq(dataSet.categoryCombo()?.uid())
                .get()
        ) doReturn Single.just(listOf(categoryOptionCombo))

        whenever(
            d2.organisationUnitModule().organisationUnits()
                .byDataSetUids(listOf(dataSetUid))
                .byOrganisationUnitScope(
                    OrganisationUnit.Scope.SCOPE_DATA_CAPTURE
                ).blockingGet()
        ) doReturn listOf(OrganisationUnit.builder().uid(UUID.randomUUID().toString()).build())
        whenever(
            d2.userModule().authorities()
                .byName()
        ) doReturn mock()
        whenever(
            d2.userModule().authorities()
                .byName().eq(AUTH_DATAVALUE_ADD)
        ) doReturn mock()
        whenever(
            d2.userModule().authorities()
                .byName().eq(AUTH_DATAVALUE_ADD)
                .blockingIsEmpty()
        ) doReturn true

        val testObserver = repository.canWriteAny().test()

        testObserver.assertNoErrors()
        testObserver.assertValue(false)

        testObserver.dispose()
    }

    @Test
    fun `Should return dataSet don't have write permission`() {
        whenever(
            d2.dataSetModule().dataSets().uid(dataSetUid).get()
        ) doReturn Single.just(
            DataSet.builder()
                .uid(UUID.randomUUID().toString())
                .access(Access.create(false, false, DataAccess.create(false, false)))
                .build()
        )

        val testObserver = repository.canWriteAny().test()

        testObserver.assertNoErrors()
        testObserver.assertValue(false)

        testObserver.dispose()
    }

    private fun dummyPeriod(periodId: String = UUID.randomUUID().toString()): Period =
        Period.builder()
            .periodId(periodId)
            .build()

    private fun dummyDataInputPeriod(
        periodId: String = UUID.randomUUID().toString()
    ): DataInputPeriod = DataInputPeriod.builder()
        .period(ObjectWithUid.create(periodId))
        .build()

    private fun dummyDataSetElement(): DataSetElement = DataSetElement.builder()
        .categoryCombo(ObjectWithUid.create(UUID.randomUUID().toString()))
        .dataSet(ObjectWithUid.create(UUID.randomUUID().toString()))
        .dataElement(ObjectWithUid.create(UUID.randomUUID().toString()))
        .build()

    private fun dummyDataSetElementWithNoCatCombo(): DataSetElement = DataSetElement.builder()
        .dataSet(ObjectWithUid.create(UUID.randomUUID().toString()))
        .dataElement(ObjectWithUid.create(UUID.randomUUID().toString()))
        .build()

    private fun dummyDataSet(): DataSet = DataSet.builder()
        .uid(UUID.randomUUID().toString())
        .access(Access.create(true, true, DataAccess.create(true, true)))
        .categoryCombo(ObjectWithUid.create(UUID.randomUUID().toString()))
        .build()

    private fun dummyCategoryCombo(): CategoryCombo = CategoryCombo.builder()
        .uid(UUID.randomUUID().toString())
        .build()

    private fun dummySection(): Section = Section.builder()
        .uid(UUID.randomUUID().toString())
        .greyedFields(
            listOf(DataElementOperand.builder().uid(UUID.randomUUID().toString()).build())
        )
        .build()
}
