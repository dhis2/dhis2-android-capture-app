package org.dhis2.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.rules.models.RuleActionDisplayText;
import org.hisp.dhis.rules.models.RuleActionHideField;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleActionShowWarning;
import org.hisp.dhis.rules.models.RuleEffect;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;


/**
 * QUADRAM. Created by ppajuelo on 07/11/2018.
 */
public class RulesUtilsProviderImplTest {

    String testUid = "XXXXXX";
    private RulesUtilsProviderImpl ruleUtils = new RulesUtilsProviderImpl();
    private FieldViewModelFactoryImpl fieldFactory = new FieldViewModelFactoryImpl(
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "");

    private List<RuleEffect> testRuleEffects = new ArrayList<>();

    HashMap<String, FieldViewModel> testFieldViewModels = new HashMap<>();

    RulesActionCallbacks actionCallbacks = new RulesActionCallbacks() {

        @Override
        public void setCalculatedValue(String calculatedValueVariable, String value) {

        }

        @Override
        public void setShowError(@NonNull RuleActionShowError showError, FieldViewModel model) {

        }

        @Override
        public void unsupportedRuleAction() {

        }

        @Override
        public void save(@NotNull @NonNull String uid, @Nullable String value) {

        }

        @Override
        public void setDisplayKeyValue(String label, String value) {

        }

        @Override
        public void setHideSection(String sectionUid) {

        }

        @Override
        public void setMessageOnComplete(String content, boolean canComplete) {

        }

        @Override
        public void setHideProgramStage(String programStageUid) {

        }

        @Override
        public void setOptionToHide(String optionUid) {

        }

        @Override
        public void setOptionGroupToHide(@NotNull String optionGroupUid, boolean toHide, @NotNull String field) {

        }
    };

    private void putFieldViewModel() {
        testFieldViewModels.put(testUid, fieldFactory.create(testUid, "label",
                ValueType.TEXT, false, "optionSet", "test", "section",
                null, true, null, null, null, 1, ObjectStyle.builder().build(),""));
    }

    @Ignore
    @Test
    public void showWarningRuleActionTest() {

        HashMap<String, FieldViewModel> testFieldViewModels = new HashMap<>();
        String testUid = "XXXXXX";
        testFieldViewModels.put(testUid, fieldFactory.create(testUid, "label",
                ValueType.TEXT, false, "", "test", null,
                null, true, null, null,
                null, null, ObjectStyle.builder().build(),""));

        putFieldViewModel();

        testRuleEffects.add(RuleEffect.create(
                RuleActionShowWarning.create("content", "action_data", testUid),
                "data")
        );
        Result<RuleEffect> ruleEffect = Result.success(testRuleEffects);

        ruleUtils.applyRuleEffects(testFieldViewModels, ruleEffect, actionCallbacks);

        Assert.assertNotNull(testFieldViewModels.get(testUid).warning());
    }

    @Test
    public void showErrorRuleActionTest() {

        putFieldViewModel();

        testRuleEffects.add(RuleEffect.create(
                RuleActionShowError.create("content", "action_data", testUid),
                "data")
        );
        Result<RuleEffect> ruleEffect = Result.success(testRuleEffects);

        ruleUtils.applyRuleEffects(testFieldViewModels, ruleEffect, actionCallbacks);

        Assert.assertNotNull(testFieldViewModels.get(testUid).error());
    }

    @Test
    public void hideFieldRuleActionTest() {

        putFieldViewModel();

        testRuleEffects.add(RuleEffect.create(
                RuleActionHideField.create("content", testUid),
                "data")
        );
        Result<RuleEffect> ruleEffect = Result.success(testRuleEffects);

        ruleUtils.applyRuleEffects(testFieldViewModels, ruleEffect, actionCallbacks);

        assertThat(testFieldViewModels).doesNotContainKey(testUid);

    }

    @Test
    public void displayTextRuleActionTest() {

        putFieldViewModel();

        testRuleEffects.add(RuleEffect.create(
                RuleActionDisplayText.createForIndicators("content", "data"),
                "data")
        );
        Result<RuleEffect> ruleEffect = Result.success(testRuleEffects);

        ruleUtils.applyRuleEffects(testFieldViewModels, ruleEffect, actionCallbacks);

        assertThat(testFieldViewModels).containsKey("content");
    }
}
