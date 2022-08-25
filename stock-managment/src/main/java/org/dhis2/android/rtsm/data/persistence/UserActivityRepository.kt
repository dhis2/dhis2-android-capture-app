package org.dhis2.android.rtsm.data.persistence

import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class UserActivityRepository @Inject constructor(private val userActivityDao: UserActivityDao) {
    fun addActivity(activity: UserActivity): Completable {
        return userActivityDao.insertActivity(activity)
    }

    fun getRecentActivities(num: Int): Single<List<UserActivity>> {
        return userActivityDao.getRecentActivities(num)
    }
}