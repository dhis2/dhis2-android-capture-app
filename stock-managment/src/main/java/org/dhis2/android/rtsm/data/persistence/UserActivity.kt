package org.dhis2.android.rtsm.data.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.dhis2.android.rtsm.data.TransactionType
import java.time.LocalDateTime

@Entity(tableName = "user_activities")
data class UserActivity(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name="transaction_type") val type: TransactionType,
    @ColumnInfo(name="transaction_date") val date: LocalDateTime,
    @ColumnInfo(name="distributed_to") var distributedTo: String?
) {
    constructor(type: TransactionType, date: LocalDateTime, distributedTo: String?):
            this(0, type, date, distributedTo)

    constructor(type: TransactionType, date: LocalDateTime):
            this(0, type, date, null)
}
