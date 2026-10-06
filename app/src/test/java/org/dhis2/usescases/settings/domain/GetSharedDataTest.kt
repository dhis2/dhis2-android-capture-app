package org.dhis2.usescases.settings.domain

import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import org.dhis2.usescases.settings.models.ErrorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetSharedDataTest {
    private lateinit var getSharedData: GetSharedData

    @Before
    fun setUp() {
        getSharedData = GetSharedData()
    }

    @Test
    fun `should return json with only selected errors`() =
        runTest {
            val selectedError =
                ErrorViewModel(
                    creationDate = null,
                    creationDateLabel = "label-1",
                    errorCode = "1",
                    errorDescription = "selected error",
                    errorComponent = "component-1",
                    isSelected = true,
                )
            val unselectedError =
                ErrorViewModel(
                    creationDate = null,
                    creationDateLabel = "label-2",
                    errorCode = "2",
                    errorDescription = "unselected error",
                    errorComponent = "component-2",
                    isSelected = false,
                )

            val result = getSharedData(listOf(selectedError, unselectedError))

            assertTrue(result.isSuccess)
            assertEquals(Gson().toJson(listOf(selectedError)), result.getOrNull())
        }

    @Test
    fun `should return empty json array when no errors are selected`() =
        runTest {
            val unselectedError =
                ErrorViewModel(
                    creationDate = null,
                    creationDateLabel = "label",
                    errorCode = "1",
                    errorDescription = "unselected error",
                    errorComponent = "component",
                    isSelected = false,
                )

            val result = getSharedData(listOf(unselectedError))

            assertTrue(result.isSuccess)
            assertEquals("[]", result.getOrNull())
        }

    @Test
    fun `should return empty json array when input list is empty`() =
        runTest {
            val result = getSharedData(emptyList())

            assertTrue(result.isSuccess)
            assertEquals("[]", result.getOrNull())
        }
}
