package org.dhis2

import android.content.Context
import java.io.File
import timber.log.Timber
import java.io.FileOutputStream

class DBTestLoader(private val context: Context) {

    fun copyDatabaseFromAssetsIfNeeded() {
        val databasePath = context.applicationInfo?.dataDir + "/databases"
        val file = File("$databasePath/$DB_NAME")

        if (file.exists()) {
            Timber.i("Database won't be copy, it already exits")
            return
        }
        val input = context.assets.open("databases/$DB_NAME_TEST")
        val output = FileOutputStream("$databasePath/$DB_NAME")

        input.copyTo(output)
    }

    companion object {
        const val DB_NAME_TEST = "dhis_test.db"
        const val DB_NAME = "127-0-0-1-8080_android_unencrypted.db"
    }
}
