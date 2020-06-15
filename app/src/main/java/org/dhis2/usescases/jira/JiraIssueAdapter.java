package org.dhis2.usescases.jira;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.databinding.JiraIssueItemBinding;
import org.dhis2.utils.jira.JiraIssue;
import org.dhis2.utils.jira.OnJiraIssueClick;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class JiraIssueAdapter extends RecyclerView.Adapter<JiraIssueHolder> {
    private final OnJiraIssueClick listener;
    private List<JiraIssue> jiraIssueList;

    public JiraIssueAdapter(OnJiraIssueClick listener) {
        jiraIssueList = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public JiraIssueHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        JiraIssueItemBinding binding = JiraIssueItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new JiraIssueHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull JiraIssueHolder holder, int position) {
        holder.bind(jiraIssueList.get(position),listener);
    }

    public void addItems(List<JiraIssue> jiraIssueList) {
        this.jiraIssueList = jiraIssueList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return jiraIssueList.size();
    }
}
