package org.dhis2.common

import android.content.Context
import org.hisp.dhis.android.core.arch.file.IFileReader
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

class FileReader(val context: Context) : IFileReader {
    override fun getStringFromFile(filename: String): String {
        val builder = StringBuilder()

        val `in` = context.assets.open(filename)
        val reader = BufferedReader(InputStreamReader(`in`, Charset.forName("UTF-8")))
        var reading: String?

        reading = reader.readLine()
        while (reading != null) {
            builder.append(reading)
            reading = reader.readLine()
        }

        `in`.close()
        return builder.toString()
    }
}