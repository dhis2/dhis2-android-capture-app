package org.dhis2.form.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.databinding.ObservableField
import androidx.lifecycle.Observer
import io.reactivex.Flowable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.data.DataEntryRepository
import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.FormRepositoryImpl
import org.dhis2.form.data.FormValueStore
import org.dhis2.form.data.GeometryController
import org.dhis2.form.data.RulesUtilsProvider
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.model.KeyboardActionType
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.model.SectionUiModelImpl
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.provider.DisplayNameProvider
import org.dhis2.form.ui.provider.LegendValueProvider
import org.dhis2.form.ui.validation.FieldErrorMessageProvider
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.option.Option
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class DataEntryIntegrationTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testingDispatcher = UnconfinedTestDispatcher()
    private val dispatcher: DispatcherProvider = mock {
        on { io() } doReturn testingDispatcher
        on { ui() } doReturn testingDispatcher
    }
    private val geometryController: GeometryController = mock()
    private val preferenceProvider: PreferenceProvider = mock()

    private val formValueStore: FormValueStore = mock {
        on {
            save(
                any<String>(),
                anyOrNull(),
                anyOrNull(),
            )
        } doAnswer { invocationOnMock ->
            StoreResult(
                uid = invocationOnMock.getArgument(0) as String,
                valueStoreResult = ValueStoreResult.VALUE_CHANGED,
            )
        }
    }
    private val fieldErrorMessageProvider: FieldErrorMessageProvider = mock()
    private val displayNameProvider: DisplayNameProvider = mock()
    private val legendValueProvider: LegendValueProvider = mock()
    private val dataEntryRepository: DataEntryRepository = mock {
        on { list() } doReturn Flowable.just(provideMalariaCaseRegistrationEventItems())
    }

    private val ruleEngineRepository: RuleEngineHelper = mock()
    private val rulesUtilsProvider: RulesUtilsProvider = mock()

    private val repository: FormRepository = FormRepositoryImpl(
        formValueStore = formValueStore,
        fieldErrorMessageProvider = fieldErrorMessageProvider,
        displayNameProvider = displayNameProvider,
        dataEntryRepository = dataEntryRepository,
        ruleEngineRepository = ruleEngineRepository,
        rulesUtilsProvider = rulesUtilsProvider,
        legendValueProvider = legendValueProvider,
        useCompose = true,
    )

    private lateinit var formViewModel: FormViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        whenever(
            dataEntryRepository.updateSection(
                any<FieldUiModel>(),
                any<Boolean>(),
                any<Int>(),
                any<Int>(),
                any<Int>(),
                any<Int>(),
            ),
        ).thenAnswer { invocationOnMock ->
            val sectionUiModel = invocationOnMock.getArgument(0) as SectionUiModelImpl
            sectionUiModel.copy(
                isOpen = invocationOnMock.getArgument(1) as Boolean,
                totalFields = invocationOnMock.getArgument(2) as Int,
                completedFields = invocationOnMock.getArgument(3) as Int,
                errors = invocationOnMock.getArgument(4) as Int,
                warnings = invocationOnMock.getArgument(5) as Int,
            )
        }
        whenever(
            dataEntryRepository.updateField(
                any<FieldUiModel>(),
                anyOrNull<String>(),
                any<List<String>>(),
                any<List<String>>(),
                any<List<String>>(),
            ),
        ).thenAnswer { invocationOnMock ->
            val fieldUiModel = invocationOnMock.getArgument(0) as FieldUiModelImpl
            fieldUiModel
        }

        formViewModel = FormViewModel(
            repository = repository,
            dispatcher = dispatcher,
            geometryController = geometryController,
            openErrorLocation = false,
            preferenceProvider = preferenceProvider,
        )
    }

    @Test
    fun shouldDataEntrySuccessfully() = runTest {
        val observedItems = mutableListOf<List<FieldUiModel>>()
        val observer = Observer<List<FieldUiModel>> { items ->
            observedItems.add(items)
        }

        formViewModel.items.observeForever(observer)

        val focusOnReportDateIntent = FormIntent.OnFocus(
            uid = "EVENT_REPORT_DATE_UID",
            value = "",
        )
        formViewModel.submitIntent(focusOnReportDateIntent)

        assert(
            observedItems.last().find { it.uid == "EVENT_REPORT_DATE_UID" }?.value.isNullOrEmpty(),
        )

        val enterReportDateIntent = FormIntent.OnTextChange(
            uid = "EVENT_REPORT_DATE_UID",
            value = "2024-03-20",
            valueType = ValueType.DATE,
        )
        formViewModel.submitIntent(enterReportDateIntent)

        val focusOnOrgUnitIntent = FormIntent.OnFocus(
            uid = "EVENT_ORG_UNIT_UID",
            value = "",
        )
        formViewModel.submitIntent(focusOnOrgUnitIntent)

        val enterOrgUnitIntent = FormIntent.OnTextChange(
            uid = "EVENT_ORG_UNIT_UID",
            value = "g8upMTyEZGZ",
            valueType = ValueType.ORGANISATION_UNIT,
        )
        formViewModel.submitIntent(enterOrgUnitIntent)

        val focusOnCoordinatesIntent = FormIntent.OnFocus(
            uid = "EVENT_COORDINATE_UID",
            value = "",
        )
        formViewModel.submitIntent(focusOnCoordinatesIntent)

        // Change section
        formViewModel.submitIntent(FormIntent.OnSection(sectionUid = "EVENT_DATA_SECTION_UID"))

        // Focus on age
        val focusOnAgeIntent = FormIntent.OnFocus(
            uid = "qrur9Dvnyt5",
            value = "",
        )
        formViewModel.submitIntent(focusOnAgeIntent)

        // Enter age
        val enterAgeIntent = FormIntent.OnTextChange(
            uid = "qrur9Dvnyt5",
            value = "20",
            valueType = ValueType.AGE,
        )
        formViewModel.submitIntent(enterAgeIntent)

        // Focus on gender
        val focusOnGenderIntent = FormIntent.OnFocus(
            uid = "oZg33kd9taw",
            value = "",
        )
        formViewModel.submitIntent(focusOnGenderIntent)

        val enterGenderIntent = FormIntent.OnSave(
            uid = "oZg33kd9taw",
            value = "Female",
            valueType = ValueType.MULTI_TEXT,
        )
        formViewModel.submitIntent(enterGenderIntent)

        assert(
            observedItems.last().find { it.uid == "EVENT_REPORT_DATE_UID" }?.value == "2024-03-20",
        )
        assert(
            observedItems.last().find { it.uid == "EVENT_ORG_UNIT_UID" }?.value == "g8upMTyEZGZ",
        )
        assert(
            observedItems.last().find { it.uid == "qrur9Dvnyt5" }?.value == "20",
        )
        assert(
            observedItems.last().find { it.uid == "oZg33kd9taw" }?.value == "Female",
        )

        // Clean up
        formViewModel.items.removeObserver(observer)
    }

    private fun provideMalariaCaseRegistrationEventItems(): List<FieldUiModel> {
        return listOf(
            SectionUiModelImpl(
                uid = "EVENT_DETAILS_SECTION_UID",
                layoutId = 0,
                label = "Event details",
                programStageSection = "EVENT_DETAILS_SECTION_UID",
                selectedField = ObservableField(""),
            ),
            FieldUiModelImpl(
                uid = "EVENT_REPORT_DATE_UID",
                layoutId = 0,
                label = "Report date",
                programStageSection = "EVENT_DETAILS_SECTION_UID",
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
                valueType = ValueType.DATE,
                mandatory = true,
                keyboardActionType = KeyboardActionType.NEXT,
            ),
            FieldUiModelImpl(
                uid = "EVENT_ORG_UNIT_UID",
                layoutId = 0,
                label = "Org unit",
                programStageSection = "EVENT_DETAILS_SECTION_UID",
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
                valueType = ValueType.ORGANISATION_UNIT,
                mandatory = true,
                keyboardActionType = KeyboardActionType.NEXT,
            ),
            FieldUiModelImpl(
                uid = "EVENT_COORDINATE_UID",
                layoutId = 0,
                label = "Coordinates",
                programStageSection = "EVENT_DETAILS_SECTION_UID",
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
                valueType = ValueType.COORDINATE,
                keyboardActionType = KeyboardActionType.NEXT,
            ),
            SectionUiModelImpl(
                uid = "EVENT_DATA_SECTION_UID",
                layoutId = 0,
                label = "Event data",
                programStageSection = "EVENT_DATA_SECTION_UID",
                selectedField = ObservableField(""),
            ),
            FieldUiModelImpl(
                uid = "qrur9Dvnyt5",
                layoutId = 0,
                label = "Age (years)",
                programStageSection = "EVENT_DATA_SECTION_UID",
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
                valueType = ValueType.AGE,
                mandatory = true,
                keyboardActionType = KeyboardActionType.NEXT,
            ),
            FieldUiModelImpl(
                uid = "oZg33kd9taw",
                layoutId = 0,
                label = "Gender",
                programStageSection = "EVENT_DATA_SECTION_UID",
                autocompleteList = emptyList(),
                optionSetConfiguration = OptionSetConfiguration.DefaultOptionSet(
                    options = listOf(
                        Option.builder()
                            .uid("rBvjJYbMCVx")
                            .code("Male")
                            .displayName("Male")
                            .name("Male")
                            .sortOrder(1)
                            .build(),
                        Option.builder()
                            .uid("Mnp3oXrpAbK")
                            .code("Female")
                            .displayName("Female")
                            .name("Female")
                            .sortOrder(2)
                            .build(),
                    ),
                    optionMetadataIcon = emptyMap(),
                ),
                valueType = ValueType.MULTI_TEXT,
                mandatory = true,
                keyboardActionType = KeyboardActionType.NEXT,
            ),
            FieldUiModelImpl(
                uid = "F3ogKBuviRA",
                layoutId = 0,
                label = "Household location",
                programStageSection = "EVENT_DATA_SECTION_UID",
                autocompleteList = emptyList(),
                optionSetConfiguration = null,
                valueType = ValueType.COORDINATE,
                keyboardActionType = KeyboardActionType.NEXT,
            ),
        )
    }
}
