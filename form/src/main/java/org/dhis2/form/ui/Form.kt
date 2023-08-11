package org.dhis2.form.ui

import android.content.Context
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Form(
    items: List<FieldUiModel>,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    textWatcher: TextWatcher,
    coordinateTextWatcher: LatitudeLongitudeTextWatcher,
    needToForceUpdate: Boolean
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
        state = scrollState
    ) {
        items.forEach {
            it.setCallback(callback)
            formItem(isSection = it is SectionUiModelImpl, key = it.uid) {
                FieldProvider(
                    context = context,
                    fieldUiModel = it,
                    needToForceUpdate = needToForceUpdate,
                    textWatcher = textWatcher,
                    coordinateTextWatcher = coordinateTextWatcher
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.formItem(
    isSection: Boolean,
    key: String,
    content: @Composable LazyItemScope.() -> Unit
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
    context: Context,
    fieldUiModel: FieldUiModel,
    needToForceUpdate: Boolean,
    textWatcher: TextWatcher,
    coordinateTextWatcher: LatitudeLongitudeTextWatcher,
) {
    AndroidViewBinding(
        modifier = Modifier.fillMaxWidth(),
        factory = { inflater, viewgroup, add ->
            getFieldView(
                context,
                inflater,
                viewgroup,
                add,
                fieldUiModel.layoutId,
                needToForceUpdate
            )
        }) {
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
    needToForceUpdate: Boolean
): ViewDataBinding {
    val layoutInflater =
        if (needToForceUpdate) {
            inflater.cloneInContext(
                ContextThemeWrapper(
                    context,
                    R.style.searchFormInputText
                )
            )
        } else {
            inflater.cloneInContext(
                ContextThemeWrapper(
                    context,
                    R.style.formInputText
                )
            )
        }

    return DataBindingUtil.inflate(layoutInflater, layoutId, viewgroup, add)
}