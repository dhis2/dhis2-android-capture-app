package org.dhis2.utils

import de.adorsys.android.securestoragelibrary.SecurePreferences

class BiometricStorage {
    companion object {
        fun saveUserCredentials(serverUrl: String, userName: String, pass: String) {
            SecurePreferences.setValue(Constants.SECURE_CREDENTIALS, true)
            SecurePreferences.setValue(Constants.SECURE_SERVER_URL, serverUrl)
            SecurePreferences.setValue(Constants.SECURE_USER_NAME, userName)
            SecurePreferences.setValue(Constants.SECURE_PASS, pass)

        }

        fun areCredentialsSet(): Boolean {
            return SecurePreferences.getBooleanValue(Constants.SECURE_CREDENTIALS, false)
        }

        fun areSameCredentials(serverUrl: String, userName: String, pass: String): Boolean {
            return SecurePreferences.getStringValue(Constants.SECURE_SERVER_URL, "") == serverUrl &&
                    SecurePreferences.getStringValue(Constants.SECURE_USER_NAME, "") == userName &&
                    SecurePreferences.getStringValue(Constants.SECURE_PASS, "") == pass

        }

        fun saveJiraCredentials(jiraAuth: String): String {
            SecurePreferences.setValue(Constants.JIRA_AUTH, jiraAuth)
            return String.format("Basic %s", jiraAuth)
        }

        fun saveJiraUser(jiraUser: String) {
            SecurePreferences.setValue(Constants.JIRA_USER, jiraUser)
        }

        fun getJiraCredentials(): String? {
            val jiraAuth = SecurePreferences.getStringValue(Constants.JIRA_AUTH, null)
            return if (!jiraAuth.isNullOrEmpty()) String.format("Basic %s", jiraAuth) else null
        }

        fun closeJiraSession() {
            SecurePreferences.removeValue(Constants.JIRA_AUTH)
        }
    }
}