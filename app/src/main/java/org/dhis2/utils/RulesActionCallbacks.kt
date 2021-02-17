package org.dhis2.utils

import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.hisp.dhis.rules.models.RuleActionShowError

interface RulesActionCallbacks {

    fun setShowError(showError: RuleActionShowError, model: FieldViewModel?)

    fun unsupportedRuleAction()

    fun save(uid: String, value: String?)

    fun setMessageOnComplete(content: String, canComplete: Boolean)

    fun setHideProgramStage(programStageUid: String)

    fun setOptionToHide(optionUid: String, field: String)

    fun setOptionGroupToHide(optionGroupUid: String, toHide: Boolean, field: String)
}
