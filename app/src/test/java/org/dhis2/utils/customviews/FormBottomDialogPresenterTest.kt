package org.dhis2.utils.customviews

import org.junit.Assert.assertTrue
import org.junit.Test

class FormBottomDialogPresenterTest {
    private val presenter = FormBottomDialogPresenter()

    @Test
    fun `Should return current message is showMandatoryFields is false`() {
        val result = presenter.appendMandatoryFieldList(false, HashMap(), "currentMessage")
        assertTrue(
            result == "currentMessage",
        )
    }

    @Test
    fun `Should append fields names to current message is showMandatoryFields is true`() {
        val result = presenter.appendMandatoryFieldList(true, mandatoryFields(), "currentMessage")
        assertTrue(
            result == "currentMessage\nlabel1\nlabel2",
        )
    }

    private fun mandatoryFields(): Map<String, String> {
        return mapOf(
            Pair("label1", "section"),
            Pair("label2", "section2"),
        )
    }
}
