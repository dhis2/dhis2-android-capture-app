package org.dhis2.form.ui

import android.content.Context
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import kotlinx.coroutines.launch
import org.dhis2.form.BR
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.SectionUiModelImpl
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.ui.forms.CollapsableState
import org.dhis2.ui.forms.FormSection
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Form(
    items: List<FieldUiModel>,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    textWatcher: TextWatcher,
    coordinateTextWatcher: LatitudeLongitudeTextWatcher,
    needToForceUpdate: Boolean,
) {
    val scrollState = rememberLazyListState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val callback = object : FieldUiModel.Callback {
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = scrollState,
    ) {
        items.forEachIndexed { index, fieldUiModel ->
            val prevItem = items.getOrNull(index - 1)
            val nextItem = items.getOrNull(index + 1)
            val showBottomShadow = (fieldUiModel is SectionUiModelImpl) &&
                prevItem != null &&
                prevItem !is SectionUiModelImpl
            val sectionNumber = items.count {
                (it is SectionUiModelImpl) && items.indexOf(it) < index
            } + 1
            val lastSectionHeight = (fieldUiModel is SectionUiModelImpl) &&
                index > 0 &&
                index == items.size - 1 &&
                prevItem != null &&
                prevItem !is SectionUiModelImpl

            fieldUiModel.updateSectionData(
                showBottomShadow = showBottomShadow,
                sectionNumber = sectionNumber,
                lastSectionHeight = lastSectionHeight,
            )
            fieldUiModel.setCallback(callback)
            formItem(isSection = fieldUiModel is SectionUiModelImpl, key = fieldUiModel.uid) {
                if (fieldUiModel is SectionUiModelImpl) {
                    FormSection(
                        sectionNumber = sectionNumber,
                        sectionLabel = fieldUiModel.label,
                        fieldCount = fieldUiModel.totalFields,
                        completedFieldCount = fieldUiModel.completedFields,
                        errorCount = fieldUiModel.errors,
                        warningCount = fieldUiModel.warnings,
                        collapsableState = when (fieldUiModel.isOpen) {
                            true -> CollapsableState.OPENED
                            false -> CollapsableState.CLOSED
                            null -> CollapsableState.FIXED
                        },
                    ) {
                        fieldUiModel.setSelected()
                    }
                } else {
                    FieldProvider(
                        modifier = Modifier.animateItemPlacement(
                            animationSpec = tween(
                                durationMillis = 750,
                                easing = LinearOutSlowInEasing,
                            ),
                        ),
                        context = context,
                        fieldUiModel = fieldUiModel,
                        needToForceUpdate = needToForceUpdate,
                        textWatcher = textWatcher,
                        coordinateTextWatcher = coordinateTextWatcher,
                    )
                }
            }
            if (fieldUiModel !is SectionUiModelImpl &&
                nextItem is SectionUiModelImpl &&
                nextItem.isOpen == false
            ) {
                item {
                    NextSectionButton {
                        nextItem.setSelected()
                    }
                }
            }
        }
    }
}

@Composable
private fun NextSectionButton(onClick: () -> Unit) {
    Row(Modifier.padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.weight(1f))
        Button(
            text = stringResource(id = R.string.next),
            icon = {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    tint = SurfaceColor.Primary,
                    contentDescription = "",
                )
            },
            onClick = onClick,
        )
    }
}

private fun FieldUiModel.updateSectionData(
    showBottomShadow: Boolean,
    sectionNumber: Int,
    lastSectionHeight: Boolean,
) {
    if (this is SectionUiModelImpl) {
        setShowBottomShadow(showBottomShadow)
        setSectionNumber(sectionNumber)
        setLastSectionHeight(lastSectionHeight)
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.formItem(
    isSection: Boolean,
    key: String,
    content: @Composable LazyItemScope.() -> Unit,
) {
    if (isSection) {
        stickyHeader(key = key) {
            content()
        }
    } else {
        item(key = key) {
            content()
        }
    }
}

@Composable
private fun FieldProvider(
    modifier: Modifier,
    context: Context,
    fieldUiModel: FieldUiModel,
    needToForceUpdate: Boolean,
    textWatcher: TextWatcher,
    coordinateTextWatcher: LatitudeLongitudeTextWatcher,
) {
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
    ) {
        this.setVariable(BR.textWatcher, textWatcher)
        this.setVariable(BR.coordinateWatcher, coordinateTextWatcher)
        this.setVariable(BR.item, fieldUiModel)
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
