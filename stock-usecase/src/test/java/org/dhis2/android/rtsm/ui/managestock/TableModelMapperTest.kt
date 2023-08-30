package org.dhis2.android.rtsm.ui.managestock

import org.dhis2.android.rtsm.R
import org.dhis2.commons.resources.ResourceManager
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class TableModelMapperTest {

    private val resources: ResourceManager = mock()
    private lateinit var mapper: TableModelMapper

    @Before
    fun setUp() {
        whenever(
            resources.getString(R.string.invalid_possitive_zero),
        ) doReturn ONLY_POSITIVE_NUMBERS
        whenever(
            resources.getString(R.string.formatting_error),
        ) doReturn FORMATTING_ERROR

        mapper = TableModelMapper(resources = resources)
    }

    @Test
    fun shouldReturnNegativeValueError() {
        val result = mapper.validate("-5")
        assertEquals(result, ONLY_POSITIVE_NUMBERS)
    }

    @Test
    fun shouldReturnNumberFormatException() {
        val result = mapper.validate("999999999999999")
        assertEquals(result, FORMATTING_ERROR)
    }

    companion object {
        const val ONLY_POSITIVE_NUMBERS = "Only positive numbers or zero allowed"
        const val FORMATTING_ERROR = "Formatting error"
    }
}
