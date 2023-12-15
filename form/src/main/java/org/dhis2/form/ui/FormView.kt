@file:OptIn(ExperimentalFoundationApi::class)

package org.dhis2.form.ui

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat.CLOCK_12H
import com.google.android.material.timepicker.TimeFormat.CLOCK_24H
import com.journeyapps.barcodescanner.ScanOptions
import org.dhis2.commons.ActivityResultObservable
import org.dhis2.commons.ActivityResultObserver
import org.dhis2.commons.Constants
import org.dhis2.commons.bindings.getFileFrom
import org.dhis2.commons.bindings.getFileFromGallery
import org.dhis2.commons.bindings.rotateImage
import org.dhis2.commons.dialogs.AlertBottomDialog
import org.dhis2.commons.dialogs.CustomDialog
import org.dhis2.commons.dialogs.calendarpicker.CalendarPicker
import org.dhis2.commons.dialogs.calendarpicker.OnDatePickerListener
import org.dhis2.commons.dialogs.imagedetail.ImageDetailBottomDialog
import org.dhis2.commons.extensions.closeKeyboard
import org.dhis2.commons.extensions.serializable
import org.dhis2.commons.extensions.truncate
import org.dhis2.commons.locationprovider.LocationProvider
import org.dhis2.commons.locationprovider.LocationSettingLauncher
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.dhis2.form.R
import org.dhis2.form.data.DataIntegrityCheckResult
import org.dhis2.form.data.FormFileProvider
import org.dhis2.form.data.RulesUtilsProviderConfigurationError
import org.dhis2.form.data.SuccessfulResult
import org.dhis2.form.data.scan.ScanContract
import org.dhis2.form.data.toMessage
import org.dhis2.form.databinding.ViewFormBinding
import org.dhis2.form.di.Injector
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FormRepositoryRecords
import org.dhis2.form.model.InfoUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.model.exception.RepositoryRecordsException
import org.dhis2.form.ui.dialog.OptionSetDialog
import org.dhis2.form.ui.dialog.QRDetailBottomDialog
import org.dhis2.form.ui.event.DialogDelegate
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.idling.FormCountingIdlingResource
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.form.ui.mapper.FormSectionMapper
import org.dhis2.form.ui.provider.EnrollmentResultDialogUiProvider
import org.dhis2.maps.views.MapSelectorActivity
import org.dhis2.maps.views.MapSelectorActivity.Companion.DATA_EXTRA
import org.dhis2.maps.views.MapSelectorActivity.Companion.FIELD_UID
import org.dhis2.maps.views.MapSelectorActivity.Companion.LOCATION_TYPE_EXTRA
import org.dhis2.ui.ErrorFieldList
import org.dhis2.ui.dialogs.bottomsheet.BottomSheetDialog
import org.dhis2.ui.dialogs.signature.SignatureDialog
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType
import timber.log.Timber
import java.io.File
import java.util.Calendar

class FormView : Fragment() {

    private var onItemChangeListener: ((action: RowAction) -> Unit)? = null
    private var locationProvider: LocationProvider? = null
    private var onLoadingListener: ((loading: Boolean) -> Unit)? = null
    private var onFocused: (() -> Unit)? = null
    private var onFinishDataEntry: (() -> Unit)? = null
    private var onActivityForResult: (() -> Unit)? = null
    private var needToForceUpdate: Boolean = false
    private var completionListener: ((percentage: Float) -> Unit)? = null
    private var onDataIntegrityCheck: ((result: DataIntegrityCheckResult) -> Unit)? = null
    private var onFieldItemsRendered: ((fieldsEmpty: Boolean) -> Unit)? = null
    private var resultDialogUiProvider: EnrollmentResultDialogUiProvider? = null
    private var actionIconsActivate: Boolean = true
    private var openErrorLocation: Boolean = false
    private var useCompose = false

    val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // Not needed
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            viewModel.items.value?.find { it.focused }?.onTextChange(p0)
        }

        override fun afterTextChanged(p0: Editable?) {
            // Not needed
        }
    }

    private val qrScanContent = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let { qrData ->
            val intent = FormIntent.OnSave(
                result.originalIntent.getStringExtra(Constants.UID)!!,
                qrData,
                ValueType.TEXT,
            )
            intentHandler(intent)
        }
    }

    private val mapContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data?.extras != null
            ) {
                val uid = it.data?.getStringExtra(FIELD_UID)
                val featureType = it.data?.getStringExtra(LOCATION_TYPE_EXTRA)
                val coordinates = it.data?.getStringExtra(DATA_EXTRA)
                if (uid != null && featureType != null) {
                    val intent = FormIntent.SelectLocationFromMap(
                        uid,
                        featureType,
                        coordinates,
                    )
                    intentHandler(intent)
                }
            }
        }

    private val requestCameraPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.values.all { isGranted -> isGranted }) {
                showAddImageOptions()
                (context as ActivityResultObservable?)?.subscribe(object : ActivityResultObserver {
                    override fun onActivityResult(
                        requestCode: Int,
                        resultCode: Int,
                        data: Intent?,
                    ) {
                        if (resultCode != RESULT_OK) {
                            showAddImageOptions()
                        }
                    }

                    override fun onRequestPermissionsResult(
                        requestCode: Int,
                        permissions: Array<String?>,
                        grantResults: IntArray,
                    ) {
                        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            showAddImageOptions()
                        }
                    }
                })
            } else {
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.camera_permission_denied),
                    Toast.LENGTH_LONG,
                ).show()
            }
        }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val imageFile = File(
                    FileResourceDirectoryHelper.getFileResourceDirectory(requireContext()),
                    TEMP_FILE,
                ).rotateImage(requireContext())
                onSavePicture?.invoke(imageFile.path)

                viewModel.getFocusedItemUid()?.let {
                    viewModel.submitIntent(FormIntent.OnAddImageFinished(it))
                }
            } else {
                viewModel.getFocusedItemUid()?.let {
                    viewModel.submitIntent(FormIntent.OnAddImageFinished(it))
                }
            }
        }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK) {
                getFileFromGallery(requireContext(), activityResult.data?.data)?.also { file ->
                    onSavePicture?.invoke(file.path)
                }
                viewModel.getFocusedItemUid()?.let {
                    viewModel.submitIntent(FormIntent.OnAddImageFinished(it))
                }
            } else {
                viewModel.getFocusedItemUid()?.let {
                    viewModel.submitIntent(FormIntent.OnAddImageFinished(it))
                }
            }
        }

    private val pickFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                getFileFrom(requireContext(), uri)?.also { file ->
                    onSavePicture?.invoke(file.path)
                }
                viewModel.getFocusedItemUid()?.let {
                    viewModel.submitIntent(FormIntent.OnAddImageFinished(it))
                }
            } else {
                viewModel.getFocusedItemUid()?.let {
                    viewModel.submitIntent(FormIntent.OnAddImageFinished(it))
                }
            }
        }

    private val requestLocationPermissions =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { result ->
            if (result.values.all { isGranted -> isGranted }) {
                viewModel.getFocusedItemUid()?.let {
                    requestCurrentLocation(RecyclerViewUiEvents.RequestCurrentLocation(it))
                }
            } else {
                displayCoordinatesPermissionDeclined()
            }
        }

    private val permissionSettings =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) {
            val result = requireActivity().checkCallingOrSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
            if (result == PackageManager.PERMISSION_GRANTED) {
                viewModel.getFocusedItemUid()?.let {
                    requestCurrentLocation(RecyclerViewUiEvents.RequestCurrentLocation(it))
                }
            } else {
                viewModel.getFocusedItemUid()?.let {
                    viewModel.submitIntent(FormIntent.OnCancelRequestCoordinates(it))
                }
            }
        }

    private val locationDisabledSettings =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (locationProvider?.hasLocationEnabled() == true) {
                viewModel.getFocusedItemUid()?.let {
                    requestCurrentLocation(RecyclerViewUiEvents.RequestCurrentLocation(it))
                }
            } else {
                viewModel.getFocusedItemUid()?.let {
                    viewModel.submitIntent(FormIntent.OnCancelRequestCoordinates(it))
                }
            }
        }

    private val viewModel: FormViewModel by viewModels {
        Injector.provideFormViewModelFactory(
            context = requireContext(),
            repositoryRecords = arguments?.serializable(RECORDS)
                ?: throw RepositoryRecordsException(),
            openErrorLocation = openErrorLocation,
            useCompose = useCompose,
        )
    }

    private lateinit var binding: ViewFormBinding
    private lateinit var dataEntryHeaderHelper: DataEntryHeaderHelper
    private lateinit var adapter: DataEntryAdapter
    private lateinit var alertDialogView: View
    private lateinit var dialogDelegate: DialogDelegate
    private lateinit var formSectionMapper: FormSectionMapper
    var scrollCallback: ((Boolean) -> Unit)? = null
    private var displayConfErrors = true
    private var onSavePicture: ((String) -> Unit)? = null

    private val storagePermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
    )

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private val storagePermissions33 = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_AUDIO,
        Manifest.permission.READ_MEDIA_VIDEO,
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val contextWrapper = ContextThemeWrapper(context, R.style.searchFormInputText)
        binding = DataBindingUtil.inflate(inflater, R.layout.view_form, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        dataEntryHeaderHelper = DataEntryHeaderHelper(binding.headerContainer, binding.recyclerView)
        dialogDelegate = DialogDelegate()
        formSectionMapper = FormSectionMapper()
        binding.recyclerView.layoutManager =
            object : LinearLayoutManager(contextWrapper, VERTICAL, false) {
                override fun onInterceptFocusSearch(focused: View, direction: Int): View {
                    return focused
                }
            }
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                dataEntryHeaderHelper.checkSectionHeader(recyclerView)
            }
        })
        if (needToForceUpdate) {
            retainInstance = true
        }
        FormFileProvider.init(contextWrapper.applicationContext)

        if (useCompose) {
            return ComposeView(requireContext()).apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
                )
                setContent {
                    val items by viewModel.items.observeAsState()
                    val sections = items?.let {
                        formSectionMapper.mapFromFieldUiModelList(it)
                    } ?: emptyList()
                    Form(
                        sections = sections,
                        intentHandler = ::intentHandler,
                        uiEventHandler = ::uiEventHandler,
                        resources = Injector.provideResourcesManager(context),
                    )
                }
            }
        } else {
            return binding.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        FormCountingIdlingResource.increment()
        dataEntryHeaderHelper.observeHeaderChanges(viewLifecycleOwner)
        adapter = DataEntryAdapter(
            needToForceUpdate,
            viewModel.areSectionCollapsable(),
        )

        binding.recyclerView.adapter = adapter

        adapter.onIntent = { intent ->
            if (intent is FormIntent.OnNext) {
                scrollToPosition(intent.position!!)
            }
            intentHandler(intent)
        }

        adapter.onRecyclerViewUiEvents = { uiEvent ->
            uiEventHandler(uiEvent)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
                val hasToShowFab = checkLastItem()
                scrollCallback?.invoke(hasToShowFab)
            }
        } else {
            binding.recyclerView.setOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val hasToShowFab = checkLastItem()
                    scrollCallback?.invoke(hasToShowFab)
                }
            })
        }

        binding.recyclerView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                view.closeKeyboard()
            }
        }

        setObservers()
    }

    private fun setObservers() {
        viewModel.savedValue.observe(
            viewLifecycleOwner,
        ) { rowAction ->
            onItemChangeListener?.let { it(rowAction) }
        }

        viewModel.queryData.observe(
            viewLifecycleOwner,
        ) { rowAction ->
            if (needToForceUpdate) {
                onItemChangeListener?.let { it(rowAction) }
            }
        }

        viewModel.items.observe(
            viewLifecycleOwner,
        ) { items ->
            FormCountingIdlingResource.decrement()
            render(items)
        }

        viewModel.loading.observe(
            viewLifecycleOwner,
        ) { loading ->
            if (onLoadingListener != null) {
                onLoadingListener?.invoke(loading)
            } else {
                if (loading) {
                    binding.progress.show()
                } else {
                    binding.progress.hide()
                }
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

        viewModel.dataIntegrityResult.observe(
            viewLifecycleOwner,
        ) { result ->
            handleDataIntegrityResult(result)
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

    private fun handleDataIntegrityResult(result: DataIntegrityCheckResult) {
        if (onDataIntegrityCheck != null) {
            onDataIntegrityCheck?.invoke(result)
        } else {
            when (result) {
                is SuccessfulResult -> onFinishDataEntry?.invoke()
                else -> showDataEntryResultDialog(result)
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

    private fun showDataEntryResultDialog(result: DataIntegrityCheckResult) {
        resultDialogUiProvider?.provideDataEntryUiModel(result)
            ?.let { (uiModel, fieldsWithIssues) ->
                BottomSheetDialog(
                    bottomSheetDialogUiModel = uiModel,
                    onSecondaryButtonClicked = {
                        if (result.allowDiscard) {
                            viewModel.discardChanges()
                        }
                        onFinishDataEntry?.invoke()
                    },
                    content = { bottomSheetDialog ->
                        ErrorFieldList(
                            fieldsWithIssues = fieldsWithIssues,
                            onItemClick = { bottomSheetDialog.dismiss() },
                        )
                    },
                ).show(childFragmentManager, AlertBottomDialog::class.java.simpleName)
            }
    }

    private fun showLoopWarning() {
        MaterialAlertDialogBuilder(requireContext(), R.style.DhisMaterialDialog)
            .setTitle(getString(R.string.program_rules_loop_warning_title))
            .setMessage(getString(R.string.program_rules_loop_warning_message))
            .setPositiveButton(R.string.action_accept) { _, _ -> }
            .setCancelable(false)
            .show()
    }

    private fun displayCoordinatesPermissionDeclined() {
        MaterialAlertDialogBuilder(requireContext(), R.style.DhisMaterialDialog)
            .setTitle(getString(R.string.info))
            .setMessage(getString(R.string.location_permission_denied))
            .setPositiveButton(R.string.action_accept) { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", requireActivity().packageName, null)
                permissionSettings.launch(intent)
            }
            .setNegativeButton(R.string.action_close) { _, _ ->
                viewModel.getFocusedItemUid()?.let {
                    viewModel.submitIntent(FormIntent.OnCancelRequestCoordinates(it))
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun scrollToPosition(position: Int) {
        val viewHolder = binding.recyclerView.findViewHolderForLayoutPosition(position + 1)
        if (viewHolder == null) {
            try {
                binding.recyclerView.smoothScrollToPosition(position + 1)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun uiEventHandler(uiEvent: RecyclerViewUiEvents) {
        when (uiEvent) {
            is RecyclerViewUiEvents.OpenCustomCalendar -> showCustomCalendar(uiEvent)
            is RecyclerViewUiEvents.OpenYearMonthDayAgeCalendar -> showYearMonthDayAgeCalendar(
                uiEvent,
            )

            is RecyclerViewUiEvents.OpenTimePicker -> showTimePicker(uiEvent)
            is RecyclerViewUiEvents.ShowDescriptionLabelDialog -> showDescriptionLabelDialog(
                uiEvent,
            )

            is RecyclerViewUiEvents.RequestCurrentLocation -> requestCurrentLocation(uiEvent)
            is RecyclerViewUiEvents.RequestLocationByMap -> requestLocationByMap(uiEvent)
            is RecyclerViewUiEvents.DisplayQRCode -> displayQRImage(uiEvent)
            is RecyclerViewUiEvents.ScanQRCode -> requestQRScan(uiEvent)
            is RecyclerViewUiEvents.OpenOrgUnitDialog -> showOrgUnitDialog(uiEvent)
            is RecyclerViewUiEvents.AddImage -> requestAddImage(uiEvent)
            is RecyclerViewUiEvents.ShowImage -> showFullPicture(uiEvent)
            is RecyclerViewUiEvents.OpenOptionSetDialog -> showOptionSetDialog(uiEvent)
            is RecyclerViewUiEvents.CopyToClipboard -> copyToClipboard(uiEvent.value)
            is RecyclerViewUiEvents.AddSignature -> showSignatureDialog(uiEvent)
            is RecyclerViewUiEvents.OpenFile -> openFile(uiEvent)
            is RecyclerViewUiEvents.OpenFileSelector -> openFileSelector(uiEvent)
            is RecyclerViewUiEvents.OpenChooserIntent -> openChooserIntent(uiEvent)
        }
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
            val intent = Intent(uiEvent.action).apply {
                when (uiEvent.action) {
                    Intent.ACTION_DIAL -> {
                        data = Uri.parse("tel:${currentValue.value}")
                    }

                    Intent.ACTION_SENDTO -> {
                        data = Uri.parse("mailto:${currentValue.value}")
                    }

                    Intent.ACTION_VIEW -> {
                        data = if (!currentValue.value.startsWith("http://") && !currentValue.value.startsWith("https://")) {
                            Uri.parse("http://${currentValue.value}")
                        } else {
                            Uri.parse(currentValue.value)
                        }
                    }
                }
            }

            val title = resources.getString(R.string.open_with)
            val chooser = Intent.createChooser(intent, title)

            try {
                startActivity(chooser)
            } catch (e: ActivityNotFoundException) {
                Timber.e("No activity found that can handle this action")
            }
        }
    }

    private fun copyToClipboard(value: String?) {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        value?.let {
            if (it.isNotEmpty()) {
                val clip = ClipData.newPlainText("copy", it)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(
                    context,
                    requireContext().getString(R.string.copied_text),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun render(items: List<FieldUiModel>) {
        viewModel.calculateCompletedFields()
        viewModel.updateConfigurationErrors()
        viewModel.displayLoopWarningIfNeeded()
        val layoutManager: LinearLayoutManager =
            binding.recyclerView.layoutManager as LinearLayoutManager
        val myFirstPositionIndex = layoutManager.findFirstVisibleItemPosition()
        val myFirstPositionView = layoutManager.findViewByPosition(myFirstPositionIndex)

        if (!useCompose) {
            handleKeyBoardOnFocusChange(items)
        }

        var offset = 0
        myFirstPositionView?.let {
            offset = it.top
        }

        adapter.swap(
            items,
        ) {
            dataEntryHeaderHelper.onItemsUpdatedCallback()
            viewModel.onItemsRendered()
            onFieldItemsRendered?.invoke(items.isEmpty())
        }
        layoutManager.scrollToPositionWithOffset(myFirstPositionIndex, offset)
        FormCountingIdlingResource.decrement()
    }

    private fun checkLastItem(): Boolean {
        val layoutManager =
            binding.recyclerView.layoutManager as LinearLayoutManager
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
        return lastVisiblePosition != -1 && (
            lastVisiblePosition == adapter.itemCount - 1 ||
                adapter.getItemViewType(lastVisiblePosition) == R.layout.form_section
            )
    }

    private fun handleKeyBoardOnFocusChange(items: List<FieldUiModel>) {
        items.firstOrNull { it.focused }?.let { fieldUiModel ->
            fieldUiModel.valueType?.let { valueType ->
                if (!viewModel.valueTypeIsTextField(valueType)) {
                    view?.closeKeyboard()
                }
            }
        }
    }

    private fun intentHandler(intent: FormIntent) {
        viewModel.submitIntent(intent)
    }

    private fun showCustomCalendar(intent: RecyclerViewUiEvents.OpenCustomCalendar) {
        val dialog = CalendarPicker(requireContext()).apply {
            setTitle(intent.label)
            setInitialDate(intent.date)
            isFutureDatesAllowed(intent.allowFutureDates)
            setListener(object : OnDatePickerListener {
                override fun onNegativeClick() {
                    intentHandler(FormIntent.ClearValue(intent.uid))
                }

                override fun onPositiveClick(datePicker: DatePicker) {
                    when (intent.isDateTime) {
                        true -> uiEventHandler(
                            dialogDelegate.handleDateTimeInput(
                                intent.uid,
                                intent.label,
                                intent.date,
                                datePicker.year,
                                datePicker.month,
                                datePicker.dayOfMonth,
                            ),
                        )

                        else -> intentHandler(
                            dialogDelegate.handleDateInput(
                                intent.uid,
                                datePicker.year,
                                datePicker.month,
                                datePicker.dayOfMonth,
                            ),
                        )
                    }
                }
            })
        }
        dialog.show()
    }

    private fun showTimePicker(intent: RecyclerViewUiEvents.OpenTimePicker) {
        val calendar = Calendar.getInstance()
        intent.date?.let { calendar.time = it }
        val is24HourFormat = DateFormat.is24HourFormat(requireContext())
        MaterialTimePicker.Builder()
            .setTheme(R.style.TimePicker)
            .setTimeFormat(CLOCK_24H.takeIf { is24HourFormat } ?: CLOCK_12H)
            .setHour(calendar[Calendar.HOUR_OF_DAY])
            .setMinute(calendar[Calendar.MINUTE])
            .setTitleText(intent.label)
            .build().apply {
                addOnPositiveButtonClickListener {
                    intentHandler(
                        dialogDelegate.handleTimeInput(
                            intent.uid,
                            if (intent.isDateTime == true) intent.date else null,
                            hour,
                            minute,
                        ),
                    )
                }
            }
            .show(childFragmentManager, "timePicker")
    }

    private fun showYearMonthDayAgeCalendar(
        intent: RecyclerViewUiEvents.OpenYearMonthDayAgeCalendar,
    ) {
        alertDialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_age, null)
        val yearPicker = alertDialogView.findViewById<TextInputEditText>(R.id.input_year)
        val monthPicker = alertDialogView.findViewById<TextInputEditText>(R.id.input_month)
        val dayPicker = alertDialogView.findViewById<TextInputEditText>(R.id.input_days)
        yearPicker.setText(intent.year.toString())
        monthPicker.setText(intent.month.toString())
        dayPicker.setText(intent.day.toString())

        MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialDialog)
            .setView(alertDialogView)
            .setPositiveButton(R.string.action_accept) { _, _ ->
                val dateIntent = dialogDelegate.handleYearMonthDayInput(
                    intent.uid,
                    negativeOrZero(yearPicker.text.toString()),
                    negativeOrZero(monthPicker.text.toString()),
                    negativeOrZero(dayPicker.text.toString()),
                )
                intentHandler(dateIntent)
            }
            .setNegativeButton(R.string.clear) { _, _ ->
                val clearIntent = FormIntent.ClearValue(intent.uid)
                intentHandler(clearIntent)
            }
            .create()
            .show()
    }

    private fun showDescriptionLabelDialog(
        intent: RecyclerViewUiEvents.ShowDescriptionLabelDialog,
    ) {
        CustomDialog(
            requireContext(),
            intent.title,
            intent.message ?: requireContext().getString(R.string.empty_description),
            requireContext().getString(R.string.action_close),
            null,
            Constants.DESCRIPTION_DIALOG,
            null,
        ).show()
    }

    private fun requestCurrentLocation(event: RecyclerViewUiEvents.RequestCurrentLocation) {
        locationProvider?.getLastKnownLocation(
            { location ->
                val geometry = GeometryHelper.createPointGeometry(
                    location.longitude.truncate(),
                    location.latitude.truncate(),
                )
                val intent = FormIntent.SelectLocationFromCoordinates(
                    event.uid,
                    geometry.coordinates(),
                    FeatureType.POINT.name,
                )

                intentHandler(intent)
            },
            {
                requestLocationPermissions.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                )
            },
            {
                LocationSettingLauncher.requestEnableLocationSetting(
                    requireContext(),
                    {
                        locationDisabledSettings.launch(
                            LocationSettingLauncher.locationSourceSettingIntent(),
                        )
                    },
                    {
                        viewModel.submitIntent(FormIntent.OnCancelRequestCoordinates(event.uid))
                    },
                )
            },
        )
    }

    private fun requestLocationByMap(event: RecyclerViewUiEvents.RequestLocationByMap) {
        onActivityForResult?.invoke()
        mapContent.launch(
            MapSelectorActivity.create(requireContext(), event.uid, event.featureType, event.value),
        )
    }

    private fun requestQRScan(event: RecyclerViewUiEvents.ScanQRCode) {
        viewModel.clearFocus()
        onEditionFinish()
        onActivityForResult?.invoke()
        val valueTypeRenderingType: ValueTypeRenderingType = event.renderingType.let {
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

    private fun requestAddImage(event: RecyclerViewUiEvents.AddImage) {
        onSavePicture = { picture ->
            intentHandler(
                FormIntent.OnStoreFile(
                    event.uid,
                    picture,
                    ValueType.IMAGE,
                ),
            )
        }
        requestCameraPermissions.launch(permissions())
    }

    private fun permissions() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        storagePermissions33
    } else {
        storagePermissions
    }

    private fun showAddImageOptions() {
        val options = arrayOf<CharSequence>(
            requireContext().getString(R.string.take_photo),
            requireContext().getString(R.string.from_gallery),
            requireContext().getString(R.string.cancel),
        )
        MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialDialog)
            .setTitle(requireContext().getString(R.string.select_option))
            .setOnCancelListener {
                viewModel.getFocusedItemUid()?.let {
                    viewModel.submitIntent(FormIntent.OnAddImageFinished(it))
                }
            }
            .setItems(options) { dialog: DialogInterface, item: Int ->
                run {
                    when (options[item]) {
                        requireContext().getString(R.string.take_photo) -> {
                            val photoUri = FileProvider.getUriForFile(
                                requireContext(),
                                FormFileProvider.fileProviderAuthority,
                                File(
                                    FileResourceDirectoryHelper.getFileResourceDirectory(
                                        requireContext(),
                                    ),
                                    TEMP_FILE,
                                ),
                            )
                            takePicture.launch(photoUri)
                        }

                        requireContext().getString(R.string.from_gallery) -> {
                            pickImage.launch(Intent(Intent.ACTION_PICK).apply { type = "image/*" })
                        }
                        requireContext().getString(R.string.cancel) -> {
                            viewModel.getFocusedItemUid()?.let {
                                viewModel.submitIntent(FormIntent.OnAddImageFinished(it))
                            }
                        }
                    }
                    dialog.dismiss()
                }
            }
            .show()
    }

    private fun showFullPicture(event: RecyclerViewUiEvents.ShowImage) {
        ImageDetailBottomDialog(event.label, File(event.value))
            .show(parentFragmentManager, ImageDetailBottomDialog.TAG)
    }

    private fun openFileSelector(event: RecyclerViewUiEvents.OpenFileSelector) {
        onSavePicture = { file ->
            intentHandler(
                FormIntent.OnStoreFile(
                    event.field.uid,
                    file,
                    event.field.valueType,
                ),
            )
        }
        pickFile.launch("*/*")
    }

    private fun openFile(event: RecyclerViewUiEvents.OpenFile) {
        event.field.displayName?.let { filePath ->
            val file = File(filePath)
            val fileUri = FileProvider.getUriForFile(
                requireContext(),
                FormFileProvider.fileProviderAuthority,
                file,
            )
            startActivity(
                Intent(Intent.ACTION_VIEW)
                    .setDataAndType(fileUri, "*/*")
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
            )
        }
    }

    private fun displayQRImage(event: RecyclerViewUiEvents.DisplayQRCode) {
        viewModel.clearFocus()
        onEditionFinish()
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
        OUTreeFragment.Builder()
            .showAsDialog()
            .withPreselectedOrgUnits(
                uiEvent.value?.let { listOf(it) } ?: emptyList(),
            )
            .singleSelection()
            .onSelection { selectedOrgUnits ->
                intentHandler(
                    FormIntent.OnSave(
                        uiEvent.uid,
                        selectedOrgUnits.firstOrNull()?.uid(),
                        ValueType.ORGANISATION_UNIT,
                    ),
                )
            }
            .orgUnitScope(uiEvent.orgUnitSelectorScope ?: OrgUnitSelectorScope.UserSearchScope())
            .build()
            .show(childFragmentManager, uiEvent.label)
    }

    private fun displayConfigurationErrors(
        configurationError: List<RulesUtilsProviderConfigurationError>,
    ) {
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

    private fun showOptionSetDialog(uiEvent: RecyclerViewUiEvents.OpenOptionSetDialog) {
        OptionSetDialog(
            field = uiEvent.field,
            onClearValue = {
                intentHandler(FormIntent.ClearValue(uiEvent.field.uid))
            },
        ) { code ->
            intentHandler(
                FormIntent.OnSave(
                    uiEvent.field.uid,
                    code,
                    uiEvent.field.valueType,
                ),
            )
        }.show(this@FormView.childFragmentManager)
    }

    private fun showSignatureDialog(uiEvent: RecyclerViewUiEvents.AddSignature) {
        SignatureDialog(uiEvent.label) {
            val file = File(
                FileResourceDirectoryHelper.getFileResourceDirectory(requireContext()),
                TEMP_FILE,
            )
            file.outputStream().use { out ->
                it.compress(Bitmap.CompressFormat.PNG, 85, out)
                out.flush()
            }
            intentHandler(
                FormIntent.OnStoreFile(
                    uiEvent.uid,
                    file.path,
                    ValueType.IMAGE,
                ),
            )
        }.show(this@FormView.childFragmentManager)
    }

    fun onEditionFinish() {
        binding.recyclerView.requestFocus()
    }

    private fun negativeOrZero(value: String): Int {
        return if (value.isEmpty()) 0 else -Integer.valueOf(value)
    }

    fun clearValues() {
        intentHandler(FormIntent.OnClear())
    }

    fun onBackPressed() {
        viewModel.runDataIntegrityCheck(backButtonPressed = true)
    }

    fun onSaveClick() {
        onEditionFinish()
        viewModel.saveDataEntry()
    }

    fun reload() {
        viewModel.loadData()
    }

    internal fun setConfiguration(
        locationProvider: LocationProvider?,
        needToForceUpdate: Boolean,
        completionListener: ((percentage: Float) -> Unit)?,
        resultDialogUiProvider: EnrollmentResultDialogUiProvider?,
        actionIconsActivate: Boolean,
        openErrorLocation: Boolean,
        useCompose: Boolean,
    ) {
        this.locationProvider = locationProvider
        this.needToForceUpdate = needToForceUpdate
        this.completionListener = completionListener
        this.resultDialogUiProvider = resultDialogUiProvider
        this.actionIconsActivate = actionIconsActivate
        this.openErrorLocation = openErrorLocation
        this.useCompose = useCompose
    }

    internal fun setCallbackConfiguration(
        onItemChangeListener: ((action: RowAction) -> Unit)?,
        onLoadingListener: ((loading: Boolean) -> Unit)?,
        onFocused: (() -> Unit)?,
        onFinishDataEntry: (() -> Unit)?,
        onActivityForResult: (() -> Unit)?,
        onDataIntegrityCheck: ((result: DataIntegrityCheckResult) -> Unit)?,
        onFieldItemsRendered: ((fieldsEmpty: Boolean) -> Unit)?,
    ) {
        this.onItemChangeListener = onItemChangeListener
        this.onLoadingListener = onLoadingListener
        this.onFocused = onFocused
        this.onFinishDataEntry = onFinishDataEntry
        this.onActivityForResult = onActivityForResult
        this.onDataIntegrityCheck = onDataIntegrityCheck
        this.onFieldItemsRendered = onFieldItemsRendered
    }

    class Builder {
        private var records: FormRepositoryRecords? = null
        private var fragmentManager: FragmentManager? = null
        private var onItemChangeListener: ((action: RowAction) -> Unit)? = null
        private var locationProvider: LocationProvider? = null
        private var needToForceUpdate: Boolean = false
        private var onLoadingListener: ((loading: Boolean) -> Unit)? = null
        private var onFocused: (() -> Unit)? = null
        private var onActivityForResult: (() -> Unit)? = null
        private var onFinishDataEntry: (() -> Unit)? = null
        private var onPercentageUpdate: ((percentage: Float) -> Unit)? = null
        private var onDataIntegrityCheck: ((result: DataIntegrityCheckResult) -> Unit)? = null
        private var onFieldItemsRendered: ((fieldsEmpty: Boolean) -> Unit)? = null
        private var resultDialogUiProvider: EnrollmentResultDialogUiProvider? = null
        private var actionIconsActive: Boolean = true
        private var openErrorLocation: Boolean = false
        private var useComposeForms: Boolean = false

        fun useComposeForm(useCompose: Boolean) = apply {
            this.useComposeForms = useCompose
        }

        /**
         * If you want to handle the behaviour of the form and be notified when any item is updated,
         * implement this listener.
         */
        fun onItemChangeListener(callback: (action: RowAction) -> Unit) =
            apply { this.onItemChangeListener = callback }

        /**
         *
         */
        fun locationProvider(locationProvider: LocationProvider) =
            apply { this.locationProvider = locationProvider }

        /**
         * If it's set to true, any change on the list will make update all of it's items.
         */
        fun needToForceUpdate(needToForceUpdate: Boolean) =
            apply { this.needToForceUpdate = needToForceUpdate }

        /**
         * Sets if loading started or finished to handle loadingfeedback
         * */
        fun onLoadingListener(callback: (loading: Boolean) -> Unit) =
            apply { this.onLoadingListener = callback }

        /**
         * It's triggered when form gets focus
         */
        fun onFocused(callback: () -> Unit) = apply { this.onFocused = callback }

        /**
         * Set a FragmentManager for instantiating the form view
         * */
        fun factory(manager: FragmentManager) = apply { fragmentManager = manager }

        /**
         *
         */
        fun resultDialogUiProvider(resultDialogUiProvider: EnrollmentResultDialogUiProvider) =
            apply { this.resultDialogUiProvider = resultDialogUiProvider }

        /**
         * Listener for the current activity to know if a activityForResult is called
         * */
        fun activityForResultListener(callback: () -> Unit) =
            apply { this.onActivityForResult = callback }

        fun onFinishDataEntry(callback: () -> Unit) = apply { this.onFinishDataEntry = callback }

        fun onPercentageUpdate(callback: (percentage: Float) -> Unit) =
            apply { this.onPercentageUpdate = callback }

        fun onDataIntegrityResult(callback: (result: DataIntegrityCheckResult) -> Unit) =
            apply { this.onDataIntegrityCheck = callback }

        fun onFieldItemsRendered(callback: (fieldsEmpty: Boolean) -> Unit) =
            apply { this.onFieldItemsRendered = callback }

        fun setRecords(records: FormRepositoryRecords) = apply { this.records = records }

        fun setActionIconsActivation(activate: Boolean) =
            apply { this.actionIconsActive = activate }

        fun openErrorLocation(openErrorLocation: Boolean) =
            apply { this.openErrorLocation = openErrorLocation }

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
                    needToForceUpdate,
                    onLoadingListener,
                    onFocused,
                    onFinishDataEntry,
                    onActivityForResult,
                    onPercentageUpdate,
                    onDataIntegrityCheck,
                    onFieldItemsRendered,
                    resultDialogUiProvider,
                    actionIconsActive,
                    openErrorLocation,
                    useComposeForms,
                )

            val fragment = fragmentManager!!.fragmentFactory.instantiate(
                this.javaClass.classLoader!!,
                FormView::class.java.name,
            ) as FormView

            val bundle = Bundle().apply {
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
