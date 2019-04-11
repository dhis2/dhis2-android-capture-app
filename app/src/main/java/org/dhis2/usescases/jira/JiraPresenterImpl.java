package org.dhis2.usescases.jira;

import com.google.gson.Gson;

import org.dhis2.data.tuples.Quartet;
import org.dhis2.utils.BiometricStorage;
import org.dhis2.utils.Constants;
import org.dhis2.utils.jira.IssueRequest;
import org.dhis2.utils.jira.JiraIssueListRequest;
import org.dhis2.utils.jira.JiraIssueListResponse;
import org.hisp.dhis.android.core.constant.Constant;

import de.adorsys.android.securestoragelibrary.SecurePreferences;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * QUADRAM. Created by ppajuelo on 24/05/2018.
 */

public class JiraPresenterImpl implements JiraPresenter {

    private final JiraIssueService issueService;

    JiraPresenterImpl() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jira.dhis2.org/")
                .client(new OkHttpClient())
                .validateEagerly(true)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        issueService = retrofit.create(JiraIssueService.class);
    }

    @Override
    public void sendIssue(Quartet<IssueRequest, String, String, Boolean> issue, Callback<ResponseBody> callback) {
        IssueRequest issueRequest = issue.val0();
        String auth = issue.val2();
        String basic = String.format("Basic %s", auth);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(issueRequest));
        issueService.createIssue(basic, requestBody)
                .enqueue(callback);
    }

    @Override
    public void getIssues(Callback<JiraIssueListResponse> callback) {
        JiraIssueListRequest request = new JiraIssueListRequest(SecurePreferences.getStringValue(Constants.JIRA_USER,""),5);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(request));
        issueService.getJiraIssues(BiometricStorage.getJiraCredentials(),requestBody).enqueue(callback);
    }

    private interface JiraIssueService {
        @POST("rest/api/2/issue")
        Call<ResponseBody> createIssue(@Header("Authorization") String auth, @Body RequestBody issueRequest);

        //@GET("rest/api/2/search?jql=%20(project=10200%20AND%20reporter=%s)+order+by+updated&maxResults=5")
        @POST("rest/api/2/search")
        Call<JiraIssueListResponse> getJiraIssues(@Header("Authorization") String auth, @Body RequestBody issueRequest);
    }

}
