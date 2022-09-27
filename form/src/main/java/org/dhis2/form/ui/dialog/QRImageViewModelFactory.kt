package org.dhis2.form.ui.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class QRImageViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return QRImageViewModel(QRImageControllerImpl()) as T
    }
}
