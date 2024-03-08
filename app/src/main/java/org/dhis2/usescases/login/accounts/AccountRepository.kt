package org.dhis2.usescases.login.accounts

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.db.access.DatabaseExportMetadata
import java.io.File

class AccountRepository(val d2: D2) {

    fun getLoggedInAccounts(): List<AccountModel> {
        return d2.userModule().accountManager().getAccounts().map {
            AccountModel(it.username(), it.serverUrl())
        }
    }

    fun importDatabase(file: File): Result<DatabaseExportMetadata> {
        return try {
            val importedMetadata =
                d2.maintenanceModule().databaseImportExport().importDatabase(file)
            Result.success(importedMetadata)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
