package org.dhis2.usescases.jira

import androidx.recyclerview.widget.RecyclerView
import org.dhis2.databinding.JiraIssueItemBinding
import org.dhis2.utils.jira.JiraIssue
import org.dhis2.utils.jira.OnJiraIssueClick

class JiraIssueHolder(
    private val binding: JiraIssueItemBinding,
    private val listener: OnJiraIssueClick
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(jiraIssue: JiraIssue) {
        binding.apply {
            issue = jiraIssue
            jiraCard.setOnClickListener { listener.onJiraIssueClick(jiraIssue.key) }
        }
    }
}