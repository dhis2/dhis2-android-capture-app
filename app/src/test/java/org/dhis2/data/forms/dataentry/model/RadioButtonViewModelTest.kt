package org.dhis2.data.forms.dataentry.model

import org.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonViewModel
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType
import org.junit.Assert
import org.junit.Test

class RadioButtonViewModelTest {

    @Test
    fun `Should set mandatory to true`() {
        val radioButtonViewModel = RadioButtonViewModel.fromRawValue(
            "123",
            1,
            "label",
            ValueType.BOOLEAN,
            false,
            RadioButtonViewModel.Value.CHECKED.toString(),
            "section",
            true,
            "none",
            ObjectStyle.builder().build(),
            ValueTypeRenderingType.DEFAULT,
            false,
            false,
            null
        )
        val radioButtonMandatory = radioButtonViewModel.setMandatory()

        Assert.assertTrue(radioButtonMandatory.mandatory())
    }
}
