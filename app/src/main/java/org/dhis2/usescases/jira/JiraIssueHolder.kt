package org.dhis2.usescases.jira

import androidx.recyclerview.widget.RecyclerView
import org.dhis2.data.jira.JiraIssue
import org.dhis2.databinding.JiraIssueItemBinding

class JiraIssueHolder(
    private val binding: JiraIssueItemBinding,
    private val onJiraIssueClick: (String) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(jiraIssue: JiraIssue) {
        binding.apply {
            issue = jiraIssue
            jiraCard.setOnClickListener { onJiraIssueClick(jiraIssue.key) }
        }
    }
}
