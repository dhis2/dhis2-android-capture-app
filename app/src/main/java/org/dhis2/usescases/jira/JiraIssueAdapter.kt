package org.dhis2.usescases.jira

import android.view.LayoutInflater
import android.view.ViewGroup

import org.dhis2.databinding.JiraIssueItemBinding
import org.dhis2.utils.jira.JiraIssue
import org.dhis2.utils.jira.OnJiraIssueClick

import java.util.ArrayList
import androidx.recyclerview.widget.RecyclerView

class JiraIssueAdapter(private val listener: OnJiraIssueClick) : RecyclerView.Adapter<JiraIssueHolder>() {
    private var jiraIssueList: List<JiraIssue>? = null

    init {
        jiraIssueList = ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JiraIssueHolder {
        val binding = JiraIssueItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JiraIssueHolder(binding)
    }

    override fun onBindViewHolder(holder: JiraIssueHolder, position: Int) {
        holder.bind(jiraIssueList!![position], listener)
    }

    fun addItems(jiraIssueList: List<JiraIssue>) {
        this.jiraIssueList = jiraIssueList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return jiraIssueList!!.size
    }
}
