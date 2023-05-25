package org.dhis2.usescases.main.program

import org.dhis2.usescases.main.program.HomeItemType.PROGRAM_STOCK
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class IdentifyProgramTypeTest {

    private lateinit var identifyProgramType: IdentifyProgramType

    private val repository: ProgramThemeRepository = mock {
        on { isStockTheme(PROGRAM_UID) } doReturn true
    }

    @Before
    fun setup() {
        identifyProgramType = IdentifyProgramType(repository)
    }

    @Test
    fun shouldReturnStockProgramType() {
        // Given user has a list of programs with a program of type Stock
        // When taps on the program of type stock
        val result: HomeItemType = identifyProgramType(PROGRAM_UID)

        // Then it should return program type STOCK
        assertEquals(result, PROGRAM_STOCK)
    }

    @Test
    fun shouldReturnDefaultProgramType() {
        // Given user has a list of programs with a program of type Stock
        // When taps on a program different that type stock
        val result: HomeItemType = identifyProgramType("1234ABCD")

        // Then it should return program, type DEFAULT
        assertEquals(result, HomeItemType.PROGRAM)
    }

    companion object {
        const val PROGRAM_UID = "F5ijs28K4s8"
    }
}
