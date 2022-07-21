package org.dhis2.form.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.form.data.OptionSetDialogRepository
import org.hisp.dhis.android.core.option.Option

class OptionSetDialogViewModel(
    private val optionRepository: OptionSetDialogRepository,
    val field: FieldUiModel,
    private val dispatchers: DispatcherProvider
) : ViewModel() {
    val options: MutableLiveData<List<Option>> = MutableLiveData(emptyList())
    val searchValue: MutableLiveData<String> = MutableLiveData("")

    init {
        viewModelScope.launch {
            val initialOptions = loadOptions()
            options.postValue(initialOptions)
        }
    }

    fun onSearchingOption(newValue: String) {
        searchValue.value = newValue
        viewModelScope.launch {
            options.postValue(loadOptions(newValue))
        }
    }

    private suspend fun loadOptions(textToSearch: String = ""): List<Option> {
        return withContext(dispatchers.io()) {
            optionRepository.searchForOption(
                field.optionSet,
                textToSearch,
                field.optionsToShow ?: emptyList(),
                field.optionsToHide ?: emptyList()
            )
        }
    }
}

class OptionSetDialogViewModelFactory(
    private val optionRepository: OptionSetDialogRepository,
    private val field: FieldUiModel,
    private val dispatchers: DispatcherProvider

) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return OptionSetDialogViewModel(optionRepository, field, dispatchers) as T
    }
}
