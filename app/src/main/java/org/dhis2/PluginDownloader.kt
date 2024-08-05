package org.dhis2

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class PluginDownloader(val context: Context) {

    private val client = OkHttpClient()

    /**
     * Downloads the plugin APK from the given URL and saves it to internal storage.
     * @param url The URL to download the APK from.
     * @return The file object pointing to the downloaded APK, or null if failed.
     */
    suspend fun downloadPlugin(url: String): File? {
        return withContext(Dispatchers.IO) {
            // Create a request to download the file
            val request = Request.Builder().url(url).build()

            // Execute the request
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null

                // Create a temporary file in the internal storage
                val tempFile = File(context.filesDir, "temp_plugin.apk")
                // Write the response body to the temp file
                response.body?.byteStream()?.let { inputStream ->
                    writeToFile(inputStream, tempFile)
                }

                // Create a file in the code cache directory
                val codeCacheDir = context.codeCacheDir
                val pluginFile = File(codeCacheDir, "plugin.apk")
                // Move the file to the code cache directory
                moveFileToCodeCacheDir(tempFile, pluginFile)

                return@withContext pluginFile
            }
        }
    }

    /**
     * Writes an InputStream to a File.
     */
    private fun writeToFile(inputStream: InputStream, file: File) {
        var outputStream: OutputStream? = null
        try {
            outputStream = FileOutputStream(file)
            val buffer = ByteArray(4 * 1024) // 4KB buffer
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream?.close()
            inputStream.close()
        }
    }

    /**
     * Moves a file to the code cache directory, ensuring it is read-only.
     */
    private fun moveFileToCodeCacheDir(sourceFile: File, destFile: File) {
        sourceFile.copyTo(destFile, overwrite = true)
        sourceFile.delete()
        destFile.setReadable(true, false)
        destFile.setWritable(false)
        destFile.setExecutable(true, false)
    }
}
