package org.dhis2.utils.customviews

import org.dhis2.data.forms.dataentry.fields.visualOptionSet.MatrixOptionSetModel
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertTrue
import org.junit.Test

class FormBottomDialogPresenterTest {
    private val presenter = FormBottomDialogPresenter()

    @Test
    fun `Should return current message is showMandatoryFields is false`() {
        val result = presenter.appendMandatoryFieldList(false, HashMap(), "currentMessage")
        assertTrue(
            result == "currentMessage"
        )
    }

    @Test
    fun `Should append fields names to current message is showMandatoryFields is true`() {
        val result = presenter.appendMandatoryFieldList(true, mandatoryFields(), "currentMessage")
        assertTrue(
            result == "currentMessage\nlabel1\nlabel2"
        )
    }

    private fun mandatoryFields(): Map<String, FieldUiModel> {
        return mapOf(
            Pair(
                "uid1",
                FieldUiModelImpl(
                    uid = "uid1",
                    layoutId = 1,
                    value = "value",
                    mandatory = true,
                    label = "label1",
                    programStageSection = "section",
                    valueType = ValueType.TEXT
                )
            ),
            Pair(
                "uid2",
                MatrixOptionSetModel.create(
                    "uid2",
                    1,
                    "label2",
                    true,
                    null,
                    "section2",
                    true,
                    "optionSetUid",
                    null,
                    ObjectStyle.builder().build(),
                    emptyList(),
                    2,
                    ValueType.TEXT
                )
            )
        )
    }
}
