package org.dhis2.utils.customviews

import io.reactivex.processors.PublishProcessor
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel
import org.dhis2.data.forms.dataentry.fields.visualOptionSet.MatrixOptionSetModel
import org.dhis2.form.model.FieldUiModel
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
                EditTextViewModel.create(
                    "uid1",
                    "label1",
                    true,
                    null,
                    "hint",
                    1,
                    ValueType.TEXT,
                    "section",
                    true,
                    null,
                    null,
                    ObjectStyle.builder().build(),
                    null,
                    "any",
                    false,
                    false,
                    PublishProcessor.create(),
                    null,
                    null
                )
            ),
            Pair(
                "uid2",
                MatrixOptionSetModel.create(
                    "uid2",
                    "label2",
                    true,
                    null,
                    "section2",
                    true,
                    "optionSetUid",
                    null,
                    ObjectStyle.builder().build(),
                    PublishProcessor.create(),
                    null,
                    emptyList(),
                    2
                )
            )
        )
    }
}
