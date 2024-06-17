package org.dhis2.usescases.deleteAccount

import android.os.Parcel
import android.os.Parcelable
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.dhis2.bindings.buildInfo
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.usescases.BaseTest
import org.dhis2.usescases.login.loginRobot
import org.dhis2.usescases.main.MainActivity
import org.dhis2.usescases.main.homeRobot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeleteAccountTest() : BaseTest(), Parcelable {

    @get:Rule
    val rule = ActivityTestRule(MainActivity::class.java, false, false)

    constructor(parcel: Parcel) : this() {
    }

    @Test
    fun deleteAccount() {
        startActivity()

        homeRobot {
            clickOnNavigationDrawerMenu()
            clickDeleteAccount()
            clickAccept()
        }
        loginRobot {
            isServerURLFieldDisplayed()
        }

    }

    private fun startActivity() {
        rule.launchActivity(null)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DeleteAccountTest> {
        override fun createFromParcel(parcel: Parcel): DeleteAccountTest {
            return DeleteAccountTest(parcel)
        }

        override fun newArray(size: Int): Array<DeleteAccountTest?> {
            return arrayOfNulls(size)
        }
    }

}
