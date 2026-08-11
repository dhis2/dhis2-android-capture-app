package org.dhis2.usescases.troubleshooting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dhis2.commons.resources.LocaleSelector
import org.dhis2.usescases.development.ProgramRuleValidation
import java.util.Locale

class TroubleshootingViewModel(
    private val localeSelector: LocaleSelector,
    private val repository: TroubleshootingRepository,
    val openLanguageSection: Boolean,
) : ViewModel() {
    private val _currentLocale = MutableLiveData<Locale>()
    val currentLocale: LiveData<Locale> = _currentLocale
    private val _ruleValidations = MutableLiveData<List<ProgramRuleValidation>>()
    val ruleValidations: LiveData<List<ProgramRuleValidation>> = _ruleValidations
    private val _visibleProgram = MutableLiveData<String?>()
    val visibleProgram: LiveData<String?> = _visibleProgram
    private val _localesToDisplay = MutableLiveData<List<Locale>>()
    val localesToDisplay: LiveData<List<Locale>> = _localesToDisplay

    private val supportedLocalLanguages =
        listOf(
            "bn",
            "ar",
            "en",
            "ckb",
            "cs",
            "es",
            "fr",
            "id",
            "km",
            "lo",
            "nb",
            "prs",
            "ps",
            "pt",
            "pt",
            "BR",
            "ru",
            "sv",
            "tet",
            "tg",
            "uk",
            "ur",
            "uz",
            "vi",
            "zh",
            "zh",
            "rCN",
        )

    val supportedLocales =
        Locale
            .getAvailableLocales()
            .filter { it.language in supportedLocalLanguages }

    init {
        val initialLocale =
            localeSelector.getUserLanguage()?.let {
                Locale.getAvailableLocales().find { locale -> locale.language == it }
            } ?: Locale.getDefault()
        _currentLocale.postValue(initialLocale)
        _localesToDisplay.postValue(
            supportedLocales.filter { it.language != initialLocale.language },
        )
    }

    fun updateLocale(locale: Locale) {
        localeSelector.overrideUserLanguage(locale)
        _currentLocale.postValue(locale)
    }

    fun fetchRuleValidations() {
        viewModelScope.launch {
            val result =
                async(context = Dispatchers.IO) {
                    repository.validateProgramRules()
                }
            try {
                _ruleValidations.postValue(result.await())
            } catch (e: Exception) {
                _ruleValidations.postValue(emptyList())
            }
        }
    }

    fun onProgramSelected(selectedProgramUid: String) {
        _visibleProgram.value =
            if (_visibleProgram.value == selectedProgramUid) {
                null
            } else {
                selectedProgramUid
            }
    }
}
