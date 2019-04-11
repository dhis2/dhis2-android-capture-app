package org.dhis2.usescases.jira;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.chip.Chip;

import org.dhis2.Components;
import org.dhis2.R;
import org.dhis2.databinding.FragmentJiraBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.utils.BiometricStorage;
import org.dhis2.utils.Constants;
import org.dhis2.utils.NetworkUtils;
import org.dhis2.utils.jira.JiraIssue;
import org.dhis2.utils.jira.JiraIssueListResponse;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import de.adorsys.android.securestoragelibrary.SecurePreferences;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.hisp.dhis.android.core.utils.support.StringUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 24/05/2018.
 */

public class JiraFragment extends FragmentGlobalAbstract {

    @Inject
    JiraPresenter presenter;
    private Context context;
    private JiraViewModel jiraViewModel;
    FragmentJiraBinding binding;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        ((Components) context.getApplicationContext()).userComponent()
                .plus(new JiraModule()).inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_jira, container, false);
        jiraViewModel = ViewModelProviders.of(this).get(JiraViewModel.class);

        jiraViewModel.rememberCredentials().observe(this, sessionActive -> {
            boolean hasActiveSession = sessionActive != null && sessionActive && SecurePreferences.contains(Constants.JIRA_USER);
            binding.setSecuredCredentials(hasActiveSession);
            if (hasActiveSession)
                presenter.getIssues(getJiraIssueListCallback());

        });

        jiraViewModel.issue().observe(this, issue -> {
            binding.setSecuredCredentials(!isEmpty(issue.val2()) && issue.val3());
            presenter.sendIssue(issue, new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NotNull Call<ResponseBody> call, @NotNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        displayMessage(context.getString(R.string.issue_reported));
                        if (issue.val3()) {
                            BiometricStorage.saveJiraCredentials(issue.val2());
                            BiometricStorage.saveJiraUser(issue.val1());
                            binding.setSecuredCredentials(true);
                        }
                    } else {
                        if (response.code() == 403 || response.code() == 401) {
                            displayMessage(context.getString(R.string.jira_credential_error));
                        } else
                            displayMessage(context.getString(R.string.jira_issue_error));
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    displayMessage(context.getString(R.string.jira_issue_error));
                }
            });
        });
        binding.setJiraViewModel(jiraViewModel);
        binding.sendReportButton.setEnabled(NetworkUtils.isOnline(context));

        return binding.getRoot();
    }

    private Callback<JiraIssueListResponse> getJiraIssueListCallback() {

        return new Callback<JiraIssueListResponse>() {
            @Override
            public void onResponse(@NotNull Call<JiraIssueListResponse> call, @NotNull Response<JiraIssueListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    binding.chipContainer.removeAllViews();
                    for (JiraIssue jiraIssue : response.body().getIssues()) {
                        Chip chip = new Chip(context);
                        chip.setText(String.format("%s - %s", jiraIssue.getKey(), jiraIssue.getFields().getStatus().getName()));
                        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
                        chip.setOnClickListener(view -> {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://jira.dhis2.org/browse/" + jiraIssue.getKey()));
                            Bundle bundle = new Bundle();
                            bundle.putString("Authorization", BiometricStorage.getJiraCredentials());
                            browserIntent.putExtra(Browser.EXTRA_HEADERS, bundle);
                            startActivity(browserIntent);
                        });
                        binding.chipContainer.addView(chip);
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<JiraIssueListResponse> call, @NotNull Throwable t) {

            }
        };
    }


}
