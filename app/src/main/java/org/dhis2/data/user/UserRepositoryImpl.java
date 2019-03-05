package org.dhis2.data.user;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.user.User;
import org.hisp.dhis.android.core.user.UserCredentialsModel;

import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import static org.dhis2.data.database.SqlConstants.ALL;
import static org.dhis2.data.database.SqlConstants.FROM;
import static org.dhis2.data.database.SqlConstants.LIMIT_1;
import static org.dhis2.data.database.SqlConstants.SELECT;
import static org.dhis2.data.database.SqlConstants.USER_TABLE;

public class UserRepositoryImpl implements UserRepository {
    private static final String SELECT_USER = SELECT + ALL + FROM + USER_TABLE + LIMIT_1;
    private static final String SELECT_USER_CREDENTIALS = SELECT + ALL + FROM + UserCredentialsModel.TABLE + LIMIT_1;
    private static final String SELECT_USER_ORG_UNITS = SELECT + ALL + FROM + OrganisationUnitModel.TABLE;

    private final BriteDatabase briteDatabase;

    UserRepositoryImpl(@NonNull BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    @Override
    public Flowable<UserCredentialsModel> credentials() {
        return briteDatabase
                .createQuery(UserCredentialsModel.TABLE, SELECT_USER_CREDENTIALS)
                .mapToOne(UserCredentialsModel::create)
                .take(1).toFlowable(BackpressureStrategy.BUFFER);
    }

    @NonNull
    @Override
    public Flowable<User> me() {
        return briteDatabase
                .createQuery(USER_TABLE, SELECT_USER)
                .mapToOne(User::create).toFlowable(BackpressureStrategy.BUFFER);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> myOrgUnits() {
        return briteDatabase
                .createQuery(OrganisationUnitModel.TABLE, SELECT_USER_ORG_UNITS)
                .mapToList(OrganisationUnitModel::create);
    }
}
