package org.dhis2.utils.database

import android.content.Context
import android.os.Environment
import org.dhis2.extensions.decrypt
import org.dhis2.extensions.encrypt
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream


class DatabaseUtils(private val context: Context, private var password: String = "PBKDF2WithHmacSHA1") {


    public fun importDB(): Boolean {
        try {
            val sd = Environment.getExternalStorageDirectory()

            if (sd.canWrite()) {
                val currentPath = "/data/data/${context.packageName}/databases/"
                val backupDBPath = "/backup"

                val directory = File(sd, backupDBPath)
                val files = directory.listFiles()
                for (file in files) {
                    if (file.name == "dhis.db") {
                        val newFile = File(currentPath + file.name)
                        if (newFile.exists()) {
                            newFile.delete()
                        }
                        val fos = FileOutputStream(newFile)
                        val outputStream = BufferedOutputStream(fos)
                        val value = file.readBytes().decrypt(password)
                        outputStream.write(value)
                        outputStream.close()
                        fos.flush()
                        fos.close()
                    }
                }

                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    public fun exportDB(): Boolean {
        try {
            val sd = Environment.getExternalStorageDirectory()
            if (sd.canWrite()) {
                val currentPath = "/data/data/${context.packageName}/databases"
                val backupDBPath = "backup/"

                val directory = File(currentPath)
                val files = directory.listFiles()
                for (file in files) {
                    if (file.name == "dhis.db") {
                        val directoryBackend = File(sd, backupDBPath)
                        if (!directoryBackend.exists())
                            directoryBackend.mkdir()
                        val backupDB = File(directoryBackend, file.name)
                        val demoDecoded = File(directoryBackend, "other.db")
                        if (backupDB.exists()) {
                            backupDB.delete()
                        }
                        if (demoDecoded.exists()) {
                            demoDecoded.delete()
                        }
                        val fos = FileOutputStream(backupDB)
                        val outputStream = BufferedOutputStream(fos)
                        val value = file.readBytes().encrypt(password)
                        outputStream.write(value)
                        outputStream.close()
                        fos.flush()
                        fos.close()
                    }
                }
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }

    }
}