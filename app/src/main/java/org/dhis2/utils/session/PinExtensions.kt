package org.dhis2.utils.session

import com.andrognito.pinlockview.PinLockListener
import com.andrognito.pinlockview.PinLockView

inline fun PinLockView.onPinSet(crossinline continuation: (String)->Unit){
    setPinLockListener(object : PinLockListener{
        override fun onEmpty() {
        }

        override fun onComplete(pin: String) {
            continuation(pin)
        }

        override fun onPinChange(pinLength: Int, intermediatePin: String?) {
        }
    })
}