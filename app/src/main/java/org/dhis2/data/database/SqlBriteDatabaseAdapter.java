package org.dhis2.data.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.arch.db.access.DatabaseAdapter;
import org.hisp.dhis.android.core.arch.db.access.Transaction;

import static org.dhis2.utils.Preconditions.isNull;

class SqlBriteDatabaseAdapter implements DatabaseAdapter {
    private final BriteDatabase sqlBriteDatabase;

    SqlBriteDatabaseAdapter(@NonNull BriteDatabase briteDatabase) {
        isNull(briteDatabase, "briteDatabase == null");
        sqlBriteDatabase = briteDatabase;
    }

    @Override
    public SQLiteStatement compileStatement(String sql) {
        return sqlBriteDatabase.getWritableDatabase().compileStatement(sql);
    }

    @Override
    public Cursor query(String sql, String... selectionArgs) {
        return sqlBriteDatabase.query(sql, selectionArgs);
    }

    @Override
    public long executeInsert(String table, SQLiteStatement sqLiteStatement) {
        return sqlBriteDatabase.executeInsert(table, sqLiteStatement);
    }

    @Override
    public int executeUpdateDelete(String table, SQLiteStatement sqLiteStatement) {
        return sqlBriteDatabase.executeUpdateDelete(table, sqLiteStatement);
    }

    @Override
    public int delete(String table, String whereClause, String[] whereArgs) {
        return sqlBriteDatabase.delete(table, whereClause, whereArgs);
    }

    @Override
    public int delete(String table) {
        return sqlBriteDatabase.delete(table, null);
    }

    @Override
    public Transaction beginNewTransaction() {
        return new SqlBriteTransaction(sqlBriteDatabase.newTransaction());
    }

    @Override
    public SQLiteDatabase database() {
        return sqlBriteDatabase.getReadableDatabase();
    }
}
