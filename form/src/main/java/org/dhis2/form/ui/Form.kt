package org.dhis2.form.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.launch
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FormSection
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.provider.inputfield.FieldProvider
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.component.Section
import org.hisp.dhis.mobile.ui.designsystem.component.SectionState
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@Composable
fun Form(
    sections: List<FormSection> = emptyList(),
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    resources: ResourceManager,
) {
    val scrollState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val callback =
        remember {
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
        modifier =
            Modifier
                .testTag("FORM_VIEW")
                .fillMaxSize()
                .background(
                    Color.White,
                    shape =
                        RoundedCornerShape(
                            topStart = Spacing.Spacing16,
                            topEnd = Spacing.Spacing16,
                            bottomStart = Spacing.Spacing0,
                            bottomEnd = Spacing.Spacing0,
                        ),
                ).clickable(
                    interactionSource =
                        remember {
                            MutableInteractionSource()
                        },
                    indication = null,
                    onClick = { focusManager.clearFocus() },
                ),
        contentPadding =
            PaddingValues(
                horizontal = Spacing.Spacing16,
                vertical = Spacing.Spacing16,
            ),
        state = scrollState,
    ) {
        if (sections.isNotEmpty()) {
            this.itemsIndexed(
                items = sections,
                key = { _, fieldUiModel -> fieldUiModel.uid },
            ) { _, section ->

                val onNextSection: () -> Unit = {
                    getNextSection(section, sections)?.let {
                        intentHandler.invoke(FormIntent.OnSection(it.uid))
                        scope.launch {
                            scrollState.animateScrollToItem(sections.indexOf(it))
                        }
                    } ?: run {
                        intentHandler.invoke(FormIntent.OnFocus("", null))
                        focusManager.clearFocus()
                    }
                }
                Section(
                    title = section.title,
                    isLastSection = getNextSection(section, sections) == null,
                    description = if (section.fields.isNotEmpty()) section.description else null,
                    completedFields = section.completeFields,
                    totalFields = section.totalFields,
                    state = section.state,
                    errorCount = section.errors,
                    warningCount = section.warnings,
                    warningMessage = section.warningMessage?.let { resources.getString(it) },
                    onNextSection = onNextSection,
                    onSectionClick = {
                        intentHandler.invoke(FormIntent.OnSection(section.uid))
                    },
                    content = {
                        if (section.fields.isNotEmpty()) {
                            section.fields.forEachIndexed { index, fieldUiModel ->
                                fieldUiModel.setCallback(callback)
                                FieldProvider(
                                    modifier = Modifier,
                                    fieldUiModel = fieldUiModel,
                                    uiEventHandler = uiEventHandler,
                                    intentHandler = intentHandler,
                                    resources = resources,
                                    focusManager = focusManager,
                                    onNextClicked = {
                                        manageOnNextEvent(
                                            focusManager,
                                            index,
                                            section,
                                            onNextSection,
                                        )
                                    },
                                    onFileSelected = { path ->
                                        intentHandler.invoke(
                                            FormIntent.OnStoreFile(
                                                uid = fieldUiModel.uid,
                                                filePath = path,
                                                valueType = fieldUiModel.valueType,
                                            ),
                                        )
                                    },
                                    reEvaluateCustomIntentRequestParameters = true,
                                )
                            }
                        }
                    },
                )
            }
            item(sections.size - 1) {
                Spacer(modifier = Modifier.height(Spacing.Spacing120))
            }
        }
    }
    if (shouldDisplayNoFieldsWarning(sections)) {
        NoFieldsWarning(resources)
    }
}

private fun manageOnNextEvent(
    focusManager: FocusManager,
    index: Int,
    section: FormSection,
    onNext: () -> Unit,
) {
    if (index == section.fields.size - 1) {
        onNext()
    } else {
        focusManager.moveFocus(FocusDirection.Down)
    }
}

fun shouldDisplayNoFieldsWarning(sections: List<FormSection>): Boolean =
    if (sections.size == 1) {
        val section = sections.first()
        section.state == SectionState.NO_HEADER && section.fields.isEmpty()
    } else {
        false
    }

@Composable
fun NoFieldsWarning(resources: ResourceManager) {
    Column(
        modifier =
            Modifier
                .padding(Spacing.Spacing16),
    ) {
        InfoBar(
            modifier =
                Modifier
                    .clip(shape = RoundedCornerShape(Radius.Full))
                    .background(SurfaceColor.WarningContainer),
            text = resources.getString(R.string.form_without_fields),
            icon = {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = "no fields",
                    tint = SurfaceColor.Warning,
                )
            },
            textColor = SurfaceColor.Warning,
            backgroundColor = SurfaceColor.WarningContainer,
        )
    }
}

private fun getNextSection(
    section: FormSection,
    sections: List<FormSection>,
): FormSection? {
    val currentIndex = sections.indexOf(section)
    if (currentIndex != -1 && currentIndex < sections.size - 1) {
        return sections[currentIndex + 1]
    }
    return null
}
