package org.dhis2.usescases.login

import android.text.TextUtils.isEmpty
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.dhis2.data.tuples.Trio
import org.dhis2.utils.TestingCredential

/**
 * QUADRAM. Created by ppajuelo on 20/03/2019.
 */
class LoginViewModel : ViewModel() {

    val serverUrl = MutableLiveData<String>()
    val userName = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val isDataComplete = MutableLiveData<Boolean>()
    val isTestingEnvironment = MutableLiveData<Trio<String, String, String>>()
    var testingCredentials: MutableMap<String, TestingCredential>? = null

    fun isDataComplete(): LiveData<Boolean> {
        return isDataComplete
    }

    fun isTestingEnvironment(): LiveData<Trio<String, String, String>> {
        return isTestingEnvironment
    }

    fun onServerChanged(serverUrl: CharSequence, start: Int, before: Int, count: Int) {
        if (serverUrl.toString() != this.serverUrl.value) {
            this.serverUrl.value = serverUrl.toString()
            checkData()
            if (this.serverUrl.value != null) {
                checkTestingEnvironment(this.serverUrl.value!!)
            }
        }
    }

    fun onUserChanged(userName: CharSequence, start: Int, before: Int, count: Int) {
        if (userName.toString() != this.userName.value) {
            this.userName.value = userName.toString()
            checkData()
        }
    }

    fun onPassChanged(password: CharSequence, start: Int, before: Int, count: Int) {
        if (password.toString() != this.password.value) {
            this.password.value = password.toString()
            checkData()
        }
    }

    private fun checkData() {
        val newValue =
            !isEmpty(serverUrl.value) && !isEmpty(userName.value) && !isEmpty(password.value)
        if (isDataComplete.value == null || isDataComplete.value != newValue) {
            isDataComplete.value = newValue
        }
    }

    private fun checkTestingEnvironment(serverUrl: String) {
        if (testingCredentials!!.containsKey(serverUrl) &&
            testingCredentials!![serverUrl] != null
        ) {
            isTestingEnvironment.value = Trio.create(
                serverUrl,
                testingCredentials!![serverUrl]!!.user_name,
                testingCredentials!![serverUrl]!!.user_pass
            )
        }
    }

    fun setTestingCredentials(testingCredentials: List<TestingCredential>) {
        this.testingCredentials = HashMap()
        for (testingCredential in testingCredentials) {
            this.testingCredentials!![testingCredential.server_url] = testingCredential
        }
    }
}
