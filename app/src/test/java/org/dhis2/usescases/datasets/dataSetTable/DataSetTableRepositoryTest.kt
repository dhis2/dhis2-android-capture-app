package org.dhis2.usescases.datasets.dataSetTable

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration
import org.hisp.dhis.android.core.dataset.DataSetInstance
import org.hisp.dhis.android.core.dataset.Section
import org.hisp.dhis.android.core.period.PeriodType
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.mock


class DataSetTableRepositoryTest {

    private lateinit var repository: DataSetTableRepositoryImpl

    private val d2: D2 = mock(D2::class.java, RETURNS_DEEP_STUBS)
    private val dataSetUid = "dataSetUid"
    private val periodId = "periodId"
    private val orgUnitUid = "orgUnitUid"
    private val catOptCombo = "catOptCombo"

    @Before
    fun setUp() {
        repository = DataSetTableRepositoryImpl(d2, dataSetUid, periodId, orgUnitUid, catOptCombo)
    }

    @Test
    fun `Should return dataSet`() {
        whenever(d2.dataSetModule().dataSets().byUid()) doReturn mock()
        whenever(d2.dataSetModule().dataSets().byUid().eq(dataSetUid)) doReturn mock()
        whenever(d2.dataSetModule().dataSets().byUid().eq(dataSetUid).one()) doReturn mock()
        whenever(
            d2.dataSetModule().dataSets().byUid().eq(dataSetUid).one().blockingGet()
        ) doReturn dummyDataSet()

        val testObserver = repository.getDataSet().test()

        testObserver.assertNoErrors()
        testObserver.assertValueAt(0) {
            it.uid() == dataSetUid
        }
    }

    @Test
    fun `Should return the list of sections' uid of a dataSet`() {
        val sections = listOf(dummySection("section_1"), dummySection("section_2"))

        whenever(d2.dataSetModule().sections().byDataSetUid()) doReturn mock()
        whenever(d2.dataSetModule().sections().byDataSetUid().eq(dataSetUid)) doReturn mock()
        whenever(
            d2.dataSetModule().sections().byDataSetUid().eq(dataSetUid).get()
        ) doReturn Single.just(sections)

        val testObserver = repository.getSections().test()

        testObserver.assertNoErrors()
        testObserver.assertValue {
            it[0] == "section_1" &&
                    it[1] == "section_2"
        }
    }

    @Test
    fun `Should return a list with 'NO_SECTION' as only item because the dataSet has no section`() {
        val sections = listOf<Section>()

        whenever(d2.dataSetModule().sections().byDataSetUid()) doReturn mock()
        whenever(d2.dataSetModule().sections().byDataSetUid().eq(dataSetUid)) doReturn mock()
        whenever(
            d2.dataSetModule().sections().byDataSetUid().eq(dataSetUid).get()
        ) doReturn Single.just(sections)

        val testObserver = repository.getSections().test()

        testObserver.assertNoErrors()
        testObserver.assertValue {
            it[0] == "NO_SECTION"
        }
    }

    @Test
    fun `Should return true if the DataSet is completed and not deleted`() {
        mockDataSetCompRegistration()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid().eq(orgUnitUid)
                .byPeriod().eq(periodId)
                .one().blockingGet()
        ) doReturn dummyDataSetCompleteRegistration(false)

        val testObserver = repository.dataSetStatus().test()

        testObserver.assertNoErrors()
        testObserver.assertValue(true)
    }

    @Test
    fun `Should return false if the DataSet is completed and deleted`() {
        mockDataSetCompRegistration()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid().eq(orgUnitUid)
                .byPeriod().eq(periodId)
                .one().blockingGet()
        ) doReturn dummyDataSetCompleteRegistration(true)

        val testObserver = repository.dataSetStatus().test()

        testObserver.assertNoErrors()
        testObserver.assertValue(false)
    }

    @Test
    fun `Should return false if no DataSet was found`() {
        mockDataSetCompRegistration()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid().eq(orgUnitUid)
                .byPeriod().eq(periodId)
                .one().blockingGet()
        ) doReturn null

        val testObserver = repository.dataSetStatus().test()

        testObserver.assertNoErrors()
        testObserver.assertValue(false)
    }

    @Test
    fun `Should return the state of the DataSetInstance if it's state is different than SYNCED`() {
        val dataSetInstance = dummyDataSetInstance(State.TO_UPDATE)
        mockDataSetInstance()
        mockDataSetCompRegistration()

        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid().eq(orgUnitUid)
                .byPeriod().eq(periodId)
                .one().blockingGet()
        ) doReturn dataSetInstance
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid().eq(orgUnitUid)
                .byPeriod().eq(periodId)
                .one().blockingGet()
        ) doReturn dummyDataSetCompleteRegistration(false)

        val testObserver = repository.dataSetState().test()

        testObserver.assertNoErrors()
        testObserver.assertValue(State.TO_UPDATE)
    }

    @Test
    fun `Should return the state of the DataSetCompReg if DataSetInstance state is SYNCED`() {
        val dataSetInstance = dummyDataSetInstance(State.SYNCED)
        mockDataSetInstance()
        mockDataSetCompRegistration()

        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid().eq(orgUnitUid)
                .byPeriod().eq(periodId)
                .one().blockingGet()
        ) doReturn dataSetInstance
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid().eq(orgUnitUid)
                .byPeriod().eq(periodId)
                .one().blockingGet()
        ) doReturn dummyDataSetCompleteRegistration(false, State.TO_POST)

        val testObserver = repository.dataSetState().test()

        testObserver.assertNoErrors()
        testObserver.assertValue(State.TO_POST)
    }

    @Test
    fun `Should return the category Combo name`() {
        val uid = "catComboUid"
        val name = "CatComboName"
        whenever(d2.categoryModule().categoryOptionCombos().uid(uid)) doReturn mock()
        whenever(
            d2.categoryModule().categoryOptionCombos().uid(uid).blockingGet()
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryOptionCombos()
                .uid(uid).blockingGet().displayName()
        ) doReturn name

        val testObserver = repository.getCatComboName(uid).test()

        testObserver.assertNoErrors()
        testObserver.assertValue { it == name }
    }

    @Test
    fun `Should return the uid of the default categoryOption if input is an empty list`() {
        val categoryOptionCombo = dummyCategoryOptionCombos()

        whenever(d2.categoryModule().categoryOptionCombos().byDisplayName()) doReturn mock()
        whenever(
            d2.categoryModule().categoryOptionCombos().byDisplayName().like("default")
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryOptionCombos()
                .byDisplayName().like("default")
                .one()
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryOptionCombos()
                .byDisplayName().like("default")
                .one().blockingGet()
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryOptionCombos()
                .byDisplayName().like("default")
                .one().blockingGet().uid()
        ) doReturn categoryOptionCombo.uid()

        val returnedValue = repository.getCatOptComboFromOptionList(listOf())

        assert(returnedValue == "uid")
    }

    @Test
    fun `Should return the uid of one of the categoryOptionCombo by a list of catOption uids`() {
        val categoryOptionCombos = dummyCategoryOptionCombos("name", "uid_1")
        val catOptionUids = listOf("catOpt_uid_1", "catOpt_Uid_1")

        whenever(
            d2.categoryModule().categoryOptionCombos().byCategoryOptions(catOptionUids)
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryOptionCombos()
                .byCategoryOptions(catOptionUids).one()
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryOptionCombos()
                .byCategoryOptions(catOptionUids).one().blockingGet()
        ) doReturn mock()
        whenever(
            d2.categoryModule().categoryOptionCombos()
                .byCategoryOptions(catOptionUids).one().blockingGet().uid()
        ) doReturn categoryOptionCombos.uid()

        val returnedValue = repository.getCatOptComboFromOptionList(catOptionUids)

        assert(returnedValue == "uid_1")
    }

    @Test
    fun `Should return true if dataset was successfully marked as completed`() {
        whenever(d2.dataSetModule().dataSetCompleteRegistrations()) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .value(periodId, orgUnitUid, dataSetUid, catOptCombo)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .value(periodId, orgUnitUid, dataSetUid, catOptCombo).exists()
        ) doReturn Single.just(true)

        val testObserver = repository.completeDataSetInstance().test()

        testObserver.assertNoErrors()
        testObserver.assertComplete()
    }

    // TODO: ValidationRules - Need to add different paths of this method when SDK has functionality
    @Test
    fun `Should return true if the dataset has validation rules to execute`() {
        val hasValidationRules = repository.hasToRunValidationRules()

        assert(hasValidationRules)
    }

    // TODO: ValidationRules - Need to add different paths of this method when SDK has functionality
    @Test
    fun `Should return true if the validation rules are optional to execute`() {
        val isOptional = repository.isValidationRuleOptional()

        assert(isOptional)
    }

    // TODO: ValidationRules - Need to add different paths of this method when SDK has functionality
    @Test
    fun `Should return true if the validation rules execution does not have errors`() {
        val wasSuccessful = repository.executeValidationRules()

        assert(wasSuccessful)
    }

    private fun dummyDataSet() = DataSet.builder().uid(dataSetUid).build()

    private fun dummySection(uid: String) = Section.builder().uid(uid).displayName(uid).build()

    private fun dummyDataSetCompleteRegistration(deleted: Boolean, state: State = State.SYNCED) =
        DataSetCompleteRegistration.builder()
            .period(periodId)
            .dataSet(dataSetUid)
            .organisationUnit(orgUnitUid)
            .attributeOptionCombo(catOptCombo)
            .state(state)
            .deleted(deleted).build()

    private fun dummyDataSetInstance(state: State) =
        DataSetInstance.builder().period(periodId)
            .dataSetUid(dataSetUid)
            .dataSetDisplayName("dataSetName")
            .organisationUnitUid(orgUnitUid)
            .organisationUnitDisplayName("orgUnitName")
            .attributeOptionComboUid(catOptCombo)
            .attributeOptionComboDisplayName("catComboName")
            .valueCount(1)
            .completed(true)
            .periodType(PeriodType.Daily)
            .state(state).build()

    private fun dummyCategoryOptionCombos(displayName: String = "default", uid: String = "uid") =
        CategoryOptionCombo.builder()
            .uid(uid)
            .displayName(displayName)
            .categoryOptions(listOf(CategoryOption.builder().uid("catOptionUid").build()))
            .build()

    private fun mockDataSetCompRegistration() {
        whenever(d2.dataSetModule().dataSetCompleteRegistrations().byDataSetUid()) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations().byDataSetUid().eq(dataSetUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid().eq(orgUnitUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid().eq(orgUnitUid)
                .byPeriod()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid().eq(orgUnitUid)
                .byPeriod().eq(periodId)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid().eq(orgUnitUid)
                .byPeriod().eq(periodId)
                .one()
        ) doReturn mock()
    }

    private fun mockDataSetInstance() {
        whenever(d2.dataSetModule().dataSetInstances().byDataSetUid()) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetInstances().byDataSetUid().eq(dataSetUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid().eq(orgUnitUid)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid().eq(orgUnitUid)
                .byPeriod()
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid().eq(orgUnitUid)
                .byPeriod().eq(periodId)
        ) doReturn mock()
        whenever(
            d2.dataSetModule().dataSetInstances()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid().eq(orgUnitUid)
                .byPeriod().eq(periodId)
                .one()
        ) doReturn mock()
    }
}
