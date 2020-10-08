package org.dhis2.data.forms.dataentry.fields.image

import org.hisp.dhis.android.core.common.ObjectStyle
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageViewModelTest {

    @Test
    fun `Should return the field uid`() {
        assertTrue(getImageViewModel().fieldUid() == "uid2")
    }

    @Test
    fun `Should return the optionUid`() {
        assertTrue(getImageViewModel().optionUid() == "optionUid")
    }

    @Test
    fun `Should return the field label`() {
        assertTrue(getImageViewModel().fieldDisplayName() == "label2")
    }

    @Test
    fun `Should return the option label`() {
        assertTrue(getImageViewModel().optionDisplayName() == "optionLabel")
    }

    @Test
    fun `Should return the option code`() {
        assertTrue(getImageViewModel().optionCode() == "optionCode")
    }

    @Test
    fun `Should return the option label when no mandatory`() {
        assertTrue(getImageViewModel().formattedLabel == "optionLabel")
    }

    @Test
    fun `Should return the option label with mandatory mark when field is mandatory`() {
        assertTrue(getImageViewModel(true).formattedLabel == "optionLabel *")
    }

    fun getImageViewModel(mandatory: Boolean = false): ImageViewModel {
        return ImageViewModel.create(
            "uid2.optionUid",
            "label2_op_optionLabel_op_optionCode",
            "optionSet",
            "",
            "section2",
            true,
            mandatory,
            null,
            ObjectStyle.builder().build()
        )
    }
}
