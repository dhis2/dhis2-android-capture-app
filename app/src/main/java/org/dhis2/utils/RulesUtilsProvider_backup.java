package org.dhis2.utils;

import androidx.annotation.NonNull;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.Map;

/**
 * QUADRAM. Created by ppajuelo on 13/06/2018.
 */

public interface RulesUtilsProvider_backup {


    void applyRuleEffects(Map<String, FieldViewModel> fieldViewModels, Result<RuleEffect> calcResult,
                          @NonNull RulesActionCallbacks rulesActionCallbacks);

    void applyRuleEffects(Map<String, ProgramStage> programStages, Result<RuleEffect> calcResult);

}
