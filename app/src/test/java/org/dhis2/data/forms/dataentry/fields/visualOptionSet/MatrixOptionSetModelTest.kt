package org.dhis2.data.forms.dataentry.fields.visualOptionSet

import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.option.Option
import org.junit.Test

class MatrixOptionSetModelTest {

    @Test
    fun `should set optionsToHide when there is program rule of hide option`() {
        val optionsToHide = listOf("1", "2")
        val matrixOptionSetModel = matrixOptionSetModel().setOptionsToHide(
            optionsToHide,
            emptyList(),
            emptyList()
        )
        assert(matrixOptionSetModel.optionsToShow().map { it.uid() } == listOf("3", "4", "5"))
    }

    @Test
    fun `should set optionsToHide when there is program rule of hide option group`() {
        val optionsInGroupToHide = listOf("1", "2")
        val matrixOptionSetModel = matrixOptionSetModel().setOptionsToHide(
            emptyList(),
            optionsInGroupToHide,
            emptyList()
        )
        assert(matrixOptionSetModel.optionsToShow().map { it.uid() } == listOf("3", "4", "5"))
    }

    @Test
    fun `should set optionsToHide when there is program rule of show option group`() {
        val optionsInGroupToShow = listOf("1", "2")
        val matrixOptionSetModel = matrixOptionSetModel().setOptionsToHide(
            emptyList(),
            emptyList(),
            optionsInGroupToShow
        )
        assert(matrixOptionSetModel.optionsToShow().map { it.uid() } == listOf("1", "2"))
    }

    @Test
    fun `should set optionsToHide when there are program rules of show and hide option group`() {
        val optionsToHide = listOf("1")
        val optionsInGroupToHide = listOf("2", "4")
        val optionsInGroupToShow = listOf("3", "5")

        val matrixOptionSetModel = matrixOptionSetModel().setOptionsToHide(
            optionsToHide,
            optionsInGroupToHide,
            optionsInGroupToShow
        )
        assert(matrixOptionSetModel.optionsToShow().map { it.uid() } == listOf("3", "5"))
    }

    private fun matrixOptionSetModel() = MatrixOptionSetModel.create(
        "",
        1,
        "",
        false,
        "",
        "",
        true,
        "",
        "",
        ObjectStyle.builder().build(),
        listOf(
            Option.builder().uid("1").build(),
            Option.builder().uid("2").build(),
            Option.builder().uid("3").build(),
            Option.builder().uid("4").build(),
            Option.builder().uid("5").build()
        ),
        5,
        ValueType.TEXT,
        null
    )
}
