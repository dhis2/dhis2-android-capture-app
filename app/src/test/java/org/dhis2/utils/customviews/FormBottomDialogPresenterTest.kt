package org.dhis2.utils.customviews

import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel
import org.dhis2.data.forms.dataentry.fields.image.ImageViewModel
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

    private fun mandatoryFields(): Map<String, FieldViewModel> {
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
                    null,null
                )
            ),
            Pair(
                "uid2",
                ImageViewModel.create(
                    "uid2.optionUid",
                    "label2_op_optionLabel_op_optionCode",
                    "optionSet",
                    "",
                    "section2",
                    true,
                    true,
                    null,
                    ObjectStyle.builder().build()
                )
            )
        )
    }
}
