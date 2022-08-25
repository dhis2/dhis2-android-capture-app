package org.dhis2.android.rtsm.services

import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig

interface OpenIdProvider {
    fun loadProvider(): OpenIDConnectConfig?
}