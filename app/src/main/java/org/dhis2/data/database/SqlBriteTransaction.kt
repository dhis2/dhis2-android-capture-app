package org.dhis2.data.database


import com.squareup.sqlbrite2.BriteDatabase

import org.hisp.dhis.android.core.data.database.Transaction

class SqlBriteTransaction(private val transaction: BriteDatabase.Transaction) : Transaction {

    override fun begin() {
        // no-op
        // transaction is started in constructor
    }

    override fun setSuccessful() {
        transaction.markSuccessful()
    }

    override fun end() {
        transaction.end()
    }
}
