package org.dhis2.usescases.login

import org.dhis2.data.server.UserManager
import org.dhis2.usescases.sync.WAS_INITIAL_SYNC_DONE

class SyncIsPerformedInteractor(
    private val userManager: UserManager?,
) {
    fun execute(): Boolean {
        if (userManager == null) return false

        val entryExists =
            userManager.d2
                .dataStoreModule()
                .localDataStore()
                .value(
                    WAS_INITIAL_SYNC_DONE,
                ).blockingExists()

        val serverUrl =
            userManager.d2
                .systemInfoModule()
                .systemInfo()
                .blockingGet()
                ?.contextPath()
        val username =
            userManager.d2
                .userModule()
                .user()
                .blockingGet()
                ?.username()

        val dataBaseIsImport =
            userManager.d2
                .userModule()
                .accountManager()
                .getCurrentAccount()
                ?.let {
                    it.serverUrl() == serverUrl && it.username() == username && it.importDB() != null
                } ?: false

        return when {
            dataBaseIsImport -> true
            entryExists -> {
                val entry =
                    userManager.d2
                        .dataStoreModule()
                        .localDataStore()
                        .value(
                            WAS_INITIAL_SYNC_DONE,
                        ).blockingGet()
                !entry?.value().isNullOrEmpty() && entry?.value() == "True"
            }

            else -> false
        }
    }
}
