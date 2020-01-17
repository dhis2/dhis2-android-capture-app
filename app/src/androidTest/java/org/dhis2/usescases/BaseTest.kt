package org.dhis2.usescases

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.DisableAnimations
import org.junit.After
import org.junit.Before
import org.junit.ClassRule
import java.io.*

open class BaseTest {

    @JvmField
    protected var context: Context? = InstrumentationRegistry.getInstrumentation().targetContext

   // @Rule
   // var rule: ActivityTestRule<*> = getActivityTestRule()

    protected open fun getPermissionsToBeAccepted() = arrayOf<String>()

    @Before
    @Throws(Exception::class)
    open fun setUp() {
        populateDatabaseFromAssetsIfNeeded()
     //   allowPermissions()
        setupMockServerIfNeeded()
        injectDependencies()
    }

    private fun allowPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            getPermissionsToBeAccepted().forEach {
                InstrumentationRegistry.getInstrumentation()
                        .uiAutomation
                        .executeShellCommand("pm grant ${context?.packageName} $it")
            }
        }
    }

    private fun populateDatabaseFromAssetsIfNeeded() {
        val databasePath = context?.applicationInfo?.dataDir + "/databases"
        val file = File("$databasePath/$DB_NAME")

     /*   if (file.exists()){
            Log.i("TEST","DB ALREADY LOADED")
            return
        } */

        try {
            val input = context!!.assets.open("databases/$DB_NAME")
            val output = FileOutputStream("$databasePath/$DB_NAME")

            writeExtractedFileToDisk(input, output)
        } catch (e: IOException) {

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



    private fun injectDependencies() {

    }

    private fun setupMockServerIfNeeded() {

    }

    @After
    @Throws(Exception::class)
    open fun teardown() {
        cleanPreferences()
    }

    private fun cleanPreferences() {

    }

    companion object {
        @ClassRule
        @JvmField
        val disableAnimationsTestRule = DisableAnimations()
        const val DB_NAME = "dhis.db"
    }
}