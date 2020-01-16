package org.dhis2.usescases

import android.content.Context
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import org.dhis2.DisableAnimations
import org.junit.After
import org.junit.Before
import org.junit.ClassRule

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
        allowPermissions()
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

    private fun populateDatabaseFromAssetsIfNeeded(){

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
    }
}