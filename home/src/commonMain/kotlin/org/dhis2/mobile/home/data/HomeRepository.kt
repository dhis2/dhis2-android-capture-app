package org.dhis2.mobile.home.data

import org.dhis2.mobile.home.domain.model.HomeItemData
import org.dhis2.mobile.home.domain.model.HomeUser

interface HomeRepository {
    suspend fun user(): HomeUser?

    suspend fun logOut(): Result<Unit>

    suspend fun clearPin(): Result<Unit>

    suspend fun hasHomeAnalytics(): Boolean

    suspend fun accountsCount(): Int

    suspend fun isPinStored(): Boolean

    suspend fun homeItemCount(): Int

    suspend fun singleHomeItemData(): HomeItemData

    suspend fun clearCache(cachePath: String): Boolean

    suspend fun clearPreferences()

    suspend fun wipeAll()

    suspend fun deleteCurrentAccount()

    suspend fun setInitialSyncDone()

    suspend fun getInitialSyncDone(): Boolean

    suspend fun isImportedDb(): Boolean

    suspend fun stopBackgroundSync()

    suspend fun restoreSyncStatus()

    suspend fun serverName(): String
}
