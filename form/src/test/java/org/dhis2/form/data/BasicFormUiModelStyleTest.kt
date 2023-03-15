package org.dhis2.form.data

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.form.ui.style.BasicFormUiModelStyle
import org.dhis2.form.ui.style.FormUiColorFactory
import org.dhis2.form.ui.style.FormUiColorType
import org.dhis2.form.ui.style.FormUiModelStyle
import org.hisp.dhis.android.core.common.ValueType
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class BasicFormUiModelStyleTest {

    private val colorFactory: FormUiColorFactory = mock()
    private val valueType: ValueType = mock()
    private lateinit var basicFormUiModelStyle: FormUiModelStyle

    @Before
    fun setUp() {
        basicFormUiModelStyle = BasicFormUiModelStyle(colorFactory, valueType, true)
    }

    @Test
    @Ignore
    fun shouldGetColorsFromStyle() {
        val mapOfColors = mapOf(
            FormUiColorType.PRIMARY to 1,
            FormUiColorType.TEXT_PRIMARY to 2,
            FormUiColorType.WARNING to 3,
            FormUiColorType.ERROR to 4
        )
        whenever(colorFactory.getBasicColors()) doReturn mapOfColors

        val result = basicFormUiModelStyle.getColors()

        assert(result[FormUiColorType.PRIMARY] == 1)
        assert(result[FormUiColorType.TEXT_PRIMARY] == 2)
        assert(result[FormUiColorType.WARNING] == 3)
        assert(result[FormUiColorType.ERROR] == 4)
    }
}
