package org.dhis2.usescases.about

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.MainRobot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import android.content.pm.PackageManager
import android.R.attr.versionName
import android.content.pm.PackageInfo
import android.provider.Settings.Global.getString
import org.dhis2.BuildConfig
import org.dhis2.R


@RunWith(AndroidJUnit4::class)
class AboutTest : BaseTest() {

    private lateinit var mainRobot: MainRobot
    private lateinit var aboutRobot: AboutRobot

    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java, false, false)

    override fun getPermissionsToBeAccepted(): Array<String> {
        return arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
    }

    override fun setUp() {
        super.setUp()
        mainRobot = MainRobot(context)
        aboutRobot = AboutRobot(context)
    }


    @Test
    fun checkVersionNames() {
        startActivity()

        val appVersion = getAppVersionName()
        val sdkVersion = getSDKVersionName()

        mainRobot.clickOnNavigationDrawerMenu()
                .clickAbout()
        aboutRobot.checkVersionNames(appVersion, sdkVersion)
        //Assert versionName
        //Assert SDK version
    }

    private fun startActivity(){
        rule.launchActivity(null)
    }

    fun getAppVersionName() : String {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    fun getSDKVersionName() =
         String.format(context.getString(R.string.about_sdk), BuildConfig.SDK_VERSION)
}