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
import org.dhis2.Bindings.default
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.prefs.PreferenceProviderImpl
import org.dhis2.utils.BiometricStorage
import org.dhis2.utils.Constants
import org.dhis2.utils.jira.IssueRequest
import org.dhis2.utils.jira.JiraIssueListRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * QUADRAM. Created by ppajuelo on 11/04/2019.
 */
class JiraViewModel : ViewModel(), JiraActions {

    private lateinit var issueService: JiraIssueService
    private lateinit var prefs: PreferenceProvider

    fun init(preferenceProvider: PreferenceProviderImpl) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://jira.dhis2.org/")
            .client(OkHttpClient())
            .validateEagerly(true)
            .build()

        issueService = retrofit.create<JiraIssueService>(JiraIssueService::class.java)
        prefs = preferenceProvider

        if (prefs.contains(Constants.JIRA_USER)) {
            getJiraIssues()
        }
    }

    private interface JiraIssueService {
        @POST("rest/api/2/issue")
        fun createIssue(
            @Header("Authorization")
            auth: String,
            @Body issueRequest: RequestBody
        ): Call<ResponseBody>

        @POST("rest/api/2/search")
        fun getJiraIssues(
            @Header("Authorization")
            auth: String?,
            @Body issueRequest: RequestBody
        ): Call<ResponseBody>
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
                    if (rememberCredentials.value!! && !prefs.contains(Constants.JIRA_AUTH)) {
                        BiometricStorage.saveJiraCredentials(getAuth())
                        BiometricStorage.saveJiraUser(userName.value!!)
                    }
                    isSessionOpen.set(true)
                    issueListResponse.value = response
                } else {
                    closeSession()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                closeSession()
            }
        }
    }

    override fun closeSession() {
        BiometricStorage.closeJiraSession()
        session.value = null
        rememberCredentials.value = false
        isSessionOpen.set(false)
    }

    private val session =
        MutableLiveData<String?>().default(prefs.getString(Constants.JIRA_AUTH, null))
    private val isSessionOpen = ObservableField<Boolean>(false)

    private val userName = MutableLiveData<String>()
    private val pass = MutableLiveData<String>()
    private val rememberCredentials =
        MutableLiveData<Boolean>().default(prefs.contains(Constants.JIRA_AUTH))

    private val summary = MutableLiveData<String>()
    private val description = MutableLiveData<String>()
    private val formCompleted = ObservableField<Boolean>(false)

    private val issueListResponse = MutableLiveData<Response<ResponseBody>>()
    private val issueMessage = MutableLiveData<String>().default("")

    fun issueListResponse(): LiveData<Response<ResponseBody>> {
        return issueListResponse
    }

    fun issueMessage(): LiveData<String> {
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

    private fun getIssueCallback(): Callback<ResponseBody>? {
        return object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    issueMessage.value = "Issue Sent"
                    getJiraIssues()
                } else {
                    issueMessage.value = "Error sending issue"
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                issueMessage.value = "Error sending issue"
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
