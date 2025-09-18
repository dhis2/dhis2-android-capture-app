package org.dhis2.mobile.commons.network

import kotlinx.coroutines.flow.Flow

interface NetworkStatusProvider {
    val connectionStatus: Flow<Boolean>
}
