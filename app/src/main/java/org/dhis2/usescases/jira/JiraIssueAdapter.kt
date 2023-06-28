package org.dhis2.usescases.jira

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import org.dhis2.data.jira.JiraIssue
import org.dhis2.databinding.JiraIssueItemBinding

class JiraIssueAdapter(private val onJiraIssueClick: (String) -> Unit) :
    ListAdapter<JiraIssue, JiraIssueHolder>(
        object : DiffUtil.ItemCallback<JiraIssue>() {
            override fun areItemsTheSame(oldItem: JiraIssue, newItem: JiraIssue): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: JiraIssue, newItem: JiraIssue): Boolean {
                return oldItem == newItem
            }
        }
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JiraIssueHolder {
        return JiraIssueHolder(
            JiraIssueItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onJiraIssueClick
        )
    }

    override fun onBindViewHolder(holder: JiraIssueHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
