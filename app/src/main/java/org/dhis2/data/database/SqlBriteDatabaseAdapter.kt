package org.dhis2.data.database

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement

import com.squareup.sqlbrite2.BriteDatabase

import org.dhis2.utils.Preconditions
import org.hisp.dhis.android.core.data.database.DatabaseAdapter
import org.hisp.dhis.android.core.data.database.Transaction


internal class SqlBriteDatabaseAdapter(private val sqlBriteDatabase: BriteDatabase) : DatabaseAdapter {

    init {
        Preconditions.isNull(sqlBriteDatabase, "briteDatabase == null")
    }

    override fun compileStatement(sql: String): SQLiteStatement {
        return sqlBriteDatabase.writableDatabase.compileStatement(sql)
    }

    override fun query(sql: String, vararg selectionArgs: String): Cursor {
        return sqlBriteDatabase.query(sql, *selectionArgs)
    }

    override fun executeInsert(table: String, sqLiteStatement: SQLiteStatement): Long {
        return sqlBriteDatabase.executeInsert(table, sqLiteStatement)
    }

    override fun executeUpdateDelete(table: String, sqLiteStatement: SQLiteStatement): Int {
        return sqlBriteDatabase.executeUpdateDelete(table, sqLiteStatement)
    }

    override fun delete(table: String, whereClause: String, whereArgs: Array<String>): Int {
        return sqlBriteDatabase.delete(table, whereClause, *whereArgs)
    }

    override fun delete(table: String): Int {
        return sqlBriteDatabase.delete(table, null)
    }

    override fun beginNewTransaction(): Transaction {
        return SqlBriteTransaction(sqlBriteDatabase.newTransaction())
    }

    override fun database(): SQLiteDatabase {
        return sqlBriteDatabase.readableDatabase
    }
}
