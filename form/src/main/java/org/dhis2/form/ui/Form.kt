package org.dhis2.form.ui

import android.content.Context
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import kotlinx.coroutines.launch
import org.dhis2.form.BR
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FormSection
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.InputText
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
import org.hisp.dhis.mobile.ui.designsystem.component.Section
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Form(
    sections: List<FormSection> = emptyList(),
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    textWatcher: TextWatcher,
    coordinateTextWatcher: LatitudeLongitudeTextWatcher,
    needToForceUpdate: Boolean,
) {
    val scrollState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val callback = remember {
        object : FieldUiModel.Callback {
            override fun intent(intent: FormIntent) {
                if (intent is FormIntent.OnNext) {
                    scope.launch {
                        intent.position?.let { scrollState.animateScrollToItem(it + 1) }
                    }
                }
                intentHandler(intent)
            }

            override fun recyclerViewUiEvents(uiEvent: RecyclerViewUiEvents) {
                uiEventHandler(uiEvent)
            }
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        state = scrollState,
        verticalArrangement = spacedBy(24.dp),
    ) {
        if (sections.isNotEmpty()) {
            this.itemsIndexed(
                items = sections,
                key = { _, fieldUiModel -> fieldUiModel.uid },
            ) { _, section ->
                Section(
                    title = section.title,
                    isLastSection = getNextSection(section, sections) == null,
                    description = section.description,
                    completedFields = section.completedFields(),
                    totalFields = section.fields.size,
                    state = section.state,
                    errorCount = section.errorCount(),
                    warningCount = section.warningCount(),
                    onNextSection = {
                        getNextSection(section, sections)?.let {
                            intentHandler.invoke(FormIntent.OnSection(it.uid))
                        }
                    },
                    onSectionClick = {
                        intentHandler.invoke(FormIntent.OnSection(section.uid))
                    },
                    content = {
                        section.fields.forEach { fieldUiModel ->
                            fieldUiModel.setCallback(callback)
                            FieldProvider(
                                modifier = Modifier.animateItemPlacement(
                                    animationSpec = tween(
                                        durationMillis = 500,
                                        easing = LinearOutSlowInEasing,
                                    ),
                                ),
                                context = context,
                                fieldUiModel = fieldUiModel,
                                needToForceUpdate = needToForceUpdate,
                                textWatcher = textWatcher,
                                coordinateTextWatcher = coordinateTextWatcher,
                                uiEventHandler = uiEventHandler,
                                intentHandler = intentHandler,
                            )
                        }
                    },
                )
            }
        }
    }
}

private fun getNextSection(section: FormSection, sections: List<FormSection>): FormSection? {
    val currentIndex = sections.indexOf(section)
    if (currentIndex != -1 && currentIndex < sections.size - 1) {
        return sections[currentIndex + 1]
    }
    return null
}

@Composable
private fun FieldProvider(
    modifier: Modifier,
    context: Context,
    fieldUiModel: FieldUiModel,
    needToForceUpdate: Boolean,
    textWatcher: TextWatcher,
    coordinateTextWatcher: LatitudeLongitudeTextWatcher,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    intentHandler: (FormIntent) -> Unit,
) {
    when {
        fieldUiModel.optionSet == null && fieldUiModel.valueType == ValueType.TEXT -> {
            var value by remember {
                mutableStateOf(fieldUiModel.value)
            }
            InputText(
                modifier = Modifier.fillMaxWidth(),
                title = fieldUiModel.label,
                state = when {
                    fieldUiModel.error != null -> InputShellState.ERROR
                    !fieldUiModel.editable -> InputShellState.DISABLED
                    fieldUiModel.focused -> InputShellState.FOCUSED
                    else -> InputShellState.UNFOCUSED
                },
                supportingText = mutableListOf<SupportingTextData>().apply {
                    fieldUiModel.error?.let {
                        add(
                            SupportingTextData(
                                it,
                                SupportingTextState.ERROR,
                            ),
                        )
                    }
                    fieldUiModel.warning?.let {
                        add(
                            SupportingTextData(
                                it,
                                SupportingTextState.WARNING,
                            ),
                        )
                    }
                    fieldUiModel.description?.let {
                        add(
                            SupportingTextData(
                                it,
                                SupportingTextState.DEFAULT,
                            ),
                        )
                    }
                },
                legendData = fieldUiModel.legend?.let {
                    LegendData(Color(it.color), it.label ?: "", null)
                },
                inputText = value ?: "",
                onNextClicked = {
                    intentHandler.invoke(FormIntent.OnNext(fieldUiModel.uid, value))
                },
                onValueChanged = {
                    value = it
                    intentHandler(
                        FormIntent.OnSave(
                            fieldUiModel.uid,
                            value,
                            fieldUiModel.valueType,
                        ),
                    )
                },
            )
        }

        else -> {
            AndroidViewBinding(
                modifier = modifier.fillMaxWidth(),
                factory = { inflater, viewgroup, add ->
                    getFieldView(
                        context,
                        inflater,
                        viewgroup,
                        add,
                        fieldUiModel.layoutId,
                        needToForceUpdate,
                    )
                },
                update = {
                    this.setVariable(BR.textWatcher, textWatcher)
                    this.setVariable(BR.coordinateWatcher, coordinateTextWatcher)
                    this.setVariable(BR.item, fieldUiModel)
                },
            )
        }
    }
}

private fun getFieldView(
    context: Context,
    inflater: LayoutInflater,
    viewgroup: ViewGroup,
    add: Boolean,
    layoutId: Int,
    needToForceUpdate: Boolean,
): ViewDataBinding {
    val layoutInflater =
        if (needToForceUpdate) {
            inflater.cloneInContext(
                ContextThemeWrapper(
                    context,
                    R.style.searchFormInputText,
                ),
            )
        } else {
            inflater.cloneInContext(
                ContextThemeWrapper(
                    context,
                    R.style.formInputText,
                ),
            )
        }

    return DataBindingUtil.inflate(layoutInflater, layoutId, viewgroup, add)
}
