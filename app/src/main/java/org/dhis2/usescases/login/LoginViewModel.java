package org.dhis2.usescases.login;

import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.Constants;
import org.dhis2.utils.TestingCredential;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 20/03/2019.
 */
public class LoginViewModel extends ViewModel {

    private MutableLiveData<String> serverUrl = new MutableLiveData<>();
    private MutableLiveData<String> userName = new MutableLiveData<>();
    private MutableLiveData<String> password = new MutableLiveData<>();
    private MutableLiveData<Boolean> isDataComplete = new MutableLiveData<>();
    private MutableLiveData<Trio<String, String, String>> isTestingEnvironment = new MutableLiveData<>();
    private Map<String, TestingCredential> testingCredentials;

    public LiveData<Boolean> isDataComplete() {
        return isDataComplete;
    }

    public LiveData<Trio<String, String, String>> isTestingEnvironment() {
        return isTestingEnvironment;
    }

    public void onServerChanged(CharSequence serverUrl, int start, int before, int count) {
        if (!serverUrl.toString().equals(this.serverUrl.getValue())) {
            this.serverUrl.setValue(serverUrl.toString());
            checkData();
            if (this.serverUrl.getValue() != null)
                checkTestingEnvironment(this.serverUrl.getValue());
        }
    }

    public void onUserChanged(CharSequence userName, int start, int before, int count) {
        if (!userName.toString().equals(this.userName.getValue())) {

            this.userName.setValue(userName.toString());
            checkData();
        }
    }

    public void onPassChanged(CharSequence password, int start, int before, int count) {
        if (!password.toString().equals(this.password.getValue())) {
            this.password.setValue(password.toString());
            checkData();
        }
    }

    private void checkData() {
        boolean newValue = !isEmpty(serverUrl.getValue()) && !isEmpty(userName.getValue()) && !isEmpty(password.getValue());
        if (isDataComplete.getValue() == null || isDataComplete.getValue() != newValue)
            isDataComplete.setValue(newValue);
    }


    private void checkTestingEnvironment(String serverUrl) {
        switch (serverUrl) {
            case Constants.URL_TEST_229:
            case Constants.URL_TEST_230:
                isTestingEnvironment.setValue(Trio.create(serverUrl, "android", "Android123"));
                break;
            default:
                if (testingCredentials.containsKey(serverUrl) && testingCredentials.get(serverUrl) != null)
                    isTestingEnvironment.setValue(
                            Trio.create(serverUrl,
                                    testingCredentials.get(serverUrl).getUser_name(),
                                    testingCredentials.get(serverUrl).getUser_pass()));
                break;
        }
    }

    public void setTestingCredentials(List<TestingCredential> testingCredentials) {
        this.testingCredentials = new HashMap<>();
        for (TestingCredential testingCredential : testingCredentials) {
            this.testingCredentials.put(testingCredential.getServer_url(), testingCredential);
        }
    }
}
