package org.dhis2.mobile.login.authentication.data.repository

import org.dhis2.mobile.login.authentication.domain.repository.SecretClipboard

/**
 * [SecretClipboard] for targets without two-factor authentication settings: nothing is ever copied,
 * so there is nothing to clear.
 */
internal class FakeSecretClipboard : SecretClipboard {
    override fun copy(text: String) = Unit

    override fun clearIfOwned() = Unit
}
