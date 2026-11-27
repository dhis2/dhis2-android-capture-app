package org.dhis2.mobile.login.authentication.domain.repository

interface SecretClipboard {
    fun copy(text: String)

    fun clearIfOwned()
}
