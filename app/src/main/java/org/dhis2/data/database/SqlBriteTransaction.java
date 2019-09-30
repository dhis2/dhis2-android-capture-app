package org.dhis2.data.database;


import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.arch.db.access.Transaction;

public class SqlBriteTransaction implements Transaction {

    private final BriteDatabase.Transaction transaction;

    public SqlBriteTransaction(BriteDatabase.Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public void begin() {
        // no-op
        // transaction is started in constructor
    }

    @Override
    public void setSuccessful() {
        transaction.markSuccessful();
    }

    @Override
    public void end() {
        transaction.end();
    }
}
