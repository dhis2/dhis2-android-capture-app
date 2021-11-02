package org.dhis2.data.forms.dataentry.fields

import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel
import org.dhis2.form.ui.validation.validators.PatternValidator
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldViewModelTest {

    @Test
    fun `should validate text fieldmask`() {
        assertFalse(mockedTextFieldViewModel("\\w\\w\\x\\x").fieldMaskIsCorrect())
        assertTrue(mockedTextFieldViewModel("\'w\'w\'x\'x").fieldMaskIsCorrect())
    }

    @Test
    fun `should call onSuccess if valueToEvaluate is null`() {
        mockedTextFieldViewModel("\'w\'w\'x\'x").validateWithFieldMask(
            null,
            object : PatternValidator {
                override fun onSuccess() {
                    assertTrue(true)
                }

                override fun onError() {
                    assertTrue(false)
                }

                override fun onPatternError() {
                    assertTrue(false)
                }
            }
        )
    }

    @Test
    fun `should call onSuccess if valueToEvaluate is empty`() {
        mockedTextFieldViewModel("\'w\'w\'x\'x").validateWithFieldMask(
            "",
            object : PatternValidator {
                override fun onSuccess() {
                    assertTrue(true)
                }

                override fun onError() {
                    assertTrue(false)
                }

                override fun onPatternError() {
                    assertTrue(false)
                }
            }
        )
    }

    @Test
    fun `should call onSuccess if fieldMask is null`() {
        mockedTextFieldViewModel(null).validateWithFieldMask(
            "hello",
            object : PatternValidator {
                override fun onSuccess() {
                    assertTrue(true)
                }

                override fun onError() {
                    assertTrue(false)
                }

                override fun onPatternError() {
                    assertTrue(false)
                }
            }
        )
    }

    @Test
    fun `should call onPatterError if fieldMask is wrong`() {
        mockedTextFieldViewModel("\\w\\w\\x\\x").validateWithFieldMask(
            "hello",
            object : PatternValidator {
                override fun onSuccess() {
                    assertTrue(false)
                }

                override fun onError() {
                    assertTrue(false)
                }

                override fun onPatternError() {
                    assertTrue(true)
                }
            }
        )
    }

    @Test
    fun `should call onSuccess if value matches pattern`() {
        mockedTextFieldViewModel("\\w\\d").validateWithFieldMask(
            "A1",
            object : PatternValidator {
                override fun onSuccess() {
                    assertTrue(true)
                }

                override fun onError() {
                    assertTrue(false)
                }

                override fun onPatternError() {
                    assertTrue(false)
                }
            }
        )
    }

    @Test
    fun `should call onError if value does not matches pattern`() {
        mockedTextFieldViewModel("\\w\\d").validateWithFieldMask(
            "AA1",
            object : PatternValidator {
                override fun onSuccess() {
                    assertTrue(false)
                }

                override fun onError() {
                    assertTrue(true)
                }

                override fun onPatternError() {
                    assertTrue(false)
                }
            }
        )
    }

    private fun mockedTextFieldViewModel(fieldMask: String?) =
        EditTextViewModel.create(
            "uid",
            -1,
            "label",
            false,
            null,
            "hint",
            1,
            ValueType.TEXT,
            null,
            true,
            null,
            null,
            ObjectStyle.builder().build(),
            fieldMask,
            "",
            true,
            false,
            null
        )
}
