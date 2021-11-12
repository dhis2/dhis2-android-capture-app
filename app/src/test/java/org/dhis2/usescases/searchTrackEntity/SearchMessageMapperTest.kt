package org.dhis2.usescases.searchTrackEntity

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiModel
import org.hisp.dhis.android.core.program.Program
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchMessageMapperTest {

    private val searchResources: SearchResources = mock()
    private val searchMessageMapper = SearchMessageMapper(searchResources)

    @Test
    fun `should display online error message`() {
        searchMessageMapper.getSearchMessage(
            mockedOnlineErrorList(),
            null,
            emptyMap(),
            true,
            5,
            "Test"
        ).apply {
            assertTrue(
                message == "online error\nonline error 2" &&
                    !canRegister &&
                    !showButton &&
                    !forceSearch
            )
        }
    }

    @Test
    fun `should display min attributes message while searching`() {
        val expectedMessage = "min attributes msg"
        whenever(searchResources.searchMinNumAttributes(any())) doReturn expectedMessage
        searchMessageMapper.getSearchMessage(
            emptyList(),
            mockedProgramWithMinAttributes(),
            mapOf(Pair("attributeUid", "value")),
            true,
            5,
            "Test"
        ).apply {
            assertTrue(
                message == expectedMessage &&
                    !canRegister &&
                    !showButton &&
                    !forceSearch
            )
        }
    }

    @Test
    fun `should display max tei reached message while searching`() {
        val expectedMessage = "max tei reached"
        whenever(searchResources.searchMaxTeiReached(any())) doReturn expectedMessage
        searchMessageMapper.getSearchMessage(
            mockedList(),
            mockedProgramWithMaxTei(),
            mapOf(Pair("attributeUid", "value")),
            true,
            5,
            "Test"
        ).apply {
            assertTrue(
                message == expectedMessage &&
                    !canRegister &&
                    !showButton &&
                    !forceSearch
            )
        }
    }

    @Test
    fun `should display no result message while searching`() {
        val expectedMessage = "no result"
        whenever(searchResources.searchCriteriaNotMet(any())) doReturn expectedMessage
        searchMessageMapper.getSearchMessage(
            emptyList(),
            mockedProgramWithMaxTei(),
            mapOf(Pair("attributeUid", "value")),
            true,
            5,
            "Test"
        ).apply {
            assertTrue(
                message == expectedMessage &&
                    canRegister &&
                    !showButton &&
                    !forceSearch
            )
        }
    }

    @Test
    fun `should not display message if checks pass`() {
        val expectedMessage = ""
        searchMessageMapper.getSearchMessage(
            mockedList(),
            mockedProgram(),
            mapOf(Pair("attributeUid", "value")),
            true,
            5,
            "Test"
        ).apply {
            assertTrue(
                message == expectedMessage &&
                    canRegister &&
                    showButton &&
                    !forceSearch
            )
        }
    }

    @Test
    fun `should display front page if not searching`() {
        val expectedMessage = ""
        searchMessageMapper.getSearchMessage(
            mockedList(),
            mockedProgramWithFrontPage(),
            emptyMap(),
            true,
            5,
            "Test"
        ).apply {
            assertTrue(
                message == expectedMessage &&
                    !canRegister &&
                    !showButton &&
                    !forceSearch
            )
        }
    }

    @Test
    fun `should display init search message`() {
        val expectedMessage = "init search"
        whenever(searchResources.searchInit()) doReturn expectedMessage
        searchMessageMapper.getSearchMessage(
            mockedList(),
            mockedProgram(),
            emptyMap(),
            true,
            5,
            "Test"
        ).apply {
            assertTrue(
                message == expectedMessage &&
                    !canRegister &&
                    !showButton &&
                    forceSearch
            )
        }
    }

    @Test
    fun `should display no attributes message`() {
        val expectedMessage = "no attributes"
        whenever(searchResources.teiTypeHasNoAttributes(any())) doReturn expectedMessage
        searchMessageMapper.getSearchMessage(
            mockedList(),
            null,
            emptyMap(),
            false,
            5,
            "Test"
        ).apply {
            assertTrue(
                message == expectedMessage &&
                    !canRegister &&
                    !showButton &&
                    !forceSearch
            )
        }
    }

    @Test
    fun `should display no result message while global searching`() {
        val expectedMessage = "no result"
        whenever(searchResources.searchCriteriaNotMet(any())) doReturn expectedMessage
        searchMessageMapper.getSearchMessage(
            emptyList(),
            null,
            mapOf(Pair("attributeUid", "value")),
            true,
            5,
            "Test"
        ).apply {
            assertTrue(
                message == expectedMessage &&
                    canRegister &&
                    !showButton &&
                    !forceSearch
            )
        }
    }

    @Test
    fun `should display max tei reached message while global searching`() {
        val expectedMessage = "max tei reached"
        whenever(searchResources.searchMaxTeiReached(any())) doReturn expectedMessage
        searchMessageMapper.getSearchMessage(
            mockedList(),
            null,
            mapOf(Pair("attributeUid", "value")),
            true,
            1,
            "Test"
        ).apply {
            assertTrue(
                message == expectedMessage &&
                    !canRegister &&
                    !showButton &&
                    !forceSearch
            )
        }
    }

    @Test
    fun `should not display message if global searching checks pass`() {
        val expectedMessage = ""
        searchMessageMapper.getSearchMessage(
            mockedList(),
            null,
            mapOf(Pair("attributeUid", "value")),
            true,
            5,
            "Test"
        ).apply {
            assertTrue(
                message == expectedMessage &&
                    canRegister &&
                    showButton &&
                    !forceSearch
            )
        }
    }

    @Test
    fun `should display init search message in global search`() {
        val expectedMessage = "init search"
        whenever(searchResources.searchInit()) doReturn expectedMessage
        searchMessageMapper.getSearchMessage(
            emptyList(),
            null,
            emptyMap(),
            true,
            5,
            "Test"
        ).apply {
            assertTrue(
                message == expectedMessage &&
                    !canRegister &&
                    !showButton &&
                    forceSearch
            )
        }
    }

    private fun mockedOnlineErrorList() = listOf(
        SearchTeiModel().apply { onlineErrorMessage = "online error" },
        SearchTeiModel().apply { onlineErrorMessage = "online error 2" }
    )

    private fun mockedList() = listOf(
        SearchTeiModel(),
        SearchTeiModel()
    )

    private fun mockedProgram() = Program.builder()
        .uid("programUid")
        .build()

    private fun mockedProgramWithMinAttributes() = Program.builder()
        .uid("programUid")
        .minAttributesRequiredToSearch(2)
        .build()

    private fun mockedProgramWithMaxTei() = Program.builder()
        .uid("programUid")
        .maxTeiCountToReturn(1)
        .build()

    private fun mockedProgramWithFrontPage() = Program.builder()
        .uid("programUid")
        .displayFrontPageList(true)
        .build()
}
