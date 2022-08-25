package org.dhis2.android.rtsm.data.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface UserActivityDao {
    @Query("SELECT * FROM user_activities ORDER BY transaction_date DESC LIMIT :count")
    fun getRecentActivities(count: Int): Single<List<UserActivity>>

    @Insert(onConflict = REPLACE)
    fun insertActivity(activity: UserActivity): Completable
}