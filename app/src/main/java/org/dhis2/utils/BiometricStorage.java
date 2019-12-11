package org.dhis2.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import de.adorsys.android.securestoragelibrary.SecurePreferences;
import de.adorsys.android.securestoragelibrary.SecureStorageException;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 24/01/2019.
 */
public class BiometricStorage {

    public static void saveUserCredentials(String serverUrl, String userName, String pass, Context context) throws SecureStorageException {
        SecurePreferences.setValue(context, Constants.SECURE_CREDENTIALS, true);
        SecurePreferences.setValue(context, Constants.SECURE_SERVER_URL, serverUrl);
        SecurePreferences.setValue(context, Constants.SECURE_USER_NAME, userName);
        SecurePreferences.setValue(context, Constants.SECURE_PASS, pass);
    }

    public static Boolean areCredentialsSet(Context context) {
       return SecurePreferences.getBooleanValue(context, Constants.SECURE_CREDENTIALS, false);
    }

    public static Boolean areSameCredentials(String serverUrl, String userName, String pass, Context context) {
        return SecurePreferences.getStringValue(context, Constants.SECURE_SERVER_URL, "").equals(serverUrl) &&
                SecurePreferences.getStringValue(context, Constants.SECURE_USER_NAME, "").equals(userName) &&
                SecurePreferences.getStringValue(context, Constants.SECURE_PASS, "").equals(pass);
    }

    @NonNull
    public static String saveJiraCredentials(@NonNull String jiraAuth) {
//        SecurePreferences.setValue(Constants.JIRA_AUTH, jiraAuth);
        return String.format("Basic %s", jiraAuth);
    }

    @NonNull
    public static void saveJiraUser(@NonNull String jiraUser) {
//        SecurePreferences.setValue(Constants.JIRA_USER, jiraUser);
    }

    @Nullable
    public static String getJiraCredentials() {
//        String jiraAuth = SecurePreferences.getStringValue(Constants.JIRA_AUTH, null);
//        return !isEmpty(jiraAuth) ? String.format("Basic %s", jiraAuth) : null;
        return "";
    }

    public static void closeJiraSession() {
//        SecurePreferences.removeValue(Constants.JIRA_AUTH);
    }
}
