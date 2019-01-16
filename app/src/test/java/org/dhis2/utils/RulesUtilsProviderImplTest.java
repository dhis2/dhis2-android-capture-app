package org.dhis2.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleActionShowWarning;
import org.hisp.dhis.rules.models.RuleEffect;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 07/11/2018.
 */
public class RulesUtilsProviderImplTest {

    String testUid = "XXXXXX";
    RulesUtilsProviderImpl ruleUtils = new RulesUtilsProviderImpl(new CodeGeneratorImpl());
    FieldViewModelFactoryImpl fieldFactory = new FieldViewModelFactoryImpl(
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "");
    List<RuleEffect> testRuleEffects = new ArrayList<>();

    RulesActionCallbacks actionCallbacks = new RulesActionCallbacks() {
        @Override
        public void setShowError(@NonNull RuleActionShowError showError, FieldViewModel model) {

        }

        @Override
        public void unsupportedRuleAction() {

        }

        @Override
        public void save(@NonNull String uid, @Nullable String value) {

        }

        @Override
        public void setDisplayKeyValue(String label, String value) {

        }

        @Override
        public void sethideSection(String sectionUid) {

        }

        @Override
        public void setMessageOnComplete(String content, boolean canComplete) {

        }

        @Override
        public void setHideProgramStage(String programStageUid) {

        }
    };

    @Test
    public void showWarningRuleActionTest() {

        HashMap<String, FieldViewModel> testFieldViewModels = new HashMap<>();
        testFieldViewModels.put(testUid, fieldFactory.create(testUid, "label",
                ValueType.TEXT, false, null, "test", null,
                null, true, null, null, null));
        testRuleEffects.add(RuleEffect.create(
                RuleActionShowWarning.create("content", "action_data", testUid),
                "data")
        );
        Result<RuleEffect> ruleEffect = Result.success(testRuleEffects);

        ruleUtils.applyRuleEffects(testFieldViewModels, ruleEffect, actionCallbacks);

        Assert.assertNotNull(testFieldViewModels.get(testUid).warning());
    }


}
