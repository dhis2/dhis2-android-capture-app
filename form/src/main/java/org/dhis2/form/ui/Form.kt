package org.dhis2.form.ui

import android.text.TextWatcher
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FormSection
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.provider.inputfield.FieldProvider
import org.hisp.dhis.mobile.ui.designsystem.component.Section

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Form(
    sections: List<FormSection> = emptyList(),
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    textWatcher: TextWatcher,
    coordinateTextWatcher: LatitudeLongitudeTextWatcher,
    needToForceUpdate: Boolean,
    resources: ResourceManager,
) {
    val scrollState = rememberLazyListState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val callback = remember {
        object : FieldUiModel.Callback {
            override fun intent(intent: FormIntent) {
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
            .padding(horizontal = 16.dp, vertical = 16.dp),
        state = scrollState
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
                            coroutineScope.launch {
                                scrollState.animateScrollToItem(sections.indexOf(it))
                                intentHandler.invoke(FormIntent.OnSection(it.uid))
                            }
                        }
                    },
                    onSectionClick = {
                        coroutineScope.launch {
                            scrollState.animateScrollToItem(sections.indexOf(section))
                            intentHandler.invoke(FormIntent.OnSection(section.uid))
                        }
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
                                resources = resources,
                                focusManager = focusManager,
                            )
                        }
                    },
                )
            }
            item(sections.size - 1) {
                Spacer(modifier = Modifier.height(120.dp))
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
