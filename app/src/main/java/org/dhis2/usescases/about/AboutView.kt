package org.dhis2.usescases.about

import org.hisp.dhis.android.core.user.User

interface AboutView {
    fun renderUserCredentials(userModel: User?)
    fun renderServerUrl(serverUrl: String?)
    fun navigateToPrivacyPolicy()
}
