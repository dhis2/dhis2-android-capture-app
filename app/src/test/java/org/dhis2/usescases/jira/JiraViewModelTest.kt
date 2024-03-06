package org.dhis2.usescases.jira

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.jira.JiraIssue
import org.dhis2.data.jira.JiraIssueListResponse
import org.dhis2.data.jira.JiraIssuesResult
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class JiraViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var jiraViewModel: JiraViewModel
    private val resourceManager: ResourceManager = mock()
    private val jiraRepository: JiraRepository = mock()
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()

    @Before
    fun setUp() {
        jiraViewModel = JiraViewModel(jiraRepository, resourceManager, schedulers)
    }

    @Test
    fun `Should get jira tickets if user is set`() {
        whenever(jiraRepository.hasJiraSessionSaved()) doReturn true
        whenever(
            jiraRepository.getJiraIssues(anyOrNull())
        ) doReturn Single.just(JiraIssueListResponse(0, 0, emptyList()))
        jiraViewModel.init()
        verify(jiraRepository, times(1)).getJiraIssues(anyOrNull())
    }

    @Test
    fun `Should not get jira tickets if username is null or empty`() {
        jiraViewModel.onJiraPassChanged("pass", 0, 0, 0)
        jiraViewModel.openSession()
        verify(jiraRepository, times(0)).getJiraIssues(anyOrNull())
    }

    @Test
    fun `Should not get jira tickets if pass is null or empty`() {
        jiraViewModel.onJiraUserChanged("userName", 0, 0, 0)
        jiraViewModel.openSession()
        verify(jiraRepository, times(0)).getJiraIssues(anyOrNull())
    }

    @Test
    fun `Should open session if both pass and username are not null or empty`() {
        jiraViewModel.onJiraPassChanged("pass", 0, 0, 0)
        jiraViewModel.onJiraUserChanged("userName", 0, 0, 0)
        whenever(
            jiraRepository.getJiraIssues(anyOrNull())
        ) doReturn Single.just(JiraIssueListResponse(0, 0, emptyList()))
        jiraViewModel.openSession()
        verify(jiraRepository, times(1)).setAuth("userName", "pass")
        verify(jiraRepository, times(1)).getJiraIssues(anyOrNull())
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
        whenever(
            jiraRepository.sendJiraIssue(any(), any())
        ) doReturn Single.error(Throwable("This is an error"))
        jiraViewModel.sendIssue()
        val captor = ArgumentCaptor.forClass(String::class.java)
        captor.run {
            verify(observer, times(1)).onChanged(capture())
            assertTrue(value == "This is an error")
        }
    }

    @Test
    fun `Should show jira issue sent message`() {
        jiraViewModel.onSummaryChanged("summary", 0, 0, 0)
        jiraViewModel.onDescriptionChanged("description", 0, 0, 0)
        val observer: Observer<String> = mock()
        jiraViewModel.issueMessage.observeForever(observer)
        whenever(
            jiraRepository.sendJiraIssue(any(), any())
        ) doReturn Single.just(ResponseBody.create(MediaType.parse("*/*"), "{}"))
        whenever(
            resourceManager.jiraIssueSentMessage()
        ) doReturn "Sent"
        whenever(
            jiraRepository.getJiraIssues(anyOrNull())
        ) doReturn Single.just(JiraIssueListResponse(0, 0, emptyList()))
        jiraViewModel.sendIssue()

        val captor = ArgumentCaptor.forClass(String::class.java)
        captor.run {
            verify(observer, times(1)).onChanged(capture())
            assertTrue(value == "Sent")
        }
        verify(jiraRepository, times(1)).getJiraIssues(anyOrNull())
    }

    @Test
    fun `Should save credentials and open session`() {
        val jiraIssuesResult = JiraIssueListResponse(0, 0, arrayListOf(JiraIssue(0, "key")))
        val observer: Observer<JiraIssuesResult> = mock()
        jiraViewModel.onCheckedChanged(true)
        jiraViewModel.issueListResponse.observeForever(observer)
        whenever(
            jiraRepository.getJiraIssues(anyOrNull())
        ) doReturn Single.just(jiraIssuesResult)
        whenever(
            jiraRepository.getJiraIssues(anyOrNull())
        ) doReturn Single.just(JiraIssueListResponse(0, 0, emptyList()))

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
        val observer: Observer<JiraIssuesResult> = mock()
        jiraViewModel.issueListResponse.observeForever(observer)
        whenever(
            jiraRepository.getJiraIssues(anyOrNull())
        ) doReturn Single.error(Throwable("Error"))
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
