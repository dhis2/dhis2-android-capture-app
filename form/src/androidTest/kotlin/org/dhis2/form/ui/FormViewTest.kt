package org.dhis2.form.ui

import androidx.fragment.app.FragmentManager
import org.dhis2.commons.locationprovider.LocationProvider
import org.dhis2.form.ui.provider.EnrollmentResultDialogProvider
import org.junit.Before
import javax.inject.Inject

class FormViewTest {


    @Inject
    lateinit var locationProvider: LocationProvider

    @Inject
    lateinit var enrollmentResultDialogProvider: EnrollmentResultDialogProvider

    @Inject
    lateinit var supportFragmentManager: FragmentManager

    @Before
    fun setUp() {
        locationProvider = mock()
        enrollmentResultDialogProvider = mock()
        supportFragmentManager = mock()
    }
}