package org.dhis2.usescases.login

import androidx.annotation.UiThread
import org.dhis2.usescases.general.AbstractActivityContracts

class LoginContracts {
    interface View : AbstractActivityContracts.View {
        @UiThread
        fun onUnlockClick()
    }
}
