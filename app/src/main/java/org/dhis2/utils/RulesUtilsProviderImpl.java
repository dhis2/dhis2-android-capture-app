package org.dhis2.utils;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel;

import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleActionDisplayKeyValuePair;
import org.hisp.dhis.rules.models.RuleActionDisplayText;
import org.hisp.dhis.rules.models.RuleActionHideField;
import org.hisp.dhis.rules.models.RuleActionHideSection;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleActionShowWarning;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.Map;

/**
 * QUADRAM. Created by ppajuelo on 13/06/2018.
 */

public class RulesUtilsProviderImpl implements RulesUtilsProvider {

    private final CodeGenerator codeGenerator;

    public RulesUtilsProviderImpl(CodeGenerator codeGenerator) {
        this.codeGenerator = codeGenerator;
    }

    @Override
    public void applyRuleEffects(Map<String, FieldViewModel> fieldViewModels, Result<RuleEffect> calcResult) {
        for (RuleEffect ruleEffect : calcResult.items()) {
            RuleAction ruleAction = ruleEffect.ruleAction();

            if (ruleAction instanceof RuleActionShowWarning) {

                RuleActionShowWarning showWarning = (RuleActionShowWarning) ruleAction;
                FieldViewModel model = fieldViewModels.get(showWarning.field());
                showWarning(showWarning, model);


            } else if (ruleAction instanceof RuleActionShowError) {
                RuleActionShowError showError = (RuleActionShowError) ruleAction;
                FieldViewModel model = fieldViewModels.get(showError.field());

                if (model != null && model instanceof EditTextViewModel) {
                    fieldViewModels.put(showError.field(),
                            ((EditTextViewModel) model).withError(showError.content()));
                }
            } else if (ruleAction instanceof RuleActionHideField) {
                RuleActionHideField hideField = (RuleActionHideField) ruleAction;
                fieldViewModels.remove(hideField.field());
            } else if (ruleAction instanceof RuleActionDisplayText) {
                String uid = codeGenerator.generate();
                RuleActionDisplayText displayText = (RuleActionDisplayText) ruleAction;
                EditTextViewModel textViewModel = EditTextViewModel.create(uid,
                        displayText.content(), false, displayText.data(), "Information", 1, ValueType.TEXT,
                        fieldViewModels.get(0).programStageSection(),
                        false,null);
                fieldViewModels.put(uid, textViewModel);
            } else if (ruleAction instanceof RuleActionDisplayKeyValuePair) {
                String uid = codeGenerator.generate();

                RuleActionDisplayKeyValuePair displayText =
                        (RuleActionDisplayKeyValuePair) ruleAction;

                EditTextViewModel textViewModel = EditTextViewModel.create(uid,
                        displayText.content(), false, displayText.data(), displayText.content(), 1, ValueType.TEXT, null, false,null);
                fieldViewModels.put(uid, textViewModel);

            } else if (ruleAction instanceof RuleActionHideSection) {
                RuleActionHideSection hideSection = (RuleActionHideSection) ruleAction;
//                dataEntryView.removeSection(hideSection.programStageSection()); TODO: check how to tell view to remove sections
            }
        }
    }

    /**
     * */
    private void showWarning(RuleActionShowWarning showWarning, FieldViewModel model) {
        /*if (model != null && model instanceof EditTextViewModel) {
            fieldViewModels.put(showWarning.field(),
                    ((EditTextViewModel) model).withWarning(showWarning.content()));
        }*/
    }
}
