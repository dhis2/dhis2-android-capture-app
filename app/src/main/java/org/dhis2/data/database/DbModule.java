package org.dhis2.data.database;

import android.content.Context;
import androidx.annotation.Nullable;

import com.squareup.sqlbrite2.BriteDatabase;
import com.squareup.sqlbrite2.SqlBrite;

import org.dhis2.data.schedulers.SchedulerProvider;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.android.core.data.database.DbOpenHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DbModule {
    private final String databaseName;

    public DbModule(@Nullable String databaseName) {
        this.databaseName = databaseName;
    }

    @Provides
    @Singleton
    SqlBrite.Logger sqlBriteLogger() {
        return new SqlBriteLogger();
    }

    @Provides
    @Singleton
    SqlBrite sqlBrite(SqlBrite.Logger logger) {
        return new SqlBrite.Builder()
                .logger(logger)
                .build();
    }

    @Provides
    @Singleton
    DbOpenHelper databaseOpenHelper(Context context) {
        return new DbOpenHelper(context, databaseName);
    }

    @Provides
    @Singleton
    BriteDatabase briteDatabase(DbOpenHelper dbOpenHelper,
                                SqlBrite sqlBrite, SchedulerProvider schedulerProvider) {
        BriteDatabase briteDatabase = sqlBrite.wrapDatabaseHelper(dbOpenHelper, schedulerProvider.io());
//        briteDatabase.setLoggingEnabled(true);
        return briteDatabase;
    }

    @Provides
    @Singleton
    DatabaseAdapter databaseAdapter(BriteDatabase briteDatabase) {
        return new SqlBriteDatabaseAdapter(briteDatabase);
    }
}
