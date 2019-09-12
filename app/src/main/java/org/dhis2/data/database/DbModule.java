package org.dhis2.data.database;

import android.content.Context;

import androidx.annotation.Nullable;

import com.squareup.sqlbrite2.BriteDatabase;
import com.squareup.sqlbrite2.SqlBrite;

import org.dhis2.data.dagger.PerServer;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.hisp.dhis.android.core.arch.db.access.DbOpenHelper;

import dagger.Module;
import dagger.Provides;

@PerServer
@Module
public class DbModule {
    private final String databaseName;

    public DbModule(@Nullable String databaseName) {
        this.databaseName = databaseName;
    }

    @Provides
    @PerServer
    SqlBrite.Logger sqlBriteLogger() {
        return new SqlBriteLogger();
    }

    @Provides
    @PerServer
    SqlBrite sqlBrite(SqlBrite.Logger logger) {
        return new SqlBrite.Builder()
                .logger(logger)
                .build();
    }

    @Provides
    @PerServer
    DbOpenHelper databaseOpenHelper(Context context) {
        return new DbOpenHelper(context, databaseName);
    }

    @Provides
    @PerServer
    BriteDatabase briteDatabase(DbOpenHelper dbOpenHelper,
                                SqlBrite sqlBrite, SchedulerProvider schedulerProvider) {
        return sqlBrite.wrapDatabaseHelper(dbOpenHelper, schedulerProvider.io());
    }
}
