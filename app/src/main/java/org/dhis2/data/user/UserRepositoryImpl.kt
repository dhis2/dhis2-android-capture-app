package org.dhis2.data.user

import com.squareup.sqlbrite2.BriteDatabase
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.user.UserCredentialsModel
import org.hisp.dhis.android.core.user.UserModel

class UserRepositoryImpl(
        val briteDatabase: BriteDatabase,
        val d2: D2
): UserRepository {

    private val SELECT_USER = "SELECT * FROM " +
            UserModel.TABLE + " LIMIT 1"
    private val SELECT_USER_CREDENTIALS = "SELECT * FROM " +
            UserCredentialsModel.TABLE + " LIMIT 1"

    override fun credentials(): Flowable<UserCredentialsModel> {
        return briteDatabase.createQuery(UserCredentialsModel.TABLE, SELECT_USER_CREDENTIALS)
                .mapToOne(UserCredentialsModel::create)
                .take(1).toFlowable(BackpressureStrategy.BUFFER)
    }

    override fun me(): Flowable<UserModel> {
        return briteDatabase
                .createQuery(UserModel.TABLE, SELECT_USER)
                .mapToOne(UserModel::create).toFlowable(BackpressureStrategy.BUFFER)
    }

}