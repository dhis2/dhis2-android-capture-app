package org.dhis2.usescases.jira;

import android.databinding.ObservableField;
import android.util.Base64;

import org.dhis2.R;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import com.google.gson.Gson;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 24/05/2018.
 */

public class JiraPresenterImpl implements JiraPresenter {

    private final JiraIssueService issueService;
    private ObservableField<String> description = new ObservableField<>("");
    private ObservableField<String> summary = new ObservableField<>("");
    private ObservableField<String> userName = new ObservableField<>("");
    private ObservableField<String> userPass = new ObservableField<>("");
    private ActivityGlobalAbstract context;

    public JiraPresenterImpl() {


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jira.dhis2.org/")
                .client(new OkHttpClient())
                .validateEagerly(true)
                .build();

        issueService = retrofit.create(JiraIssueService.class);
    }

    @Override
    public void init(ActivityGlobalAbstract context) {
        this.context = context;

    }

    @Override
    public void onSendClick() {

        if (!isEmpty(userName.get()) && !isEmpty(userPass.get()) && !isEmpty(summary.get()) && !isEmpty(description.get())) {

            String credentials = String.format("%s:%s", userName.get(), userPass.get());
            String basic = String.format("Basic %s", Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP));
            IssueRequest issuesRequest = new IssueRequest(summary.get(), description.get());
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), new Gson().toJson(issuesRequest));

            issueService.createIssue(basic, requestBody)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                context.displayMessage(context.getString(R.string.issue_reported));
                            } else {
                                if (response.code() == 403)
                                    context.displayMessage(context.getString(R.string.jira_credential_error));
                                else
                                    context.displayMessage(context.getString(R.string.jira_issue_error));
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            context.displayMessage(context.getString(R.string.jira_issue_error));
                        }
                    });
        } else
            context.displayMessage(context.getString(R.string.jira_fields_error));

    }

    @Override
    public void onSummaryChanged(CharSequence s, int start, int before, int count) {
        summary.set(s.toString());
    }

    @Override
    public void onDescriptionChanged(CharSequence s, int start, int before, int count) {
        description.set(s.toString());
    }

    @Override
    public void onJiraUserChanged(CharSequence s, int start, int before, int count) {
        userName.set(s.toString());
    }

    @Override
    public void onJiraPassChanged(CharSequence s, int start, int before, int count) {
        userPass.set(s.toString());
    }

    private interface JiraIssueService {
        @POST("rest/api/2/issue")
        Call<ResponseBody> createIssue(@Header("Authorization") String auth, @Body RequestBody issueRequest);
    }

    private class IssueRequest {

        private Fields fields;

        IssueRequest(String summary, String description) {
            this.fields = new Fields(summary, description);
        }
    }

    private class IssueResponse {
        private String id;
        private String key;
        private String self;

        public String getId() {
            return id;
        }

        public String getKey() {
            return key;
        }
    }

    private class Fields {
        private Project project;
        private String summary;
        private String description;
        private Issue issuetype;
        private ArrayList<Component> components;

        Fields(String summary, String description) {
            this.summary = summary;
            this.description = description;
            this.project = new Project("10200");
            this.issuetype = new Issue("Bug");
            this.components = new ArrayList<>();
            this.components.add(new Component("AndroidApp"));
        }


    }

    private class Project {
        private String id;

        Project(String projectId) {
            this.id = projectId;
        }
    }

    private class Issue {
        private String name;

        Issue(String bug) {
            this.name = bug;
        }
    }

    private class Component {
        private String name;

        Component(String componentName) {
            name = componentName;
        }
    }
}
