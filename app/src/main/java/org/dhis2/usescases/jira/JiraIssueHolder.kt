package org.dhis2.usescases.jira

import org.dhis2.databinding.JiraIssueItemBinding
import org.dhis2.utils.jira.JiraIssue
import org.dhis2.utils.jira.OnJiraIssueClick
import androidx.recyclerview.widget.RecyclerView

class JiraIssueHolder(private val binding: JiraIssueItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(jiraIssue: JiraIssue, listener: OnJiraIssueClick) {
        binding.issue = jiraIssue
        itemView.setOnClickListener { view -> listener.onJiraIssueClick(jiraIssue.key!!) }
    }
}
