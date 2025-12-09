package org.dhis2.form.ui

import android.webkit.WebView
import com.google.gson.Gson
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FormSection
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.plugin.FieldMetadata
import org.dhis2.form.ui.plugin.PluginInterface
import org.dhis2.form.ui.plugin.PluginProps
import org.dhis2.form.ui.provider.inputfield.FieldProvider
import org.hisp.dhis.mobile.ui.designsystem.component.InfoBar
import org.hisp.dhis.mobile.ui.designsystem.component.Section
import org.hisp.dhis.mobile.ui.designsystem.component.SectionState
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

// Mock data for testing the plugin
private fun createMockPluginProps(): PluginProps {
    return PluginProps(
        values = mapOf(
            "field1" to "Test Value 1",
            "field2" to 42,
            "field3" to null,
        ),
        errors = mapOf(
            "field2" to listOf("Value must be less than 40"),
        ),
        warnings = mapOf(
            "field1" to listOf("Consider updating this value"),
        ),
        formSubmitted = false,
        fieldsMetadata = mapOf(
            "field1" to FieldMetadata(
                id = "field1",
                name = "First Name",
                shortName = "FName",
                formName = "First Name",
                disabled = false,
                compulsory = true,
                description = "Enter your first name",
                type = "TEXT",
                optionSet = null,
                displayInForms = true,
                displayInReports = true,
                icon = null,
                unique = null,
                searchable = true,
                url = null,
            ),
            "field2" to FieldMetadata(
                id = "field2",
                name = "Age",
                shortName = "Age",
                formName = "Age",
                disabled = false,
                compulsory = false,
                description = "Enter your age",
                type = "INTEGER",
                optionSet = null,
                displayInForms = true,
                displayInReports = true,
                icon = null,
                unique = null,
                searchable = false,
                url = null,
            ),
            "field3" to FieldMetadata(
                id = "field3",
                name = "Email",
                shortName = "Email",
                formName = "Email Address",
                disabled = false,
                compulsory = false,
                description = "Enter your email address",
                type = "EMAIL",
                optionSet = null,
                displayInForms = true,
                displayInReports = false,
                icon = null,
                unique = true,
                searchable = true,
                url = null,
            ),
        ),
    )
}

@Composable
fun Form(
    sections: List<FormSection> = emptyList(),
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    resources: ResourceManager,
    pluginProps: PluginProps? = null,
) {
    // Use mock data for testing if no pluginProps provided
    val effectivePluginProps = pluginProps ?: createMockPluginProps()

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
        item {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowFileAccess = true
                        settings.allowContentAccess = true
                        settings.allowFileAccessFromFileURLs = true
                        settings.allowUniversalAccessFromFileURLs = true
                        
                        // Enable WebView debugging
                        WebView.setWebContentsDebuggingEnabled(true)
                        
                        addJavascriptInterface(
                            PluginInterface { value ->
                                android.util.Log.d("PluginInterface", "Received value: $value")
                                // TODO: Parse value and call SDK to save field value
                            },
                            "Android",
                        )
                        
                        webViewClient = object : android.webkit.WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                // Inject fetch interceptor early before any scripts run
                                view?.evaluateJavascript(
                                    """
                                    (function() {
                                        if (window.__fetchIntercepted) return;
                                        window.__fetchIntercepted = true;
                                        
                                        const originalFetch = window.fetch;
                                        window.fetch = function(url, options) {
                                            console.log('[FetchInterceptor] Intercepting:', url);
                                            
                                            // Intercept API calls
                                            if (url && (url.includes('/api/') || url.startsWith('file:///api/'))) {
                                                console.log('[FetchInterceptor] Mocking API call:', url);
                                                
                                                if (url.includes('/api/system/info')) {
                                                    return Promise.resolve(new Response(JSON.stringify({
                                                        version: "2.40",
                                                        revision: "android-sdk",
                                                        contextPath: "https://play.dhis2.org/40"
                                                    }), {
                                                        status: 200,
                                                        headers: { 'Content-Type': 'application/json' }
                                                    }));
                                                }
                                                
                                                // Default empty response for other API calls
                                                return Promise.resolve(new Response('{}', {
                                                    status: 200,
                                                    headers: { 'Content-Type': 'application/json' }
                                                }));
                                            }
                                            
                                            return originalFetch.apply(this, arguments);
                                        };
                                        console.log('[FetchInterceptor] Fetch interceptor installed');
                                    })();
                                    """.trimIndent(),
                                    null,
                                )
                            }
                            
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                android.util.Log.d("WebView", "Page finished loading: $url")
                                // Send props after page loads
                                val propsJson = Gson().toJson(effectivePluginProps)
                                view?.evaluateJavascript(
                                    """
                                    console.log('Injecting props from onPageFinished');
                                    window.__ANDROID_PLUGIN_PROPS__ = $propsJson;
                                    if (window.setPluginProps) {
                                        window.setPluginProps($propsJson);
                                    }
                                    window.dispatchEvent(new CustomEvent('androidPropsUpdated', { detail: $propsJson }));
                                    """.trimIndent(),
                                    null,
                                )
                            }
                            
                            override fun onReceivedError(
                                view: WebView?,
                                request: android.webkit.WebResourceRequest?,
                                error: android.webkit.WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                android.util.Log.e("WebView", "Error loading: ${request?.url}, error: ${error?.description}")
                            }
                        }
                        
                        webChromeClient = object : android.webkit.WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                                android.util.Log.d("WebViewConsole", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                                return true
                            }
                        }
                        
                        // Load the bridge HTML
                        loadUrl("file:///android_asset/simple-capture-plugin-1.0.0/android-bridge.html")
                    }
                },
                update = { _ ->
                    // Props are now sent in onPageFinished callback
                },
            )
        }
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
