package org.dhis2.form.data

import org.dhis2.form.model.FieldUiModelImpl
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.option.Option
import org.junit.Test

class FieldUiModelTest {

    @Test
    fun `should set optionsToDisplay when there is optionsToHide`() {
        val optionsToHide = listOf("1", "2")
        val matrixOptionSetModel = fieldUiModel().also {
            it.optionsToHide = optionsToHide
        }
        assert(matrixOptionSetModel.optionsToDisplay?.map { it.uid() } == listOf("3", "4", "5"))
    }

    @Test
    fun `should set optionsToDisplay when there is optionsToShow`() {
        val optionsInGroupToShow = listOf("1", "2")
        val matrixOptionSetModel = fieldUiModel().also {
            it.optionsToShow = optionsInGroupToShow
        }
        assert(matrixOptionSetModel.optionsToDisplay?.map { it.uid() } == listOf("1", "2"))
    }

    @Test
    fun `should set optionsToDisplay when there are optionsToShow and optionsToHide`() {
        val optionsToHide = listOf("1")
        val optionsInGroupToShow = listOf("3", "5")

        val matrixOptionSetModel = fieldUiModel().also {
            it.optionsToHide = optionsToHide
            it.optionsToShow = optionsInGroupToShow
        }
        assert(matrixOptionSetModel.optionsToDisplay?.map { it.uid() } == listOf("3", "5"))
    }

    private fun fieldUiModel() = FieldUiModelImpl(
        "uid",
        1,
        label = "label",
        options = listOf(
            Option.builder().uid("1").build(),
            Option.builder().uid("2").build(),
            Option.builder().uid("3").build(),
            Option.builder().uid("4").build(),
            Option.builder().uid("5").build()
        ),
        valueType = ValueType.TEXT
    )
}
