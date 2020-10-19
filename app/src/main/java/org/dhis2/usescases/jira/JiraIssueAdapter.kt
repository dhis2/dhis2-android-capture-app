package org.dhis2.usescases.jira

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import org.dhis2.databinding.JiraIssueItemBinding
import org.dhis2.utils.jira.JiraIssue
import org.dhis2.utils.jira.OnJiraIssueClick

class JiraIssueAdapter(val listener: OnJiraIssueClick) : ListAdapter<JiraIssue, JiraIssueHolder>(
    object : DiffUtil.ItemCallback<JiraIssue>() {
        override fun areItemsTheSame(oldItem: JiraIssue, newItem: JiraIssue): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: JiraIssue, newItem: JiraIssue): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): JiraIssueHolder {
        return JiraIssueHolder(
            JiraIssueItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: JiraIssueHolder, position: Int) {
        holder.bind(getItem(position))
    }
}