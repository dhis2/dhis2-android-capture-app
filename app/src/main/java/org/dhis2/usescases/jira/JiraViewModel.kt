package org.dhis2.usescases.jira

import android.util.Base64
import android.widget.CompoundButton
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.adorsys.android.securestoragelibrary.SecurePreferences
import org.dhis2.Bindings.default
import org.dhis2.data.tuples.Quartet
import org.dhis2.utils.BiometricStorage
import org.dhis2.utils.Constants
import org.dhis2.utils.jira.IssueRequest
import org.hisp.dhis.android.core.utils.support.StringUtils.isEmpty

/**
 * QUADRAM. Created by ppajuelo on 11/04/2019.
 */
class JiraViewModel : ViewModel(), JiraActions {

    override fun closeSession() {
        BiometricStorage.closeJiraSession()
        rememberCredentials.value = false
        auth.value = null
    }

    private val userName = MutableLiveData<String>()
    private val pass = MutableLiveData<String>()
    private val description = MutableLiveData<String>()
    private val summary = MutableLiveData<String>()
    private val rememberCredentials = MutableLiveData<Boolean>().default(SecurePreferences.contains(Constants.JIRA_AUTH))
    private val auth = MutableLiveData<String>()
    private val issue = MutableLiveData<Quartet<IssueRequest, String, String, Boolean>>()
    private val formCompleted = ObservableField<Boolean>(false)

    fun formComplete(): ObservableField<Boolean> {
        return formCompleted
    }

    fun issue(): LiveData<Quartet<IssueRequest, String, String, Boolean>> {
        return issue
    }

    fun rememberCredentials(): LiveData<Boolean> {
        return rememberCredentials
    }

    override fun sendIssue() {
        val issuesRequest = IssueRequest(summary.value, description.value)
        issue.value = Quartet.create(issuesRequest, userName.value!!, auth.value!!, rememberCredentials.value!!)
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
        checkFormCompletion()
    }

    override fun onJiraPassChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        pass.value = s.toString()
        checkFormCompletion()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        rememberCredentials.value = isChecked
    }

    private fun checkFormCompletion() {
        val userPassCheck = !isEmpty(userName.value) && !isEmpty(pass.value)
        if (userPassCheck) {
            val credentials = String.format("%s:%s", userName.value, pass.value)
            auth.value = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        }
        val check = !isEmpty(auth.value) && !isEmpty(summary.value) && !isEmpty(description.value)

        if (check != formCompleted.get())
            formCompleted.set(check)
    }
}
