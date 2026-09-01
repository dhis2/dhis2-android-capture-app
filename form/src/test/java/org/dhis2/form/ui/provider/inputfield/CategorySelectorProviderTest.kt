package org.dhis2.form.ui.provider.inputfield

import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.junit.Assert.assertEquals
import org.junit.Test

class CategorySelectorProviderTest {
    @Test
    fun `should drop the error state once a category option is selected`() {
        assertEquals(
            InputShellState.UNFOCUSED,
            getInputState(InputShellState.ERROR, isEmpty = false),
        )
    }

    @Test
    fun `should keep the error state while no category option is selected`() {
        assertEquals(
            InputShellState.ERROR,
            getInputState(InputShellState.ERROR, isEmpty = true),
        )
    }

    @Test
    fun `should pass through any state other than error`() {
        assertEquals(
            InputShellState.DISABLED,
            getInputState(InputShellState.DISABLED, isEmpty = true),
        )
        assertEquals(
            InputShellState.UNFOCUSED,
            getInputState(InputShellState.UNFOCUSED, isEmpty = false),
        )
    }
}
