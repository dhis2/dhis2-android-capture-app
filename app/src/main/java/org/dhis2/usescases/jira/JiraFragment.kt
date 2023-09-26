package org.dhis2.usescases.jira

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import org.dhis2.data.jira.ClickedIssueData
import org.dhis2.data.jira.JiraIssuesResult
import org.dhis2.databinding.FragmentJiraBinding
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.main.MainActivity
import org.dhis2.utils.NetworkUtils
import javax.inject.Inject

class JiraFragment : FragmentGlobalAbstract() {
    @Inject
    lateinit var jiraViewModelFactory: JiraViewModelFactory
    private val jiraModel: JiraViewModel by viewModels {
        jiraViewModelFactory
    }
    private val jiraIssueAdapter by lazy {
        JiraIssueAdapter { jiraModel.onJiraIssueClick(it) }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            context.mainComponent.plus(JiraModule()).inject(this)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return FragmentJiraBinding.inflate(inflater, container, false).apply {
            jiraViewModel = jiraModel
            rememberCheck.setOnCheckedChangeListener { _, isChecked ->
                jiraModel.onCheckedChanged(isChecked)
            }
            sendReportButton.isEnabled = NetworkUtils.isOnline(context)
            issueRecycler.apply {
                adapter = jiraIssueAdapter
                addItemDecoration(
                    DividerItemDecoration(
                        context,
                        DividerItemDecoration.VERTICAL,
                    ),
                )
            }
            jiraModel.apply {
                init()
                issueListResponse.observe(viewLifecycleOwner, Observer { handleListResponse(it) })
                issueMessage.observe(viewLifecycleOwner, Observer { handleMessage(it) })
                clickedIssueData.observe(
                    viewLifecycleOwner,
                    Observer { openJiraTicketInBrowser(it) },
                )
            }
        }.root
    }

    private fun handleListResponse(result: JiraIssuesResult) {
        if (result.isSuccess()) {
            jiraIssueAdapter.submitList(result.issues)
        } else {
            displayMessage(result.errorMessage)
        }
    }

    private fun handleMessage(message: String) {
        displayMessage(message)
    }

    private fun openJiraTicketInBrowser(clickedIssueData: ClickedIssueData) {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(clickedIssueData.uriString),
        ).apply {
            val bundle = Bundle().apply {
                putString(clickedIssueData.authHeader(), clickedIssueData.basicAuth())
            }
            putExtra(Browser.EXTRA_HEADERS, bundle)
        }
        startActivity(browserIntent)
    }
}
