package org.dhis2.usescases.main.program

import org.hisp.dhis.android.core.usecase.stock.StockUseCase
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class StockManagementMapperTest {

    private lateinit var stockManagementMapper: StockManagementMapper

    private val stockTheme: StockUseCase = mock()
    private val repository: ProgramThemeRepository = mock {
        on { getStockTheme(PROGRAM_UID) } doReturn stockTheme
    }

    @Before
    fun setup() {
        stockManagementMapper = StockManagementMapper(repository)
    }

    @Test
    fun shouldMapAProgramUidToStockManagementModel() {
        // When getting Stock model from given uid
        val result = repository.getStockTheme(PROGRAM_UID)

        // Then it should return the model
        assertNotNull(result)
    }

    companion object {
        const val PROGRAM_UID = "F5ijs28K4s8"
    }
}
