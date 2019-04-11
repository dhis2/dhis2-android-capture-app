package org.dhis2.usescases.jira;

import org.dhis2.data.tuples.Quartet;
import org.dhis2.utils.jira.IssueRequest;
import org.dhis2.utils.jira.JiraIssueListResponse;

import okhttp3.ResponseBody;
import retrofit2.Callback;

/**
 * QUADRAM. Created by ppajuelo on 24/05/2018.
 */

public interface JiraPresenter {

    void sendIssue(Quartet<IssueRequest, String, String, Boolean> issue, Callback<ResponseBody> callback);

    void getIssues(Callback<JiraIssueListResponse> callback);
}
