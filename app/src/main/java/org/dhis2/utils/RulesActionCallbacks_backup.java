package org.dhis2.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.hisp.dhis.rules.models.RuleActionShowError;

/**
 * QUADRAM. Created by ppajuelo on 13/06/2018.
 */

public interface RulesActionCallbacks_backup {

    void setCalculatedValue(String calculatedValueVariable, String value);

    void setShowError(@NonNull RuleActionShowError showError, FieldViewModel model);

    void unsupportedRuleAction();

    void save(@NonNull String uid, @Nullable String value);

    void setDisplayKeyValue(String label, String value);

    void sethideSection(String sectionUid);

    void setMessageOnComplete(String content, boolean canComplete);

    void setHideProgramStage(String programStageUid);

    void setOptionToHide(String optionUid);

    void setOptionGroupToHide(String optionGroupUid);
}
