package org.dhis2.usescases.main

import io.reactivex.Single
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import org.hisp.dhis.android.core.user.User

interface HomeRepository {
    fun user(): Single<User?>

    fun defaultCatCombo(): Single<CategoryCombo?>

    fun defaultCatOptCombo(): Single<CategoryOptionCombo?>

    fun hasProgramWithAssignment(): Boolean

    fun checkDeleteBiometricsPermission()

    fun hasHomeAnalytics(): Boolean

    fun getServerVersion(): Single<SystemInfo?>

    fun accountsCount(): Int

    fun isPinStored(): Boolean

    fun homeItemCount(): Int

    suspend fun singleHomeItemData(): HomeItemData?

    suspend fun logOut(): Result<Unit>

    suspend fun clearSessionLock(): Result<Unit>
}
