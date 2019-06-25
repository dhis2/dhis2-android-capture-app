package org.dhis2.data.database

import android.content.Context

import com.squareup.sqlbrite2.BriteDatabase
import com.squareup.sqlbrite2.SqlBrite

import org.dhis2.data.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.data.database.DatabaseAdapter
import org.hisp.dhis.android.core.data.database.DbOpenHelper

import javax.inject.Singleton

import dagger.Module
import dagger.Provides

@Module
class DbModule(private val databaseName: String?) {

    @Provides
    @Singleton
    internal fun sqlBriteLogger(): SqlBrite.Logger {
        return SqlBriteLogger()
    }

    @Provides
    @Singleton
    internal fun sqlBrite(logger: SqlBrite.Logger): SqlBrite {
        return SqlBrite.Builder()
                .logger(logger)
                .build()
    }

    @Provides
    @Singleton
    internal fun databaseOpenHelper(context: Context): DbOpenHelper {
        return DbOpenHelper(context, databaseName)
    }

    @Provides
    @Singleton
    internal fun briteDatabase(dbOpenHelper: DbOpenHelper,
                               sqlBrite: SqlBrite, schedulerProvider: SchedulerProvider): BriteDatabase {
//        briteDatabase.setLoggingEnabled(true);
        return sqlBrite.wrapDatabaseHelper(dbOpenHelper, schedulerProvider.io())
    }

    @Provides
    @Singleton
    internal fun databaseAdapter(briteDatabase: BriteDatabase): DatabaseAdapter {
        return SqlBriteDatabaseAdapter(briteDatabase)
    }
}
