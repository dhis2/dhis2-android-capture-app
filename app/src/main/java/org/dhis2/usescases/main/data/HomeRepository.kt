package org.dhis2.usescases.main.data

import org.dhis2.usescases.main.HomeItemData
import org.hisp.dhis.android.core.user.User
import java.io.File

interface HomeRepository {
    suspend fun user(): User?

    suspend fun logOut()

    suspend fun clearPin()

    suspend fun hasHomeAnalytics(): Boolean

    suspend fun accountsCount(): Int

    suspend fun isPinStored(): Boolean

    suspend fun homeItemCount(): Int

    suspend fun singleHomeItemData(): HomeItemData

    suspend fun clearCache(cache: File): Boolean

    suspend fun clearPreferences()

    suspend fun wipeAll()

    suspend fun deleteCurrentAccount()

    suspend fun setInitialSyncDone()

    suspend fun getInitialSyncDone(): Boolean

    suspend fun isImportedDb(): Boolean
}
