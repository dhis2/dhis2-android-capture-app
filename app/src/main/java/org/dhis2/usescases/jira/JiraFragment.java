package org.dhis2.usescases.jira;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.gson.Gson;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.prefs.PreferenceProviderImpl;
import org.dhis2.databinding.FragmentJiraBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.NetworkUtils;
import org.dhis2.utils.jira.JiraIssue;
import org.dhis2.utils.jira.JiraIssueListResponse;
import org.dhis2.utils.jira.OnJiraIssueClick;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 24/05/2018.
 */

public class JiraFragment extends FragmentGlobalAbstract implements OnJiraIssueClick {

    private Context context;
    private JiraViewModel jiraViewModel;
    private JiraIssueAdapter adapter = new JiraIssueAdapter(this);

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentJiraBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_jira, container, false);
        jiraViewModel = ViewModelProviders.of(this).get(JiraViewModel.class);
        jiraViewModel.init(new PreferenceProviderImpl(context.getApplicationContext()));

        jiraViewModel.issueListResponse().observe(this, response -> {
            if (response.isSuccessful() && response.body() != null) {
                List<JiraIssue> issueList = new ArrayList<>();
                try {
                    JiraIssueListResponse jiraIssueListRes = new Gson().fromJson(response.body().string(), JiraIssueListResponse.class);
                    issueList = jiraIssueListRes.getIssues();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                adapter.addItems(issueList);
            } else
                displayMessage(response.message());
        });

        jiraViewModel.issueMessage().observe(this, message -> {
            if (!isEmpty(message))
                displayMessage(message);
        });

        binding.setJiraViewModel(jiraViewModel);
        binding.sendReportButton.setEnabled(NetworkUtils.isOnline(context));
        binding.issueRecycler.setAdapter(adapter);
        binding.issueRecycler.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        return binding.getRoot();
    }

    @Override
    public void onJiraIssueClick(String issueKey) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://jira.dhis2.org/browse/" + issueKey));
        Bundle bundle = new Bundle();
        bundle.putString("Authorization", String.format("Basic %s", jiraViewModel.getAuth()));
        browserIntent.putExtra(Browser.EXTRA_HEADERS, bundle);
        startActivity(browserIntent);
    }


}
