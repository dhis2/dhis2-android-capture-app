package org.dhis2

import android.content.Context
import android.util.Log
import timber.log.Timber
import java.io.*

class DBTestLoader(private val context: Context) {

     fun populateDatabaseFromAssetsIfNeeded() {
        val databasePath = context?.applicationInfo?.dataDir + "/databases"
        val file = File("$databasePath/${DB_NAME}")

        if (file.exists()){
            Log.i("TEST","DB ALREADY LOADED")
            return
        }
        try {
            val input = context!!.assets.open("databases/${DB_NAME}")
            val output = FileOutputStream("$databasePath/${DB_NAME}")

            writeExtractedFileToDisk(input, output)
        } catch (e: IOException) {
            Timber.e(Throwable("Could not load testing database"))
        }
    }

    @Throws(IOException::class)
    fun writeExtractedFileToDisk(input: InputStream, outs: OutputStream) {
        val buffer = ByteArray(1024)
        var length: Int

        length = input.read(buffer)
        while (length > 0) {
            outs.write(buffer, 0, length)
            length = input.read(buffer)
        }

        outs.flush()
        outs.close()
        input.close()
    }

    companion object {
        const val DB_NAME = "dhis.db"
    }
}