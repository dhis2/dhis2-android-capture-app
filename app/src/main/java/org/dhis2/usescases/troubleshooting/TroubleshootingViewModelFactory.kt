package org.dhis2.usescases.troubleshooting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.resources.LocaleSelector

@Suppress("UNCHECKED_CAST")
class TroubleshootingViewModelFactory(
    private val localeSelector: LocaleSelector,
    private val troubleshootingRepository: TroubleshootingRepository,
    private val openLanguageSection: Boolean,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TroubleshootingViewModel(
            localeSelector,
            troubleshootingRepository,
            openLanguageSection,
        ) as T
    }
}
