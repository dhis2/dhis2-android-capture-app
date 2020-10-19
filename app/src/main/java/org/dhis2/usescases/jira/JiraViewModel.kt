package org.dhis2.usescases.jira

import android.text.TextUtils.isEmpty
import android.util.Base64
import android.widget.CompoundButton
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.prefs.PreferenceProviderImpl
import org.dhis2.utils.Constants
import org.dhis2.utils.jira.IssueRequest
import org.dhis2.utils.jira.JiraIssue
import org.dhis2.utils.jira.JiraIssueListRequest
import org.dhis2.utils.jira.JiraIssueListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.util.ArrayList

class JiraViewModel : ViewModel(), JiraActions {

    private lateinit var issueService: JiraIssueService
    private lateinit var prefs: PreferenceProvider

    private var session: MutableLiveData<String?> = MutableLiveData()
    private val isSessionOpen = ObservableField<Boolean>(false)

    private val userName = MutableLiveData<String>()
    private val pass = MutableLiveData<String>()
    private var rememberCredentials: MutableLiveData<Boolean> = MutableLiveData()

    private val summary = MutableLiveData<String>()
    private val description = MutableLiveData<String>()
    private val formCompleted = ObservableField<Boolean>(false)

    private val issueListResponse = MutableLiveData<Result<List<JiraIssue>>>()
    private var issueMessage: MutableLiveData<Result<String>> = MutableLiveData()

    fun init(preferenceProvider: PreferenceProviderImpl) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://jira.dhis2.org/")
            .client(OkHttpClient())
            .validateEagerly(true)
            .build()

        issueService = retrofit.create<JiraIssueService>(JiraIssueService::class.java)
        prefs = preferenceProvider

        session.value = prefs.getString(Constants.JIRA_AUTH, null)
        rememberCredentials.value = prefs.contains(Constants.JIRA_AUTH)

        if (prefs.contains(Constants.JIRA_USER)) {
            getJiraIssues()
        }
    }

    override fun openSession() {
        if (!isEmpty(userName.value) && !isEmpty(pass.value)) {
            session.value = getAuth()
            getJiraIssues()
        }
    }

    fun getJiraIssues() {
        val basic = String.format("Basic %s", session.value)
        val request = JiraIssueListRequest(prefs.getString(Constants.JIRA_USER, userName.value), 20)
        val requestBody =
            RequestBody.create(MediaType.parse("application/json"), Gson().toJson(request))
        issueService.getJiraIssues(basic, requestBody).enqueue(getJiraIssueListCallback())
    }

    private fun getJiraIssueListCallback(): Callback<ResponseBody> {
        return object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    saveCredentialsIfNeeded()
                    isSessionOpen.set(true)
                    var issueList: List<JiraIssue> = ArrayList()
                    try {
                        val jiraIssueListRes = Gson()
                            .fromJson(
                                response.body()!!.string(),
                                JiraIssueListResponse::class.java
                            )
                        issueList = jiraIssueListRes.issues
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    issueListResponse.value = Result.success(issueList)
                } else {
                    issueListResponse.value = Result.failure(Exception(response.message()))
                    closeSession()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                issueListResponse.value = Result.failure(t)
                closeSession()
            }
        }
    }

    private fun saveCredentialsIfNeeded(){
        if (rememberCredentials.value!! && !prefs.contains(Constants.JIRA_AUTH)) {
            prefs.saveJiraCredentials(getAuth())
            prefs.saveJiraUser(userName.value!!)
        }
    }

    override fun closeSession() {
        prefs.closeJiraSession()
        session.value = null
        rememberCredentials.value = false
        isSessionOpen.set(false)
    }

    fun issueListResponse(): LiveData<Result<List<JiraIssue>>> {
        return issueListResponse
    }

    fun issueMessage(): LiveData<Result<String>> {
        return issueMessage
    }

    fun formComplete(): ObservableField<Boolean> {
        return formCompleted
    }

    fun isSessionOpen(): ObservableField<Boolean> {
        return isSessionOpen
    }

    override fun sendIssue() {
        val issueRequest = IssueRequest(summary.value, description.value)
        val basic = String.format("Basic %s", session.value)
        val requestBody =
            RequestBody.create(MediaType.parse("application/json"), Gson().toJson(issueRequest))
        issueService.createIssue(basic, requestBody).enqueue(getIssueCallback())
    }

    private fun getIssueCallback(): Callback<ResponseBody> {
        return object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    issueMessage.value = Result.success("Issue Sent")
                    getJiraIssues()
                } else {
                    issueMessage.value = Result.failure(Exception("Error sending issue"))
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                issueMessage.value = Result.failure(t)
            }
        }
    }

    override fun onSummaryChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        summary.value = s.toString()
        checkFormCompletion()
    }

    override fun onDescriptionChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        description.value = s.toString()
        checkFormCompletion()
    }

    override fun onJiraUserChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        userName.value = s.toString()
    }

    override fun onJiraPassChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        pass.value = s.toString()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        rememberCredentials.value = isChecked
    }

    private fun checkFormCompletion() {
        val check =
            !isEmpty(summary.value) && !isEmpty(description.value) && isSessionOpen().get()!!

        formCompleted.set(check)
    }

    fun getAuth(): String {
        return if (isEmpty(session.value)) {
            Base64.encodeToString(
                String.format("%s:%s", userName.value, pass.value).toByteArray(),
                Base64.NO_WRAP
            )
        } else {
            session.value!!
        }
    }
}
