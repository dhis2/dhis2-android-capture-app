package org.dhis2.usescases.development

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import timber.log.Timber

class RulesValidationsModel(private val ruleValidator: ProgramRulesValidations) : ViewModel() {
    private val _ruleValidations: MutableLiveData<List<RuleValidation>> = MutableLiveData()
    val ruleValidations: LiveData<List<RuleValidation>> = _ruleValidations

    private val _programVariables: MutableLiveData<List<FieldUiModel>> = MutableLiveData()
    val programVariables: LiveData<List<FieldUiModel>> = _programVariables

    private val variableValueMap = HashMap<String, String>()

    val expressionValidationResult = MutableLiveData<Boolean>()

    init {
        fetchValidations()
        fetchProgramVariables("lxAQ7Zs9VYR")
    }

    private fun fetchValidations() {
        viewModelScope.launch {
            val result = async(context = Dispatchers.IO) {
                return@async ruleValidator.validateProgramRules().toMutableList()
            }
            try {
                _ruleValidations.value = result.await()
            } catch (e: Exception) {
                Timber.d(e)
            }
        }
    }

    private fun fetchProgramVariables(programUid: String) {
        viewModelScope.launch {
            val result = async(context = Dispatchers.IO) {
                return@async ruleValidator.programVariables(programUid)
            }
            try {
                _programVariables.value = result.await()
            } catch (e: Exception) {
                Timber.d(e)
            }
        }
    }

    fun handleAction(action: RowAction) {
        when (action.type) {
            ActionType.ON_SAVE,
            ActionType.ON_TEXT_CHANGE ->
                if (action.value != null) {
                    variableValueMap[action.id] = action.value!!
                } else {
                    variableValueMap.remove(action.id)
                }
        }
    }

    fun runCurrentValidation() {
        viewModelScope.launch {
            val result = async(context = Dispatchers.IO) {
                return@async ruleValidator.runValidation("lxAQ7Zs9VYR", variableValueMap)
            }
            try {
                expressionValidationResult.value = result.await()
            } catch (e: Exception) {
                Timber.d(e)
            }
        }
    }
}
