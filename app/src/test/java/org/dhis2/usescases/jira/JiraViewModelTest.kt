package org.dhis2.usescases.jira

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.nhaarman.mockitokotlin2.willAnswer
import junit.framework.Assert.assertTrue
import org.dhis2.data.jira.JiraIssue
import org.dhis2.data.jira.JiraIssuesResult
import org.dhis2.utils.resources.ResourceManager
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor

class JiraViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var jiraViewModel: JiraViewModel
    private val resourceManager: ResourceManager = mock()
    private val jiraRepository: JiraRepository = mock()

    @Before
    fun setUp() {
        jiraViewModel = JiraViewModel(jiraRepository, resourceManager)
    }

    @Test
    fun `Should get jira tickets if user is set`() {
        whenever(jiraRepository.hasJiraSessionSaved()) doReturn true
        jiraViewModel.init()
        verify(jiraRepository, times(1)).getJiraIssues(anyOrNull(), anyOrNull())
    }

    @Test
    fun `Should not get jira tickets if username is null or empty`() {
        jiraViewModel.onJiraPassChanged("pass", 0, 0, 0)
        jiraViewModel.openSession()
        verify(jiraRepository, times(0)).getJiraIssues(anyOrNull(), anyOrNull())
    }

    @Test
    fun `Should not get jira tickets if pass is null or empty`() {
        jiraViewModel.onJiraUserChanged("userName", 0, 0, 0)
        jiraViewModel.openSession()
        verify(jiraRepository, times(0)).getJiraIssues(anyOrNull(), anyOrNull())
    }

    @Test
    fun `Should open session if both pass and username are not null or empty`() {
        jiraViewModel.onJiraPassChanged("pass", 0, 0, 0)
        jiraViewModel.onJiraUserChanged("userName", 0, 0, 0)
        jiraViewModel.openSession()
        verify(jiraRepository, times(1)).setAuth("userName", "pass")
        verify(jiraRepository, times(1)).getJiraIssues(anyOrNull(), anyOrNull())
    }

    @Test
    fun `Should close session`() {
        jiraViewModel.closeSession()
        verify(jiraRepository, times(1)).closeSession()
        assertTrue(jiraViewModel.isSessionOpen.get() == false)
    }

    @Test
    fun `Should handle throwable error when send issue fails`() {
        jiraViewModel.onSummaryChanged("summary", 0, 0, 0)
        jiraViewModel.onDescriptionChanged("description", 0, 0, 0)
        val observer: Observer<String> = mock()
        jiraViewModel.issueMessage.observeForever(observer)
        given(
            jiraRepository.sendJiraIssue(
                any(),
                any(),
                any(),
                any()
            )
        ) willAnswer {
            jiraViewModel.handleThrowableResponse(Throwable("error"))
        }
        jiraViewModel.sendIssue()

        val captor = ArgumentCaptor.forClass(String::class.java)
        captor.run {
            verify(observer, times(1)).onChanged(capture())
            assertTrue(value == "error")
        }
    }

    @Test
    fun `Should show jira issue error sent message`() {
        jiraViewModel.onSummaryChanged("summary", 0, 0, 0)
        jiraViewModel.onDescriptionChanged("description", 0, 0, 0)
        val observer: Observer<String> = mock()
        jiraViewModel.issueMessage.observeForever(observer)
        given(
            jiraRepository.sendJiraIssue(
                any(),
                any(),
                any(),
                any()
            )
        ) willAnswer {
            jiraViewModel.handleSendResponse(false)
        }
        whenever(
            resourceManager.jiraIssueSentErrorMessage()
        ) doReturn "Sending error"
        jiraViewModel.sendIssue()

        val captor = ArgumentCaptor.forClass(String::class.java)
        captor.run {
            verify(observer, times(1)).onChanged(capture())
            assertTrue(value == "Sending error")
        }
    }

    @Test
    fun `Should show jira issue sent message`() {
        jiraViewModel.onSummaryChanged("summary", 0, 0, 0)
        jiraViewModel.onDescriptionChanged("description", 0, 0, 0)
        val observer: Observer<String> = mock()
        jiraViewModel.issueMessage.observeForever(observer)
        given(
            jiraRepository.sendJiraIssue(
                any(),
                any(),
                any(),
                any()
            )
        ) willAnswer {
            jiraViewModel.handleSendResponse(true)
        }
        whenever(
            resourceManager.jiraIssueSentMessage()
        ) doReturn "Sent"
        jiraViewModel.sendIssue()

        val captor = ArgumentCaptor.forClass(String::class.java)
        captor.run {
            verify(observer, times(1)).onChanged(capture())
            assertTrue(value == "Sent")
        }
        verify(jiraRepository, times(1)).getJiraIssues(anyOrNull(), anyOrNull())
    }

    @Test
    fun `Should save credentials and open session`() {
        val jiraIssuesResult = JiraIssuesResult(arrayListOf(JiraIssue(0, "key")))
        val observer: Observer<JiraIssuesResult> = mock()
        jiraViewModel.onCheckedChanged(true)
        jiraViewModel.issueListResponse.observeForever(observer)
        given {
            jiraRepository.getJiraIssues(anyOrNull(), anyOrNull())
        } willAnswer { jiraViewModel.handleResponse(jiraIssuesResult) }

        jiraViewModel.getJiraTickets()

        val captor = ArgumentCaptor.forClass(JiraIssuesResult::class.java)
        captor.run {
            verify(observer, times(1)).onChanged(capture())
            assertTrue(value.isSuccess())
        }
        verify(jiraRepository).saveCredentials()
        assertTrue(jiraViewModel.isSessionOpen.get() == true)
    }

    @Test
    fun `Should close session if get issues fails`() {
        val jiraIssuesResult = JiraIssuesResult(errorMessage = "Error")
        val observer: Observer<JiraIssuesResult> = mock()
        jiraViewModel.issueListResponse.observeForever(observer)
        given {
            jiraRepository.getJiraIssues(anyOrNull(), anyOrNull())
        } willAnswer { jiraViewModel.handleResponse(jiraIssuesResult) }
        whenever(jiraRepository.hasJiraSessionSaved()) doReturn true

        jiraViewModel.getJiraTickets()

        val captor = ArgumentCaptor.forClass(JiraIssuesResult::class.java)
        captor.run {
            verify(observer, times(1)).onChanged(capture())
            assertTrue(!value.isSuccess())
        }
        verify(jiraRepository).closeSession()
        assertTrue(jiraViewModel.isSessionOpen.get() == false)
    }
}
