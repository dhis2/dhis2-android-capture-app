package org.dhis2.utils

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import java.util.ArrayList
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl
import org.dhis2.data.forms.dataentry.fields.display.DisplayViewModel
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.rules.models.RuleActionAssign
import org.hisp.dhis.rules.models.RuleActionDisplayKeyValuePair
import org.hisp.dhis.rules.models.RuleActionDisplayText
import org.hisp.dhis.rules.models.RuleActionErrorOnCompletion
import org.hisp.dhis.rules.models.RuleActionHideField
import org.hisp.dhis.rules.models.RuleActionHideOption
import org.hisp.dhis.rules.models.RuleActionHideOptionGroup
import org.hisp.dhis.rules.models.RuleActionHideProgramStage
import org.hisp.dhis.rules.models.RuleActionHideSection
import org.hisp.dhis.rules.models.RuleActionSetMandatoryField
import org.hisp.dhis.rules.models.RuleActionShowError
import org.hisp.dhis.rules.models.RuleActionShowOptionGroup
import org.hisp.dhis.rules.models.RuleActionShowWarning
import org.hisp.dhis.rules.models.RuleActionWarningOnCompletion
import org.hisp.dhis.rules.models.RuleEffect
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * QUADRAM. Created by ppajuelo on 07/11/2018.
 */
class RulesUtilsProviderImplTest {

    private lateinit var ruleUtils: RulesUtilsProvider
    private lateinit var testFieldViewModels: MutableMap<String, FieldViewModel>
    private lateinit var fieldFactory: FieldViewModelFactory

    private val actionCallbacks: RulesActionCallbacks = mock()

    private val testRuleEffects = ArrayList<RuleEffect>()

    @Before
    fun setUp() {
        ruleUtils = RulesUtilsProviderImpl()
        fieldFactory = FieldViewModelFactoryImpl(
            "", "",
            "", "", "", "",
            "", "", ""
        )
        testFieldViewModels = getTestingFieldViewModels().associateBy { it.uid() }.toMutableMap()
    }

    private fun getTestingFieldViewModels(): MutableList<FieldViewModel> {
        return arrayListOf(
            randomFieldViewModel("uid1", ValueType.TEXT, "section1", null),
            randomFieldViewModel("uid2", ValueType.TEXT, "section1"),
            randomFieldViewModel("uid3", ValueType.TEXT, "section2"),
            randomFieldViewModel("uid4", ValueType.TEXT, "section2"),
            randomFieldViewModel("uid5", ValueType.TEXT, "section2"),
            randomFieldViewModel("uid6", ValueType.TEXT, "section3"),
            randomFieldViewModel("uid7", ValueType.TEXT, "section3")
        )
    }

    private fun randomFieldViewModel(
        uid: String,
        valueType: ValueType,
        section: String,
        value: String? = "test"
    ): FieldViewModel {
        return fieldFactory.create(
            uid,
            "label",
            valueType,
            false,
            null,
            value,
            section,
            null,
            true,
            null,
            null,
            null,
            null,
            ObjectStyle.builder().build(),
            ""
        )
    }

    @Test
    fun `Should update fieldViewModel with a warning message`() {
        val testingUid = "uid1"

        testRuleEffects.add(
            RuleEffect.create(
                RuleActionShowWarning.create("content", "action_data", testingUid),
                "data"
            )
        )

        ruleUtils.applyRuleEffects(
            testFieldViewModels,
            Result.success(testRuleEffects),
            actionCallbacks
        )

        Assert.assertNotNull(testFieldViewModels["uid1"]!!.warning())
        Assert.assertEquals(testFieldViewModels["uid1"]!!.warning(), "content data")
    }

    @Test
    fun `Should update fieldViewModel with error message`() {
        val testingUid = "uid1"

        testRuleEffects.add(
            RuleEffect.create(
                RuleActionShowError.create("content", "action_data", testingUid),
                "data"
            )
        )

        val testModel = testFieldViewModels[testingUid]

        ruleUtils.applyRuleEffects(
            testFieldViewModels,
            Result.success(testRuleEffects),
            actionCallbacks
        )

        Assert.assertNotNull(testFieldViewModels[testingUid]!!.error())
        Assert.assertEquals(testFieldViewModels[testingUid]!!.error(), "content data")
        verify(actionCallbacks, times(1)).setShowError(
            testRuleEffects[0].ruleAction() as RuleActionShowError,
            testModel
        )
    }

    @Test
    fun `Should remove field from list`() {
        val testingUid = "uid3"
        testRuleEffects.add(
            RuleEffect.create(
                RuleActionHideField.create("content", testingUid),
                "data"
            )
        )

        ruleUtils.applyRuleEffects(
            testFieldViewModels,
            Result.success(testRuleEffects),
            actionCallbacks
        )

        Assert.assertFalse(testFieldViewModels.contains(testingUid))
        verify(actionCallbacks, times(1)).save(testingUid, null)
    }

    @Test
    fun `RuleActionDisplayText Should add new DisplayViewModel`() {
        testRuleEffects.add(
            RuleEffect.create(
                RuleActionDisplayText.createForFeedback("content", "action data"),
                "data"
            )
        )

        val testFieldViewModelSize = testFieldViewModels.size

        ruleUtils.applyRuleEffects(
            testFieldViewModels,
            Result.success(testRuleEffects),
            actionCallbacks
        )

        Assert.assertTrue(testFieldViewModels.size == testFieldViewModelSize + 1)
        Assert.assertTrue(testFieldViewModels.containsKey("content"))
        Assert.assertTrue(testFieldViewModels["content"] is DisplayViewModel)
        Assert.assertEquals(testFieldViewModels["content"]!!.value(), "content data")
    }

    @Test
    fun `RuleActionDisplayKeyValuePair should add new DisplayViewModel`() {
        testRuleEffects.add(
            RuleEffect.create(
                RuleActionDisplayKeyValuePair.createForIndicators("content", "action data"),
                "data"
            )
        )

        val testFieldViewModelSize = testFieldViewModels.size

        ruleUtils.applyRuleEffects(
            testFieldViewModels,
            Result.success(testRuleEffects),
            actionCallbacks
        )

        Assert.assertTrue(testFieldViewModels.size == testFieldViewModelSize + 1)
        Assert.assertTrue(testFieldViewModels.containsKey("content"))
        Assert.assertTrue(testFieldViewModels["content"] is DisplayViewModel)
        Assert.assertEquals(testFieldViewModels["content"]!!.label(), "content")
        Assert.assertEquals(testFieldViewModels["content"]!!.value(), "data")

        verify(actionCallbacks, times(1)).setDisplayKeyValue("content", "data")
    }

    @Test
    fun `RuleActionHideSection should remove all fieldViewModel from a given section`() {
        val testingSectionUid = "section2"
        testRuleEffects.add(
            RuleEffect.create(
                RuleActionHideSection.create(testingSectionUid),
                "data"
            )
        )

        ruleUtils.applyRuleEffects(
            testFieldViewModels,
            Result.success(testRuleEffects),
            actionCallbacks
        )

        verify(actionCallbacks, times(1)).setHideSection(testingSectionUid)
        verify(actionCallbacks, times(1)).save("uid3", null)
        verify(actionCallbacks, times(1)).save("uid4", null)
        verify(actionCallbacks, times(1)).save("uid5", null)
    }

    @Test
    fun `RuleActionAssign should set a value to a given field without value`() {
        val testingUid = "uid1"

        testRuleEffects.add(
            RuleEffect.create(
                RuleActionAssign.create("content", "data", testingUid),
                "data"
            )
        )

        ruleUtils.applyRuleEffects(
            testFieldViewModels,
            Result.success(testRuleEffects),
            actionCallbacks
        )

        verify(actionCallbacks, times(1)).save(testingUid, "data")
        Assert.assertTrue(testFieldViewModels[testingUid]!!.value().equals("data"))
        Assert.assertFalse(testFieldViewModels[testingUid]!!.editable()!!)
    }

    @Test
    fun `RuleActionAssign should set a value to a given field with value`() {
        val testingUid = "uid2"
        val testingUid2 = "uid3"

        testRuleEffects.add(
            RuleEffect.create(
                RuleActionAssign.create("content", "data", testingUid),
                "data"
            )
        )
        testRuleEffects.add(
            RuleEffect.create(
                RuleActionAssign.create("content", "data", testingUid2),
                "test"
            )
        )

        ruleUtils.applyRuleEffects(
            testFieldViewModels,
            Result.success(testRuleEffects),
            actionCallbacks
        )

        verify(actionCallbacks, times(1)).save(testingUid, "data")
        verify(actionCallbacks, times(0)).save(testingUid2, "test")
        Assert.assertTrue(testFieldViewModels[testingUid]!!.value().equals("data"))
        Assert.assertTrue(testFieldViewModels[testingUid2]!!.value().equals("test"))
        Assert.assertFalse(testFieldViewModels[testingUid]!!.editable()!!)
        Assert.assertFalse(testFieldViewModels[testingUid]!!.editable()!!)
    }

    @Test
    fun `RuleActionAssign should set a value to calculated value`() {
        testRuleEffects.add(
            RuleEffect.create(
                RuleActionAssign.create("content", "data", null),
                "data"
            )
        )

        ruleUtils.applyRuleEffects(
            testFieldViewModels,
            Result.success(testRuleEffects),
            actionCallbacks
        )

        verify(actionCallbacks, times(1)).setCalculatedValue("content", "data")
    }

    @Test
    fun `RuleActionSetMandatory should mark field as mandatory`() {
        val testingUid = "uid2"

        testRuleEffects.add(
            RuleEffect.create(
                RuleActionSetMandatoryField.create(testingUid),
                "data"
            )
        )

        ruleUtils.applyRuleEffects(
            testFieldViewModels,
            Result.success(testRuleEffects),
            actionCallbacks
        )

        Assert.assertTrue(testFieldViewModels[testingUid]!!.mandatory())
    }

    @Test
    fun `RuleActionWarningOnCompletion should set warning to field and allow completion`() {
        val testingUid = "uid1"

        testRuleEffects.add(
            RuleEffect.create(
                RuleActionWarningOnCompletion.create("content", "action_data", testingUid),
                "data"
            )
        )

        ruleUtils.applyRuleEffects(
            testFieldViewModels,
            Result.success(testRuleEffects),
            actionCallbacks
        )

        Assert.assertEquals(testFieldViewModels[testingUid]!!.warning(), "content data")
        verify(actionCallbacks, times(1)).setMessageOnComplete("content", true)
    }

    @Test
    fun `RuleActionErrorOnCompletion should set warning to field and not allow completion`() {
        val testingUid = "uid1"

        testRuleEffects.add(
            RuleEffect.create(
                RuleActionErrorOnCompletion.create("content", "action_data", testingUid),
                "data"
            )
        )

        ruleUtils.applyRuleEffects(
            testFieldViewModels,
            Result.success(testRuleEffects),
            actionCallbacks
        )

        Assert.assertEquals(testFieldViewModels[testingUid]!!.warning(), "content data")
        verify(actionCallbacks, times(1)).setMessageOnComplete("content", false)
    }

    @Test
    fun `RuleActionHideProgramStage should execute callback action`() {
        val testingUid = "stageUid"
        testRuleEffects.add(
            RuleEffect.create(
                RuleActionHideProgramStage.create(testingUid),
                "data"
            )
        )

        ruleUtils.applyRuleEffects(
            testFieldViewModels,
            Result.success(testRuleEffects),
            actionCallbacks
        )

        verify(actionCallbacks, times(1)).setHideProgramStage(testingUid)
    }

    @Test
    fun `RuleActionHideProgramStage should remove stage from possible selections`() {
    }

    @Test
    fun `RuleActionHideOption should execute callback action`() {
        testRuleEffects.add(
            RuleEffect.create(
                RuleActionHideOption.create("content", "optionUid", "field"),
                "data"
            )
        )

        ruleUtils.applyRuleEffects(
            testFieldViewModels,
            Result.success(testRuleEffects),
            actionCallbacks
        )

        verify(actionCallbacks, times(1)).setOptionToHide("optionUid")
    }

    @Test
    fun `RuleActionHideOptionGroup should execute callback action`() {
        testRuleEffects.add(
            RuleEffect.create(
                RuleActionHideOptionGroup.create("content", "optionGroupUid"),
                "data"
            )
        )

        ruleUtils.applyRuleEffects(
            testFieldViewModels,
            Result.success(testRuleEffects),
            actionCallbacks
        )

        verify(actionCallbacks, times(1)).setOptionGroupToHide("optionGroupUid", true)
    }

    @Test
    fun `RuleActionShowOptionGroup should execute callback action`() {
        testRuleEffects.add(
            RuleEffect.create(
                RuleActionShowOptionGroup.create("content", "optionGroupUid", "field"),
                "data"
            )
        )

        ruleUtils.applyRuleEffects(
            testFieldViewModels,
            Result.success(testRuleEffects),
            actionCallbacks
        )

        verify(actionCallbacks, times(1)).setOptionGroupToHide("optionGroupUid", false, "field")
    }
}
