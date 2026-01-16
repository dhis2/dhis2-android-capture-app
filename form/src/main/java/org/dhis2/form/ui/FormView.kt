package org.dhis2.form.ui

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.journeyapps.barcodescanner.ScanOptions
import org.dhis2.commons.Constants
import org.dhis2.commons.data.FormFileProvider
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.dialogs.AlertBottomDialog
import org.dhis2.commons.dialogs.CustomDialog
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialog
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.commons.dialogs.bottomsheet.DialogButtonStyle
import org.dhis2.commons.extensions.closeKeyboard
import org.dhis2.commons.extensions.serializable
import org.dhis2.commons.locationprovider.LocationProvider
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.periods.ui.PeriodSelectorContent
import org.dhis2.form.R
import org.dhis2.form.data.RulesUtilsProviderConfigurationError
import org.dhis2.form.data.scan.ScanContract
import org.dhis2.form.data.toMessage
import org.dhis2.form.di.Injector
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FormRepositoryRecords
import org.dhis2.form.model.InfoUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.model.exception.RepositoryRecordsException
import org.dhis2.form.ui.customintent.CustomIntentActivityResultContract
import org.dhis2.form.ui.customintent.CustomIntentInput
import org.dhis2.form.ui.customintent.CustomIntentResult
import org.dhis2.form.ui.dialog.QRDetailBottomDialog
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.mapper.FormSectionMapper
import org.dhis2.maps.views.MapSelectorActivity
import org.dhis2.maps.views.MapSelectorActivity.Companion.DATA_EXTRA
import org.dhis2.maps.views.MapSelectorActivity.Companion.FIELD_UID
import org.dhis2.maps.views.MapSelectorActivity.Companion.LOCATION_TYPE_EXTRA
import org.dhis2.mobile.commons.files.FileHandlerImpl
import org.dhis2.mobile.commons.orgunit.OrgUnitSelectorScope
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType
import timber.log.Timber
import java.io.File

class FormView : Fragment() {
    private var onItemChangeListener: ((action: RowAction) -> Unit)? = null
    private var locationProvider: LocationProvider? = null
    private var onLoadingListener: ((loading: Boolean) -> Unit)? = null
    private var onFocused: (() -> Unit)? = null
    private var onFinishDataEntry: (() -> Unit)? = null
    private var onActivityForResult: (() -> Unit)? = null
    private var completionListener: ((percentage: Float) -> Unit)? = null
    private var onFieldItemsRendered: ((fieldsEmpty: Boolean) -> Unit)? = null
    private var actionIconsActivate: Boolean = true
    private var openErrorLocation: Boolean = false
    private var useCompose = false
    private var programUid: String? = null

    private val qrScanContent =
        registerForActivityResult(ScanContract()) { result ->
            result.contents?.let { qrData ->
                val intent =
                    FormIntent.OnSave(
                        result.originalIntent.getStringExtra(Constants.UID)!!,
                        qrData,
                        ValueType.TEXT,
                    )
                intentHandler(intent)
            }
        }

    private val mapContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data?.extras != null) {
                val uid = it.data?.getStringExtra(FIELD_UID)
                val featureType = it.data?.getStringExtra(LOCATION_TYPE_EXTRA)
                val coordinates = it.data?.getStringExtra(DATA_EXTRA)
                if (uid != null && featureType != null) {
                    val intent =
                        FormIntent.SelectLocationFromMap(
                            uid,
                            featureType,
                            coordinates,
                        )
                    intentHandler(intent)
                }
            }
        }

    private val requestStoragePermissions =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            if (granted) {
                downloadFile(viewModel.filePath)
                viewModel.filePath = null
            } else {
                Toast
                    .makeText(
                        requireContext(),
                        requireContext().getString(R.string.storage_permission_denied),
                        Toast.LENGTH_LONG,
                    ).show()
            }
        }

    private var openCustomIntentLauncher =
        registerForActivityResult(
            CustomIntentActivityResultContract(),
        ) {
            when (it) {
                is CustomIntentResult.Error -> {
                    val loadingIntent = FormIntent.OnFieldFinishedLoadingData(it.fieldUid)
                    intentHandler(loadingIntent)
                    val intent =
                        FormIntent.OnSaveCustomIntent(
                            it.fieldUid,
                            null,
                            true,
                        )
                    intentHandler(intent)
                }
                is CustomIntentResult.Success -> {
                    val loadingIntent = FormIntent.OnFieldFinishedLoadingData(it.fieldUid)
                    intentHandler(loadingIntent)
                    val intent =
                        FormIntent.OnSaveCustomIntent(
                            it.fieldUid,
                            it.value,
                            false,
                        )
                    intentHandler(intent)
                }
            }
        }

    private val viewModel: FormViewModel by viewModels {
        Injector.provideFormViewModelFactory(
            context = requireContext(),
            repositoryRecords =
                arguments?.serializable(RECORDS)
                    ?: throw RepositoryRecordsException(),
            openErrorLocation = openErrorLocation,
            useCompose = useCompose,
        )
    }
    private lateinit var formSectionMapper: FormSectionMapper
    var scrollCallback: ((Boolean) -> Unit)? = null
    private var displayConfErrors = true

    private val fileHandler = FileHandlerImpl()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val contextWrapper = ContextThemeWrapper(context, R.style.searchFormInputText)
        formSectionMapper = FormSectionMapper()

        FormFileProvider.init(contextWrapper.applicationContext)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                val items by viewModel.items.observeAsState()
                val sections =
                    items?.let {
                        formSectionMapper.mapFromFieldUiModelList(it)
                    } ?: emptyList()

                var resultDialogData: FormViewModel.FormActions.ShowResultDialog? by remember {
                    mutableStateOf(null)
                }

                LaunchedEffect(viewModel.actionsChannel) {
                    viewModel.actionsChannel.collect { action ->
                        when (action) {
                            FormViewModel.FormActions.OnFinish ->
                                onFinishDataEntry?.invoke()

                            is FormViewModel.FormActions.ShowResultDialog ->
                                resultDialogData = action
                        }
                    }
                }

                Form(
                    sections = sections,
                    intentHandler = ::intentHandler,
                    uiEventHandler = ::uiEventHandler,
                    resources = Injector.provideResourcesManager(context),
                )

                resultDialogData?.let {
                    DataEntryBottomSheet(
                        model = it.model,
                        allowDiscard = it.allowDiscard,
                        fieldsWithIssues = it.fieldsWithIssues,
                        onPrimaryButtonClick = {
                            when (it.model.mainButton) {
                                DialogButtonStyle.CompleteButton -> {
                                    viewModel.completeEvent()
                                    onFinishDataEntry?.invoke()
                                }

                                else -> {
                                    // Do nothing
                                }
                            }
                        },
                        onSecondaryButtonClick = {
                            onFinishDataEntry?.invoke()
                        },
                        onDiscardChanges = viewModel::discardChanges,
                        onDismiss = {
                            resultDialogData = null
                        },
                    )
                }
            }
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setObservers()
    }

    private fun setObservers() {
        viewModel.savedValue.observe(
            viewLifecycleOwner,
        ) { rowAction ->
            onItemChangeListener?.let { it(rowAction) }
        }

        viewModel.items.observe(
            viewLifecycleOwner,
        ) { items ->
            render(items)
        }

        viewModel.loading.observe(
            viewLifecycleOwner,
        ) { loading ->
            if (onLoadingListener != null) {
                onLoadingListener?.invoke(loading)
            }
        }

        viewModel.confError.observe(
            viewLifecycleOwner,
        ) { confErrors ->
            displayConfigurationErrors(confErrors)
        }

        viewModel.showToast.observe(
            viewLifecycleOwner,
        ) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.focused.observe(
            viewLifecycleOwner,
        ) { onFocused?.invoke() }

        viewModel.showInfo.observe(
            viewLifecycleOwner,
        ) { infoUiModel ->
            showInfoDialog(infoUiModel)
        }

        viewModel.completionPercentage.observe(
            viewLifecycleOwner,
        ) { percentage ->
            completionListener?.invoke(percentage)
        }

        viewModel.calculationLoop.observe(
            viewLifecycleOwner,
        ) { displayLoopWarning ->
            if (displayLoopWarning) {
                showLoopWarning()
            }
        }
    }

    private fun showInfoDialog(infoUiModel: InfoUiModel) {
        CustomDialog(
            requireContext(),
            requireContext().getString(infoUiModel.title),
            requireContext().getString(infoUiModel.description),
            requireContext().getString(R.string.action_close),
            null,
            Constants.DESCRIPTION_DIALOG,
            null,
        ).show()
    }

    private fun showLoopWarning() {
        MaterialAlertDialogBuilder(requireContext(), R.style.DhisMaterialDialog)
            .setTitle(getString(R.string.program_rules_loop_warning_title))
            .setMessage(getString(R.string.program_rules_loop_warning_message))
            .setPositiveButton(R.string.action_accept) { _, _ -> }
            .setCancelable(false)
            .show()
    }

    private fun uiEventHandler(uiEvent: RecyclerViewUiEvents) {
        when (uiEvent) {
            is RecyclerViewUiEvents.RequestLocationByMap -> requestLocationByMap(uiEvent)
            is RecyclerViewUiEvents.DisplayQRCode -> displayQRImage(uiEvent)
            is RecyclerViewUiEvents.ScanQRCode -> requestQRScan(uiEvent)
            is RecyclerViewUiEvents.OpenOrgUnitDialog -> showOrgUnitDialog(uiEvent)
            is RecyclerViewUiEvents.OpenFile -> openFile(uiEvent)
            is RecyclerViewUiEvents.OpenChooserIntent -> openChooserIntent(uiEvent)
            is RecyclerViewUiEvents.SelectPeriod -> showPeriodDialog(uiEvent)
            is RecyclerViewUiEvents.LaunchCustomIntent -> launchCustomIntent(uiEvent)
        }
    }

    private fun launchCustomIntent(uiEvent: RecyclerViewUiEvents.LaunchCustomIntent) {
        uiEvent.customIntent?.let {
            val updatedRequestParams = viewModel.getCustomIntentRequestParams(it.uid)
            val updatedCustomIntent = uiEvent.customIntent.copy(customIntentRequest = updatedRequestParams)
            val intent = FormIntent.OnFieldLoadingData(uiEvent.uid)
            intentHandler(intent)
            openCustomIntentLauncher.launch(
                CustomIntentInput(
                    fieldUid = uiEvent.uid,
                    customIntent = updatedCustomIntent,
                    defaultTitle = resources.getString(R.string.select_app_intent),
                ),
            )
        }
    }

    private fun showPeriodDialog(uiEvent: RecyclerViewUiEvents.SelectPeriod) {
        BottomSheetDialog(
            bottomSheetDialogUiModel =
                BottomSheetDialogUiModel(
                    title = uiEvent.title,
                    iconResource = -1,
                ),
            onSecondaryButtonClicked = {
            },
            onMainButtonClicked = { _ ->
            },
            showTopDivider = true,
            showBottomDivider = true,
            content = { bottomSheetDialog, scrollState ->
                val periods = viewModel.fetchPeriods().collectAsLazyPagingItems()
                PeriodSelectorContent(
                    periods = periods,
                    scrollState = scrollState,
                ) { period ->
                    val dateString = DateUtils.oldUiDateFormat().format(period.startDate)
                    intentHandler(
                        FormIntent.OnSave(
                            uiEvent.uid,
                            dateString,
                            ValueType.DATE,
                        ),
                    )
                    bottomSheetDialog.dismiss()
                }
            },
        ).show(childFragmentManager, AlertBottomDialog::class.java.simpleName)
    }

    private fun openChooserIntent(uiEvent: RecyclerViewUiEvents.OpenChooserIntent) {
        val currentValue = viewModel.getUpdatedData(uiEvent)
        if (currentValue.error != null) {
            intentHandler(
                FormIntent.OnSave(
                    uiEvent.uid,
                    currentValue.value,
                    currentValue.valueType,
                ),
            )
        } else if (actionIconsActivate && !currentValue.value.isNullOrEmpty()) {
            view?.closeKeyboard()
            val intent =
                Intent(uiEvent.action).apply {
                    when (uiEvent.action) {
                        Intent.ACTION_DIAL -> {
                            data = "tel:${currentValue.value}".toUri()
                        }

                        Intent.ACTION_SENDTO -> {
                            data = "mailto:${currentValue.value}".toUri()
                        }

                        Intent.ACTION_VIEW -> {
                            data =
                                if (!currentValue.value.startsWith("http://") &&
                                    !currentValue.value.startsWith(
                                        "https://",
                                    )
                                ) {
                                    "http://${currentValue.value}".toUri()
                                } else {
                                    currentValue.value.toUri()
                                }
                        }

                        Intent.ACTION_SEND -> {
                            val contentUri =
                                FileProvider.getUriForFile(
                                    requireContext(),
                                    FormFileProvider.fileProviderAuthority,
                                    File(currentValue.value),
                                )
                            setDataAndType(
                                contentUri,
                                requireContext().contentResolver.getType(contentUri),
                            )
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            putExtra(Intent.EXTRA_STREAM, contentUri)
                        }
                    }
                }

            val title = resources.getString(R.string.open_with)
            val chooser = Intent.createChooser(intent, title)

            try {
                startActivity(chooser)
            } catch (_: ActivityNotFoundException) {
                Timber.e("No activity found that can handle this action")
            }
        }
    }

    private fun render(items: List<FieldUiModel>) {
        viewModel.calculateCompletedFields()
        viewModel.updateConfigurationErrors()
        viewModel.displayLoopWarningIfNeeded()
        viewModel.onItemsRendered()
        onFieldItemsRendered?.invoke(items.isEmpty())
    }

    private fun intentHandler(intent: FormIntent) {
        viewModel.submitIntent(intent)
    }

    private fun requestLocationByMap(event: RecyclerViewUiEvents.RequestLocationByMap) {
        onActivityForResult?.invoke()
        mapContent.launch(
            MapSelectorActivity
                .create(requireContext(), event.uid, event.featureType, event.value, programUid),
        )
    }

    private fun requestQRScan(event: RecyclerViewUiEvents.ScanQRCode) {
        viewModel.clearFocus()
        onActivityForResult?.invoke()
        val valueTypeRenderingType: ValueTypeRenderingType =
            event.renderingType.let {
                when (it) {
                    UiRenderType.QR_CODE -> ValueTypeRenderingType.QR_CODE
                    UiRenderType.BAR_CODE -> ValueTypeRenderingType.BAR_CODE
                    else -> ValueTypeRenderingType.DEFAULT
                }
            }

        qrScanContent.launch(
            ScanOptions().apply {
                setDesiredBarcodeFormats()
                setPrompt("")
                setBeepEnabled(true)
                setBarcodeImageEnabled(false)
                addExtra(Constants.UID, event.uid)
                event.optionSet?.let { addExtra(Constants.OPTION_SET, event.optionSet) }
                addExtra(Constants.SCAN_RENDERING_TYPE, valueTypeRenderingType)
            },
        )
    }

    private fun openFile(event: RecyclerViewUiEvents.OpenFile) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            downloadFile(event.field.displayName)
        } else {
            viewModel.filePath = event.field.displayName
            requestStoragePermissions.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun downloadFile(fileName: String?) {
        fileName?.let { filePath ->
            fileHandler.copyAndOpen(File(filePath)) {
                Toast
                    .makeText(
                        requireContext(),
                        getString(R.string.file_downloaded),
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        }
    }

    private fun displayQRImage(event: RecyclerViewUiEvents.DisplayQRCode) {
        viewModel.clearFocus()
        QRDetailBottomDialog(
            event.label,
            event.value,
            event.renderingType,
            event.editable,
        ) {
            requestQRScan(
                RecyclerViewUiEvents.ScanQRCode(
                    event.uid,
                    event.optionSet,
                    event.renderingType,
                ),
            )
        }.show(
            childFragmentManager,
            QRDetailBottomDialog.TAG,
        )
    }

    private fun showOrgUnitDialog(uiEvent: RecyclerViewUiEvents.OpenOrgUnitDialog) {
        OUTreeFragment
            .Builder()
            .withPreselectedOrgUnits(
                uiEvent.value?.let { listOf(it) } ?: emptyList(),
            ).singleSelection()
            .onSelection { selectedOrgUnits ->
                intentHandler(
                    FormIntent.OnSave(
                        uiEvent.uid,
                        selectedOrgUnits.firstOrNull()?.uid(),
                        ValueType.ORGANISATION_UNIT,
                    ),
                )
            }.orgUnitScope(uiEvent.orgUnitSelectorScope ?: OrgUnitSelectorScope.UserSearchScope())
            .build()
            .show(childFragmentManager, uiEvent.label)
    }

    private fun displayConfigurationErrors(configurationError: List<RulesUtilsProviderConfigurationError>) {
        if (displayConfErrors && configurationError.isNotEmpty()) {
            MaterialAlertDialogBuilder(requireContext(), R.style.DhisMaterialDialog)
                .setTitle(R.string.warning_error_on_complete_title)
                .setMessage(configurationError.toMessage(requireContext()))
                .setPositiveButton(
                    R.string.action_close,
                ) { _, _ -> }
                .setNegativeButton(
                    getString(R.string.action_do_not_show_again),
                ) { _, _ -> displayConfErrors = false }
                .setCancelable(false)
                .show()
        }
    }

    fun onBackPressed() {
        viewModel.runDataIntegrityCheck(backButtonPressed = true)
    }

    fun onSaveClick() {
        viewModel.saveDataEntry()
    }

    fun reload() {
        viewModel.loadData()
    }

    internal fun setConfiguration(
        locationProvider: LocationProvider?,
        completionListener: ((percentage: Float) -> Unit)?,
        actionIconsActivate: Boolean,
        openErrorLocation: Boolean,
        programUid: String?,
    ) {
        this.locationProvider = locationProvider
        this.completionListener = completionListener
        this.actionIconsActivate = actionIconsActivate
        this.openErrorLocation = openErrorLocation
        this.programUid = programUid
    }

    internal fun setCallbackConfiguration(
        onItemChangeListener: ((action: RowAction) -> Unit)?,
        onLoadingListener: ((loading: Boolean) -> Unit)?,
        onFocused: (() -> Unit)?,
        onFinishDataEntry: (() -> Unit)?,
        onActivityForResult: (() -> Unit)?,
        onFieldItemsRendered: ((fieldsEmpty: Boolean) -> Unit)?,
    ) {
        this.onItemChangeListener = onItemChangeListener
        this.onLoadingListener = onLoadingListener
        this.onFocused = onFocused
        this.onFinishDataEntry = onFinishDataEntry
        this.onActivityForResult = onActivityForResult
        this.onFieldItemsRendered = onFieldItemsRendered
    }

    class Builder {
        private var records: FormRepositoryRecords? = null
        private var fragmentManager: FragmentManager? = null
        private var onItemChangeListener: ((action: RowAction) -> Unit)? = null
        private var locationProvider: LocationProvider? = null
        private var onLoadingListener: ((loading: Boolean) -> Unit)? = null
        private var onFocused: (() -> Unit)? = null
        private var onActivityForResult: (() -> Unit)? = null
        private var onFinishDataEntry: (() -> Unit)? = null
        private var onPercentageUpdate: ((percentage: Float) -> Unit)? = null
        private var onFieldItemsRendered: ((fieldsEmpty: Boolean) -> Unit)? = null
        private var actionIconsActive: Boolean = true
        private var openErrorLocation: Boolean = false
        private var programUid: String? = null

        /**
         * If you want to handle the behaviour of the form and be notified when any item is updated,
         * implement this listener.
         */
        fun onItemChangeListener(callback: (action: RowAction) -> Unit) = apply { this.onItemChangeListener = callback }

        /**
         *
         */
        fun locationProvider(locationProvider: LocationProvider) = apply { this.locationProvider = locationProvider }

        /**
         * Sets if loading started or finished to handle loadingfeedback
         * */
        fun onLoadingListener(callback: (loading: Boolean) -> Unit) = apply { this.onLoadingListener = callback }

        /**
         * It's triggered when form gets focus
         */
        fun onFocused(callback: () -> Unit) = apply { this.onFocused = callback }

        /**
         * Set a FragmentManager for instantiating the form view
         * */
        fun factory(manager: FragmentManager) = apply { fragmentManager = manager }

        fun onFinishDataEntry(callback: () -> Unit) = apply { this.onFinishDataEntry = callback }

        fun onPercentageUpdate(callback: (percentage: Float) -> Unit) = apply { this.onPercentageUpdate = callback }

        fun setRecords(records: FormRepositoryRecords) = apply { this.records = records }

        fun openErrorLocation(openErrorLocation: Boolean) = apply { this.openErrorLocation = openErrorLocation }

        fun setProgramUid(programUid: String?) = apply { this.programUid = programUid }

        fun build(): FormView {
            if (fragmentManager == null) {
                throw Exception("You need to call factory method and pass a FragmentManager")
            }
            if (records == null) {
                throw Exception("You need to set record information in order to persist your data")
            }
            fragmentManager!!.fragmentFactory =
                FormViewFragmentFactory(
                    locationProvider,
                    onItemChangeListener,
                    onLoadingListener,
                    onFocused,
                    onFinishDataEntry,
                    onActivityForResult,
                    onPercentageUpdate,
                    onFieldItemsRendered,
                    actionIconsActive,
                    openErrorLocation,
                    programUid,
                )

            val fragment =
                fragmentManager!!.fragmentFactory.instantiate(
                    this.javaClass.classLoader!!,
                    FormView::class.java.name,
                ) as FormView

            val bundle =
                Bundle().apply {
                    putSerializable(RECORDS, records)
                }
            fragment.arguments = bundle
            return fragment
        }
    }

    companion object {
        const val RECORDS = "RECORDS"
        const val TEMP_FILE = "tempFile.png"
    }
}
