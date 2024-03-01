package org.dhis2.form.ui

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FormSection
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.provider.inputfield.FieldProvider
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBarData
import org.hisp.dhis.mobile.ui.designsystem.component.Section
import org.hisp.dhis.mobile.ui.designsystem.component.SectionState
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@OptIn(ExperimentalFoundationApi::class)
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
    val focusNext = remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp))
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = null,
                onClick = { focusManager.clearFocus() },
            ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        state = scrollState,
    ) {
        if (sections.isNotEmpty()) {
            this.itemsIndexed(
                items = sections,
                key = { _, fieldUiModel -> fieldUiModel.uid },
            ) { _, section ->
                val isSectionOpen = remember(section.state) {
                    derivedStateOf { section.state == SectionState.OPEN }
                }

                LaunchIfTrue(isSectionOpen.value) {
                    scrollState.animateScrollToItem(sections.indexOf(section))
                    focusManager.moveFocusNext(focusNext)
                }

                val onNextSection: () -> Unit = {
                    getNextSection(section, sections)?.let {
                        intentHandler.invoke(FormIntent.OnSection(it.uid))
                        scope.launch {
                            scrollState.animateScrollToItem(sections.indexOf(it))
                        }
                    } ?: run {
                        focusManager.clearFocus()
                    }
                }

                Section(
                    title = section.title,
                    isLastSection = getNextSection(section, sections) == null,
                    description = if (section.fields.isNotEmpty()) section.description else null,
                    completedFields = section.completedFields(),
                    totalFields = section.fields.size,
                    state = section.state,
                    errorCount = section.errorCount(),
                    warningCount = section.warningCount(),
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
                                    modifier = Modifier.animateItemPlacement(
                                        animationSpec = tween(
                                            durationMillis = 500,
                                            easing = LinearOutSlowInEasing,
                                        ),
                                    ),
                                    fieldUiModel = fieldUiModel,
                                    uiEventHandler = uiEventHandler,
                                    intentHandler = intentHandler,
                                    resources = resources,
                                    focusManager = focusManager,
                                    onNextClicked = {
                                        if (index == section.fields.size - 1) {
                                            onNextSection()
                                            focusNext.value = true
                                        } else {
                                            focusManager.moveFocus(FocusDirection.Down)
                                        }
                                    },
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

fun shouldDisplayNoFieldsWarning(sections: List<FormSection>): Boolean {
    return if (sections.size == 1) {
        val section = sections.first()
        section.state == SectionState.NO_HEADER && section.fields.isEmpty()
    } else {
        false
    }
}

@Composable
fun NoFieldsWarning(resources: ResourceManager) {
    Column(
        modifier = Modifier
            .padding(Spacing.Spacing16),
    ) {
        InfoBar(
            infoBarData = InfoBarData(
                text = resources.getString(R.string.form_without_fields),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.ErrorOutline,
                        contentDescription = "no fields",
                        tint = SurfaceColor.Warning,
                    )
                },
                color = SurfaceColor.Warning,
                backgroundColor = SurfaceColor.WarningContainer,
                actionText = null,
                onClick = null,
            ),
            modifier = Modifier
                .clip(shape = RoundedCornerShape(Radius.Full))
                .background(SurfaceColor.WarningContainer),
        )
    }
}

private fun FocusManager.moveFocusNext(focusNext: MutableState<Boolean>) {
    if (focusNext.value) {
        this.moveFocus(FocusDirection.Next)
        focusNext.value = false
    }
}

@Composable
private fun LaunchIfTrue(key: Boolean, block: suspend CoroutineScope.() -> Unit) {
    LaunchedEffect(key) {
        if (key) {
            block()
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
