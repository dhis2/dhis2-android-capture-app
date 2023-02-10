package org.dhis2.usescases.main

import io.reactivex.Completable
import io.reactivex.Single
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import org.hisp.dhis.android.core.user.User

interface HomeRepository {
    fun user(): Single<User>
    fun defaultCatCombo(): Single<CategoryCombo>
    fun defaultCatOptCombo(): Single<CategoryOptionCombo>
    fun logOut(): Completable
    fun hasProgramWithAssignment(): Boolean
    fun hasHomeAnalytics(): Boolean
    fun getServerVersion(): Single<SystemInfo>
    fun accountsCount(): Int
    fun isPinStored(): Boolean
}
