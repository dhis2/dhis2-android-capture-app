package org.dhis2.usescases.general

import androidx.fragment.app.Fragment
import org.dhis2.commons.service.SessionManagerServiceImpl
import javax.inject.Inject

abstract class SessionManagerFragment : Fragment() {
    @Inject
    lateinit var sessionManagerServiceImpl: SessionManagerServiceImpl

    fun isUserLoggedIn(): Boolean = (::sessionManagerServiceImpl.isInitialized && sessionManagerServiceImpl.isUserLoggedIn())
}
