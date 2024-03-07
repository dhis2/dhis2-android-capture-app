package org.dhis2.form.data

import io.reactivex.Flowable
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.form.ui.FieldViewModelFactoryImpl
import org.dhis2.form.ui.provider.AutoCompleteProvider
import org.dhis2.form.ui.provider.DisplayNameProvider
import org.dhis2.form.ui.provider.HintProvider
import org.dhis2.form.ui.provider.KeyboardActionProvider
import org.dhis2.form.ui.provider.LayoutProvider
import org.dhis2.form.ui.provider.LegendValueProvider
import org.dhis2.form.ui.provider.UiEventTypesProvider
import org.dhis2.form.ui.provider.UiStyleProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.rules.models.RuleAction
import org.hisp.dhis.rules.models.RuleEffect
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RulesUtilsProviderImplTest {

    private lateinit var ruleUtils: RulesUtilsProvider
    private lateinit var testFieldViewModels: MutableMap<String, FieldUiModel>
    private lateinit var fieldFactory: FieldViewModelFactory
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val valueStore: FormValueStore = mock()
    private val uiStyleProvider: UiStyleProvider = mock()
    private val layoutProvider: LayoutProvider = mock()
    private val hintProvider: HintProvider = mock()
    private val displayNameProvider: DisplayNameProvider = mock {
        on {
            provideDisplayName(
                valueType = ValueType.TEXT,
                value = "Old Value",
                optionSet = "optionSetUid",
            )
        } doReturn "OldDisplayName"
    }
    private val uiEventTypesProvider: UiEventTypesProvider = mock()
    private val keyboardActionProvider: KeyboardActionProvider = mock()
    private val legendValueProvider: LegendValueProvider = mock()
    private val optionsRepository: OptionsRepository = mock()
    private val autoCompleteProvider: AutoCompleteProvider = mock()

    private val testRuleEffects = ArrayList<RuleEffect>()

    @Before
    fun setUp() {
        ruleUtils = RulesUtilsProviderImpl(d2, optionsRepository)
        fieldFactory = FieldViewModelFactoryImpl(
            uiStyleProvider,
            layoutProvider,
            hintProvider,
            displayNameProvider,
            uiEventTypesProvider,
            keyboardActionProvider,
            legendValueProvider,
            autoCompleteProvider,
        )
        testFieldViewModels = getTestingFieldViewModels().associateBy { it.uid }.toMutableMap()
    }

    private fun getTestingFieldViewModels(): MutableList<FieldUiModel> {
        return arrayListOf(
            randomFieldViewModel("uid1", ValueType.TEXT, "section1", null),
            randomFieldViewModel("uid2", ValueType.TEXT, "section1"),
            randomFieldViewModel("uid3", ValueType.TEXT, "section2"),
            randomFieldViewModel("uid4", ValueType.TEXT, "section2"),
            randomFieldViewModel("uid5", ValueType.TEXT, "section2"),
            randomFieldViewModel("uid6", ValueType.TEXT, "section3"),
            randomFieldViewModel("uid7", ValueType.TEXT, "section3"),
            randomFieldViewModel("uid8", ValueType.INTEGER_POSITIVE, "section4"),
            randomFieldViewModel("uid9", ValueType.NUMBER, "section4"),
            randomFieldViewModel("uid10", ValueType.BOOLEAN, "section4"),
            randomFieldViewModel(
                "uid11",
                ValueType.TEXT,
                "section4",
                value = null,
                optionSet = "optionSetUid",
            ),
            randomFieldViewModel(
                "uid12",
                ValueType.TEXT,
                "section4",
                value = "Old Value",
                optionSet = "optionSetUid",
            ),
        )
    }

    private fun randomFieldViewModel(
        uid: String,
        valueType: ValueType,
        section: String,
        value: String? = "test",
        optionSet: String? = null,
    ): FieldUiModel {
        return fieldFactory.create(
            uid,
            "label",
            valueType,
            false,
            optionSet,
            value,
            section,
            null,
            true,
            null,
            null,
            null,
            ObjectStyle.builder().build(),
            "",
            null,
            null,
        )
    }

    @Test
    fun `Should update fieldViewModel with a warning message`() {
        val testingUid = "uid1"

        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    "action_data",
                    ProgramRuleActionType.SHOWWARNING.name,
                    mutableMapOf(
                        Pair("content", "content"),
                        Pair("field", testingUid),
                    ),
                ),
                "data",
            ),
        )

        ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )

        assertNotNull(testFieldViewModels["uid1"]!!.warning)
        assertEquals(testFieldViewModels["uid1"]!!.warning, "content data")
    }

    @Test
    fun `Should update fieldViewModel with error message`() {
        val testingUid = "uid1"

        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    null,
                    ProgramRuleActionType.SHOWERROR.name,
                    mutableMapOf(
                        "content" to "content",
                        "field" to testingUid,
                    ),
                ),
                "data",
            ),
        )

        val result = ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )

        assertNotNull(testFieldViewModels[testingUid]!!.error)
        assertEquals(testFieldViewModels[testingUid]!!.error, "content data")
        assertTrue(result.errorMap().size == 1)
        assertTrue(result.errorMap().containsKey(testingUid))
    }

    @Test
    fun `Should remove field from list`() {
        val testingUid = "uid3"
        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    "data",
                    ProgramRuleActionType.HIDEFIELD.name,
                    mutableMapOf(
                        "content" to "content",
                        "field" to testingUid,
                    ),
                ),
            ),
        )

        whenever(valueStore.saveWithTypeCheck(testingUid, null)) doReturn Flowable.just(
            StoreResult(
                testingUid,
                ValueStoreResult.VALUE_CHANGED,
            ),
        )

        val result = ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )

        Assert.assertFalse(testFieldViewModels.contains(testingUid))
        verify(valueStore, times(1)).saveWithTypeCheck(testingUid, null)
        assertTrue(result.fieldsToUpdate.any { it.fieldUid == testingUid })
    }

    @Test
    fun `RuleActionDisplayText Should not add new FieldUIModel`() {
        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    "action_data",
                    ProgramRuleActionType.DISPLAYTEXT.name,
                    mutableMapOf(
                        "content" to "content",
                        "location" to "feedback",
                    ),
                ),
                "data",
            ),
        )

        val testFieldViewModelSize = testFieldViewModels.size

        ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )

        assertTrue(testFieldViewModels.size == testFieldViewModelSize)
        assertTrue(!testFieldViewModels.containsKey("content"))
    }

    @Test
    fun `RuleActionDisplayKeyValuePair should not add new FieldUIModel`() {
        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    "action_data",
                    ProgramRuleActionType.DISPLAYKEYVALUEPAIR.name,
                    mutableMapOf(
                        "content" to "content",
                        "location" to "indicators",
                    ),
                ),
                "data",
            ),
        )

        val testFieldViewModelSize = testFieldViewModels.size

        ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )

        assertTrue(testFieldViewModels.size == testFieldViewModelSize)
        assertTrue(!testFieldViewModels.containsKey("content"))
    }

    @Test
    fun `RuleActionHideSection should remove all fieldViewModel from a given section`() {
        val testingSectionUid = "section2"
        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    null,
                    ProgramRuleActionType.HIDESECTION.name,
                    mutableMapOf("programStageSection" to testingSectionUid),
                ),
                "data",
            ),
        )

        val mandatoryFieldUid = "uid3"
        testFieldViewModels.apply {
            put(mandatoryFieldUid, get(mandatoryFieldUid)!!.setFieldMandatory())
        }

        ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )

        assertTrue(testFieldViewModels[mandatoryFieldUid] != null)
        assertTrue(testFieldViewModels["uid4"] == null)
        assertTrue(testFieldViewModels["uid5"] == null)
    }

    @Test
    fun `RuleActionAssign should set a value to a given field without value`() {
        val testingUid = "uid1"

        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    "data",
                    ProgramRuleActionType.ASSIGN.name,
                    mutableMapOf(
                        "content" to "content",
                        "field" to testingUid,
                    ),
                ),
                "data",
            ),
        )

        ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )

        verify(valueStore, times(1)).saveWithTypeCheck(testingUid, "data")
        assertTrue(testFieldViewModels[testingUid]!!.value.equals("data"))
        assertTrue(!testFieldViewModels[testingUid]!!.editable)
    }

    @Test
    fun `RuleActionAssign should set a value to a given field with value`() {
        val testingUid = "uid2"
        val testingUid2 = "uid3"

        testRuleEffects.add(
            RuleEffect(
                "ruleUid1",
                RuleAction(
                    "data",
                    ProgramRuleActionType.ASSIGN.name,
                    mutableMapOf(
                        "content" to "content",
                        "field" to testingUid,
                    ),
                ),
                "data",
            ),
        )
        testRuleEffects.add(
            RuleEffect(
                "ruleUid2",
                RuleAction(
                    "data",
                    ProgramRuleActionType.ASSIGN.name,
                    mutableMapOf(
                        "content" to "content",
                        "field" to testingUid2,
                    ),
                ),
                "test",
            ),
        )

        whenever(valueStore.saveWithTypeCheck(any(), any())) doReturn Flowable.just(
            StoreResult(
                testingUid,
                ValueStoreResult.VALUE_CHANGED,
            ),
        )

        val result = ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )

        verify(valueStore, times(1)).saveWithTypeCheck(testingUid, "data")
        verify(valueStore, times(0)).saveWithTypeCheck(testingUid2, "test")
        assertTrue(testFieldViewModels[testingUid]!!.value.equals("data"))
        assertTrue(testFieldViewModels[testingUid2]!!.value.equals("test"))
        assertTrue(!testFieldViewModels[testingUid]!!.editable)
        assertTrue(!testFieldViewModels[testingUid]!!.editable)
        assertTrue(result.fieldsToUpdate.size == 1)
        assertTrue(result.fieldsToUpdate.any { it.fieldUid == testingUid })
    }

    @Test
    fun `RuleActionAssign should set a value to calculated value`() {
        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    "data",
                    ProgramRuleActionType.ASSIGN.name,
                    mutableMapOf(
                        "content" to "content",
                    ),
                ),
                "data",
            ),
        )

        val result = ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )

        assertTrue(result.fieldsToUpdate.isEmpty())
    }

    @Test
    fun `RuleActionAssign should assign a value to an empty field with option set`() {
        val newValue = "New Value"
        // Given a rule effect with an action of type ASSIGN
        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    newValue,
                    ProgramRuleActionType.ASSIGN.name,
                    mutableMapOf(
                        "content" to "content",
                        "field" to "uid11",
                    ),
                ),
                newValue,
            ),
        )
        // And a target field with an empty value
        assertNull(testFieldViewModels["uid11"]!!.value)

        // And a target field with option set
        whenever(
            optionsRepository.getOptionByCode("optionSetUid", newValue),
        ) doReturn Option.builder()
            .uid("OptionUid")
            .code(newValue)
            .name("optionName")
            .displayName("OptionDisplayName")
            .build()

        // When applying RuleEffects
        val result = ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )

        // Then the field value has been updated
        val updatedField = testFieldViewModels["uid11"]!!
        assertEquals(updatedField.value, newValue)
        assertEquals(updatedField.displayName, "OptionDisplayName")
        assertTrue(result.fieldsToUpdate.isEmpty())
    }

    @Test
    fun `RuleActionAssign should assign a value to field with value and with option set`() {
        val oldValue = "Old Value"
        val newValue = "New Value"
        val optionSetUid = "optionSetUid"
        // Given a rule effect with an action of type ASSIGN
        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    newValue,
                    ProgramRuleActionType.ASSIGN.name,
                    mutableMapOf(
                        "content" to "content",
                        "field" to "uid12",
                    ),
                ),
                newValue,
            ),
        )
        // And a target field with an existing value
        assertEquals(testFieldViewModels["uid12"]!!.value, oldValue)
        whenever(
            optionsRepository.getOptionByDisplayName(optionSetUid, "OldDisplayName"),
        ) doReturn Option.builder()
            .uid("OptionUid")
            .displayName("OldDisplayName")
            .name("optionName")
            .code(oldValue)
            .build()

        // And a target field with option set
        whenever(
            optionsRepository.getOptionByCode(optionSetUid, newValue),
        ) doReturn Option.builder()
            .uid("OptionUid")
            .code(newValue)
            .name("optionName")
            .displayName("OptionDisplayName")
            .build()

        // When applying RuleEffects
        val result = ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )

        // Then the field value has been updated
        val updatedField = testFieldViewModels["uid12"]!!
        assertEquals(updatedField.value, newValue)
        assertEquals(updatedField.displayName, "OptionDisplayName")
        assertTrue(result.fieldsToUpdate.isEmpty())
    }

    @Test
    fun `RuleActionSetMandatory should mark field as mandatory`() {
        val testingUid = "uid2"

        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    "data",
                    ProgramRuleActionType.SETMANDATORYFIELD.name,
                    mutableMapOf(
                        "field" to testingUid,
                    ),
                ),
                "data",
            ),
        )

        ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )

        assertTrue(testFieldViewModels[testingUid]!!.mandatory)
    }

    @Test
    fun `RuleActionWarningOnCompletion should set warning to field and allow completion`() {
        val testingUid = "uid1"

        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    "action_data",
                    ProgramRuleActionType.WARNINGONCOMPLETE.name,
                    mutableMapOf(
                        "content" to "content",
                        "field" to testingUid,
                    ),
                ),
                "data",
            ),
        )

        val result = ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )

        assertEquals(testFieldViewModels[testingUid]!!.warning, "content data")
        assertTrue(result.messageOnComplete == "content data")
        assertTrue(result.canComplete)
    }

    @Test
    fun `RuleActionErrorOnCompletion should set warning to field and not allow completion`() {
        val testingUid = "uid1"

        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    "action_data",
                    ProgramRuleActionType.ERRORONCOMPLETE.name,
                    mutableMapOf(
                        "content" to "content",
                        "field" to testingUid,
                    ),
                ),
                "data",
            ),
        )

        val result = ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )

        assertEquals(testFieldViewModels[testingUid]!!.error, "content data")
        assertTrue(result.messageOnComplete == "content data")
        assertTrue(!result.canComplete)
    }

    @Test
    fun `RuleActionHideProgramStage should execute callback action`() {
        val testingUid = "stageUid"
        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    null,
                    ProgramRuleActionType.HIDEPROGRAMSTAGE.name,
                    mutableMapOf(
                        "programStage" to testingUid,
                    ),
                ),
                "data",
            ),
        )

        ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )
    }

    @Test
    fun `RuleActionHideProgramStage should remove stage from possible selections`() {
    }

    @Test
    fun `RuleActionHideOption should execute callback action`() {
        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    null,
                    ProgramRuleActionType.HIDEOPTION.name,
                    mutableMapOf(
                        "content" to "content",
                        "field" to "field",
                        "option" to "optionUid",
                    ),
                ),
                "data",
            ),
        )

        testFieldViewModels["field"] = FieldUiModelImpl(
            "field",
            1,
            "label",
            false,
            null,
            true,
            null,
            true,
            "label",
            "section",
            null,
            null,
            "description",
            ValueType.TEXT,
            null,
            "optionSetUid",
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            null,
            null,
        )

        whenever(valueStore.deleteOptionValueIfSelected(any(), any())) doReturn StoreResult(
            "field",
            ValueStoreResult.VALUE_HAS_NOT_CHANGED,
        )

        val result = ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )

        assertTrue(
            result.optionsToHide.isNotEmpty(),
        )

        verify(valueStore).deleteOptionValueIfSelected("field", "optionUid")
    }

    @Test
    fun `RuleActionHideOptionGroup should execute callback action`() {
        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    null,
                    ProgramRuleActionType.HIDEOPTIONGROUP.name,
                    mutableMapOf(
                        "content" to "content",
                        "field" to "field",
                        "optionGroup" to "optionGroupUid",
                    ),
                ),
                "data",
            ),
        )

        testFieldViewModels["field"] = FieldUiModelImpl(
            "field",
            1,
            "label",
            false,
            null,
            true,
            null,
            true,
            "label",
            "section",
            null,
            null,
            "description",
            ValueType.TEXT,
            null,
            "optionSetUid",
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            null,
            null,
        )

        whenever(
            valueStore.deleteOptionValueIfSelectedInGroup(
                any(),
                any(),
                any(),
            ),
        ) doReturn StoreResult(
            "field",
            ValueStoreResult.VALUE_HAS_NOT_CHANGED,
        )

        val result = ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )

        assertTrue(
            result.optionGroupsToHide.isNotEmpty(),
        )

        verify(
            valueStore,
        ).deleteOptionValueIfSelectedInGroup("field", "optionGroupUid", true)
    }

    @Test
    fun `RuleActionShowOptionGroup should execute callback action`() {
        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    null,
                    ProgramRuleActionType.SHOWOPTIONGROUP.name,
                    mutableMapOf(
                        "content" to "content",
                        "field" to "field",
                        "optionGroup" to "optionGroupUid",
                    ),
                ),
                "data",
            ),
        )

        testFieldViewModels["field"] = FieldUiModelImpl(
            "field",
            1,
            "label",
            false,
            null,
            true,
            null,
            true,
            "label",
            "section",
            null,
            null,
            "description",
            ValueType.TEXT,
            null,
            "optionSetUid",
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            null,
            null,
        )

        val result = ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )

        assertTrue(
            result.optionGroupsToShow.isNotEmpty(),
        )
        verify(valueStore).deleteOptionValueIfSelectedInGroup("field", "optionGroupUid", false)
    }

    @Test
    fun `Should not assign value to a hidden field`() {
        val testingUid = "uid3"
        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    null,
                    ProgramRuleActionType.HIDEFIELD.name,
                    mutableMapOf(
                        "content" to "content",
                        "field" to testingUid,
                    ),
                ),
                "data",
            ),
        )
        testRuleEffects.add(
            RuleEffect(
                "ruleUid2",
                RuleAction(
                    "data",
                    ProgramRuleActionType.HIDEFIELD.name,
                    mutableMapOf(
                        "content" to "content",
                        "field" to testingUid,
                    ),
                ),
                "data",
            ),
        )

        whenever(valueStore.saveWithTypeCheck(testingUid, null)) doReturn Flowable.just(
            StoreResult(
                testingUid,
                ValueStoreResult.VALUE_CHANGED,
            ),
        )

        val result = ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )

        Assert.assertFalse(testFieldViewModels.contains(testingUid))
        verify(valueStore, times(1)).saveWithTypeCheck(testingUid, null)
        verify(valueStore, times(0)).saveWithTypeCheck(testingUid, "data")
        assertTrue(result.fieldsToUpdate.any { it.fieldUid == testingUid })
    }

    @Test
    fun `Should format value to assign`() {
        val integerUid = "uid8"
        val numberUid = "uid9"
        val booleanUid = "uid10"
        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    "5.0",
                    ProgramRuleActionType.ASSIGN.name,
                    mutableMapOf(
                        "content" to "content",
                        "field" to integerUid,
                    ),
                ),
                "5.0",
            ),
        )
        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    "2.52",
                    ProgramRuleActionType.ASSIGN.name,
                    mutableMapOf(
                        "content" to "content",
                        "field" to numberUid,
                    ),
                ),
                "2.52",
            ),
        )
        testRuleEffects.add(
            RuleEffect(
                "ruleUid",
                RuleAction(
                    "1",
                    ProgramRuleActionType.ASSIGN.name,
                    mutableMapOf(
                        "content" to "content",
                        "field" to booleanUid,
                    ),
                ),
                "1",
            ),
        )
        ruleUtils.applyRuleEffects(
            true,
            testFieldViewModels,
            testRuleEffects,
            valueStore,
        )
        assertTrue(testFieldViewModels[integerUid]?.value == "5")
        assertTrue(testFieldViewModels[numberUid]?.value == "2.52")
        assertTrue(testFieldViewModels[booleanUid]?.value == "true")
    }
}
