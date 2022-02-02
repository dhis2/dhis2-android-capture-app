package org.dhis2.usescases.troubleshooting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dhis2.commons.resources.LocaleSelector
import org.dhis2.usescases.development.RuleValidation
import java.util.Locale

class TroubleshootingViewModel(
    private val localeSelector: LocaleSelector,
    private val repository: TroubleshootingRepository
) : ViewModel() {
    private val _currentLocale = MutableLiveData(Locale.getDefault())
    val currentLocale: LiveData<Locale> = _currentLocale
    private val _ruleValidations = MutableLiveData(emptyList<RuleValidation>())
    val ruleValidations: LiveData<List<RuleValidation>> = _ruleValidations

    val supportedLocales = listOf(
        Locale("en"),
        Locale("ar"),
        Locale("bn"),
        Locale("ckb"),
        Locale("cs"),
        Locale("es"),
        Locale("fr"),
        Locale("id"),
        Locale("km"),
        Locale("lo"),
        Locale("nb"),
        Locale("prs"),
        Locale("ps"),
        Locale("pt"),
        Locale("pt", "BR"),
        Locale("ru"),
        Locale("sv"),
        Locale("tet"),
        Locale("tg"),
        Locale("uk"),
        Locale("ur"),
        Locale("uz"),
        Locale("vi"),
        Locale("zh"),
        Locale("zh", "rCN")
    )

    fun updateLocale(locale: Locale) {
        localeSelector.overrideUserLanguage(locale)
        _currentLocale.postValue(locale)
    }

    fun fetchRuleValidations() {
        viewModelScope.launch {
            val result = async(context = Dispatchers.IO) {
                repository.validateProgramRules()
            }
            try {
                _ruleValidations.postValue(result.await())
            } catch (e: Exception) {
                _ruleValidations.postValue(emptyList())
            }
        }
    }

}