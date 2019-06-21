package org.dhis2.utils

import de.adorsys.android.securestoragelibrary.SecurePreferences

class BiometricStorage {
    companion object {
        fun saveUserCredentials(serverUrl: String?, userName: String?, pass: String?) {
            SecurePreferences.setValue(SECURE_CREDENTIALS, true)
            SecurePreferences.setValue(SECURE_SERVER_URL, serverUrl ?: "")
            SecurePreferences.setValue(SECURE_USER_NAME, userName ?: "")
            SecurePreferences.setValue(SECURE_PASS, pass ?: "")

        }

        fun areCredentialsSet(): Boolean {
            return SecurePreferences.getBooleanValue(SECURE_CREDENTIALS, false)
        }

        fun areSameCredentials(serverUrl: String?, userName: String?, pass: String?): Boolean {
            return SecurePreferences.getStringValue(SECURE_SERVER_URL, "") == serverUrl &&
                    SecurePreferences.getStringValue(SECURE_USER_NAME, "") == userName &&
                    SecurePreferences.getStringValue(SECURE_PASS, "") == pass

        }

        fun saveJiraCredentials(jiraAuth: String): String {
            SecurePreferences.setValue(JIRA_AUTH, jiraAuth)
            return String.format("Basic %s", jiraAuth)
        }

        fun saveJiraUser(jiraUser: String) {
            SecurePreferences.setValue(JIRA_USER, jiraUser)
        }

        fun getJiraCredentials(): String? {
            val jiraAuth = SecurePreferences.getStringValue(JIRA_AUTH, null)
            return if (!jiraAuth.isNullOrEmpty()) String.format("Basic %s", jiraAuth) else null
        }

        fun closeJiraSession() {
            SecurePreferences.removeValue(JIRA_AUTH)
        }
    }
}