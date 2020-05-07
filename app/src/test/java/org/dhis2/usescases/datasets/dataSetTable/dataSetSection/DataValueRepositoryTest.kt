package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
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
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration
import org.hisp.dhis.android.core.dataset.DataSetElement
import org.hisp.dhis.android.core.dataset.Section
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.Period
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.UUID

class DataValueRepositoryTest {

    private lateinit var repository: DataValueRepositoryImpl
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val dataSetUid = "dataSetUid"

    @Before
    fun setUp() {
        repository = DataValueRepositoryImpl(d2, dataSetUid)
    }

    @Test
    fun `Should return period`() {
        val period = dummyPeriod()
        whenever(
            d2.periodModule().periods().byPeriodId().eq(period.periodId())
        ) doReturn mock()
        whenever(
            d2.periodModule().periods().byPeriodId().eq(period.periodId()).one()
        ) doReturn mock()
        whenever(
            d2.periodModule().periods().byPeriodId().eq(period.periodId()).one().get()
        ) doReturn Single.just(period)


        val testObserver = repository.getPeriod(period.periodId()!!).test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(period)

        testObserver.dispose()
    }

    @Test
    fun `Should return dataInputPeriod`() {
        val dataInputPeriods = listOf(dummyDataInputPeriod(), dummyDataInputPeriod())

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

        val testObserver = repository.dataInputPeriod.test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(dataInputPeriods)

        testObserver.dispose()
    }

    @Test
    fun `Should return catOptions when there are no section and dataSetElement have catOptions`() {
        val dataSetElements = listOf(dummyDataSetElement(), dummyDataSetElement())

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
                .withCategoryOptionCombos()
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
                .withCategoryOptionCombos().orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
                .withCategoryOptionCombos().orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .get()
        ) doReturn Single.just(categoryCombos)


        val testObserver = repository.getCatCombo("NO_SECTION").test()

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
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids)
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
                .withCategoryOptionCombos()
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
                .withCategoryOptionCombos().orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
                .withCategoryOptionCombos().orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .get()
        ) doReturn Single.just(categoryCombos)


        val testObserver = repository.getCatCombo("NO_SECTION").test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(categoryCombos)

        testObserver.dispose()
    }
    
    @Test
    fun `Should return catOptions for a section and dataSetElement have catOptions`() {
        val dataSetElements = listOf(dummyDataSetElement(), dummyDataSetElement())
        val sectionName = "section"
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
            d2.dataSetModule()
                .sections()
                .withDataElements()
                .byDataSetUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule()
                .sections()
                .withDataElements()
                .byDataSetUid().eq(dataSetUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule()
                .sections()
                .withDataElements()
                .byDataSetUid().eq(dataSetUid)
                .byDisplayName()
        ) doReturn mock()
        whenever(
            d2.dataSetModule()
                .sections()
                .withDataElements()
                .byDataSetUid().eq(dataSetUid)
                .byDisplayName().eq(sectionName)
        ) doReturn mock()
        whenever(
            d2.dataSetModule()
                .sections()
                .withDataElements()
                .byDataSetUid().eq(dataSetUid)
                .byDisplayName().eq(sectionName)
                .one()
        ) doReturn mock()
        whenever(
            d2.dataSetModule()
                .sections()
                .withDataElements()
                .byDataSetUid().eq(dataSetUid)
                .byDisplayName().eq(sectionName)
                .one().blockingGet()
        ) doReturn mock()
        whenever(
            d2.dataSetModule()
                .sections()
                .withDataElements()
                .byDataSetUid().eq(dataSetUid)
                .byDisplayName().eq(sectionName)
                .one().blockingGet()
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
                .withCategoryOptionCombos()
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
                .withCategoryOptionCombos().orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryCombos().byUid().`in`(categoryCombosUids).withCategories()
                .withCategoryOptionCombos().orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                .get()
        ) doReturn Single.just(categoryCombos)


        val testObserver = repository.getCatCombo(sectionName).test()

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


        val testObserver = repository.dataSet.test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(dataSet)

        testObserver.dispose()
    }

    @Test
    fun `Should return empty list for greyFields without section`() {
        val sectionName = "section"
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
                .sections().withGreyedFields().byDataSetUid().eq(dataSetUid).byDisplayName()
        ) doReturn mock()
        whenever(
            d2.dataSetModule()
                .sections().withGreyedFields().byDataSetUid().eq(dataSetUid).byDisplayName()
                .eq(sectionName)
        ) doReturn mock()
        whenever(
            d2.dataSetModule()
                .sections().withGreyedFields().byDataSetUid().eq(dataSetUid).byDisplayName()
                .eq(sectionName).one()
        ) doReturn mock()
        whenever(
            d2.dataSetModule()
                .sections().withGreyedFields().byDataSetUid().eq(dataSetUid).byDisplayName()
                .eq(sectionName).one().get()
        ) doReturn Single.just(section)
        val testObserver = repository.getGreyFields(sectionName).test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(section.greyedFields())

        testObserver.dispose()
    }

    @Test
    fun `Should return greyFields for section`() {
        val sectionName = "NO_SECTION"

        val testObserver = repository.getGreyFields(sectionName).test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(listOf())

        testObserver.dispose()
    }

    @Test
    fun `Should return section by sectionName`() {
        val sectionName = "section"
        val section = dummySection()
        whenever(
            d2.dataSetModule().sections().byDataSetUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections().byDataSetUid().eq(dataSetUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections().byDataSetUid().eq(dataSetUid).byDisplayName()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections()
                .byDataSetUid().eq(dataSetUid)
                .byDisplayName().eq(sectionName)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections()
                .byDataSetUid().eq(dataSetUid)
                .byDisplayName().eq(sectionName).one()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().sections()
                .byDataSetUid().eq(dataSetUid)
                .byDisplayName()
                .eq(sectionName)
                .one().get()
        ) doReturn Single.just(section)
        val testObserver = repository.getSectionByDataSet(sectionName).test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(section)

        testObserver.dispose()
    }

    @Test
    fun `Should return empty section for no section`() {
        val sectionName = "NO_SECTION"

        val testObserver = repository.getSectionByDataSet(sectionName).test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(Section.builder().uid("").build())

        testObserver.dispose()
    }

    @Test
    fun `Should return dataSet is complete`() {
        val orgUnit = "orgUnit"
        val period = "period"
        val attributeOptionCombo = "attributeOptionCombo"
        val dataSetCompleteRegistration = DataSetCompleteRegistration.builder()
            .dataSet(dataSetUid)
            .organisationUnit(orgUnit)
            .period(period)
            .attributeOptionCombo(attributeOptionCombo)
            .deleted(true)
            .build()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byOrganisationUnitUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byOrganisationUnitUid().eq(orgUnit)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriod()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriod().eq(period)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriod().eq(period)
                .byAttributeOptionComboUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriod().eq(period)
                .byAttributeOptionComboUid().eq(attributeOptionCombo)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriod().eq(period)
                .byAttributeOptionComboUid().eq(attributeOptionCombo)
                .one()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriod().eq(period)
                .byAttributeOptionComboUid().eq(attributeOptionCombo)
                .one().blockingGet()
        ) doReturn dataSetCompleteRegistration


        val testObserver = repository.isCompleted(orgUnit, period, attributeOptionCombo).test()

        testObserver.assertNoErrors()
        testObserver.assertValue(false)

        testObserver.dispose()
    }

    @Test
    fun `Should return dataSet is approved`() {
        val orgUnit = "orgUnit"
        val period = "period"
        val attributeOptionCombo = "attributeOptionCombo"
        val dataApproval = DataApproval.builder()
            .state(DataApprovalState.APPROVED_HERE)
            .organisationUnit(orgUnit)
            .period(period)
            .attributeOptionCombo(attributeOptionCombo)
            .workflow("workflow")
            .build()
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
        whenever(
            d2.dataSetModule().dataApprovals()
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriodId().eq(period)
                .byAttributeOptionComboUid().eq(attributeOptionCombo)
                .one().blockingGet()
        ) doReturn dataApproval


        val testObserver = repository.isApproval(orgUnit, period, attributeOptionCombo).test()

        testObserver.assertNoErrors()
        testObserver.assertValue(true)

        testObserver.dispose()
    }

    @Test
    fun `Should return dataSet is not approved`() {
        val orgUnit = "orgUnit"
        val period = "period"
        val attributeOptionCombo = "attributeOptionCombo"
        val dataApproval = DataApproval.builder()
            .state(DataApprovalState.UNAPPROVED_ABOVE)
            .organisationUnit(orgUnit)
            .period(period)
            .attributeOptionCombo(attributeOptionCombo)
            .workflow("workflow")
            .build()
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
        whenever(
            d2.dataSetModule().dataApprovals()
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriodId().eq(period)
                .byAttributeOptionComboUid().eq(attributeOptionCombo)
                .one().blockingGet()
        ) doReturn dataApproval


        val testObserver = repository.isApproval(orgUnit, period, attributeOptionCombo).test()

        testObserver.assertNoErrors()
        testObserver.assertValue(false)

        testObserver.dispose()
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

    private fun dummyPeriod(): Period =
        Period.builder()
            .periodId(UUID.randomUUID().toString())
            .build()

    private fun dummyDataInputPeriod(): DataInputPeriod =
        DataInputPeriod.builder()
            .period(ObjectWithUid.create(UUID.randomUUID().toString()))
            .build()

    private fun dummyDataSetElement(): DataSetElement =
        DataSetElement.builder()
            .categoryCombo(ObjectWithUid.create(UUID.randomUUID().toString()))
            .dataSet(ObjectWithUid.create(UUID.randomUUID().toString()))
            .dataElement(ObjectWithUid.create(UUID.randomUUID().toString()))
            .build()

    private fun dummyDataSetElementWithNoCatCombo(): DataSetElement =
        DataSetElement.builder()
            .dataSet(ObjectWithUid.create(UUID.randomUUID().toString()))
            .dataElement(ObjectWithUid.create(UUID.randomUUID().toString()))
            .build()

    private fun dummyDataSet(): DataSet =
        DataSet.builder()
            .uid(UUID.randomUUID().toString())
            .access(Access.create(true, true, DataAccess.create(true, true)))
            .categoryCombo(ObjectWithUid.create(UUID.randomUUID().toString()))
            .build()

    private fun dummyCategoryCombo(): CategoryCombo =
        CategoryCombo.builder()
            .uid(UUID.randomUUID().toString())
            .build()

    private fun dummySection(): Section =
        Section.builder()
            .uid(UUID.randomUUID().toString())
            .greyedFields(
                listOf(DataElementOperand.builder().uid(UUID.randomUUID().toString()).build())
            )
            .build()

}