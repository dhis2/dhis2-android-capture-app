package org.dhis2.data.forms.dataentry.fields.scan

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.dhis2.BR
import org.dhis2.Bindings.closeKeyboard
import org.dhis2.R
import org.dhis2.databinding.ScanTextViewAccentBinding
import org.dhis2.databinding.ScanTextViewBinding
import org.dhis2.usescases.qrScanner.ScanActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.utils.ActivityResultObservable
import org.dhis2.utils.ActivityResultObserver
import org.dhis2.utils.ColorUtils
import org.dhis2.utils.Constants
import org.dhis2.utils.Constants.EXTRA_DATA
import org.dhis2.utils.Constants.RQ_QR_SCANNER
import org.dhis2.utils.Preconditions.Companion.equals
import org.dhis2.utils.customviews.CustomDialog
import org.dhis2.utils.customviews.FieldLayout
import org.hisp.dhis.android.core.common.ValueTypeRenderingType

class ScanTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FieldLayout(context, attrs, defStyle), ActivityResultObserver {

    private lateinit var iconView: ImageView
    private lateinit var editText: TextInputEditText
    private lateinit var inputLayout: TextInputLayout
    private lateinit var descriptionLabel: ImageView
    private lateinit var binding: ViewDataBinding
    private lateinit var qrIcon: ImageView
    private lateinit var labelText: TextView
    private lateinit var delete: ImageView
    var optionSet: String? = null
    private var renderingType: ValueTypeRenderingType? = null
    private lateinit var viewModel: ScanTextViewModel

    init {
        init(context)
    }

    private fun setLayoutData(isBgTransparent: Boolean) {
        if (!::binding.isInitialized) {
            binding = when {
                isBgTransparent -> ScanTextViewBinding.inflate(inflater, this, true)
                else -> ScanTextViewAccentBinding.inflate(inflater, this, true)
            }
        }

        this.editText = binding.root.findViewById(R.id.input_editText)
        this.qrIcon = binding.root.findViewById(R.id.descIcon)
        this.iconView = binding.root.findViewById(R.id.renderImage)
        this.inputLayout = binding.root.findViewById(R.id.input_layout)
        this.descriptionLabel = binding.root.findViewById(R.id.descriptionLabel)
        this.labelText = binding.root.findViewById(R.id.label)
        this.delete = binding.root.findViewById(R.id.delete)

        qrIcon.setOnClickListener {
            viewModel.onItemClick()
            goToScanActivity()
        }

        editText.setOnTouchListener { _, event ->
            if (MotionEvent.ACTION_UP == event.action) {
                viewModel.onItemClick()
            }
            return@setOnTouchListener false
        }

        editText.setOnFocusChangeListener { _, _ ->
            if (valueHasChanged()) {
                viewModel.onScanSelected(editText.text.toString())
            }
        }

        editText.setOnEditorActionListener { v, actionId, _ ->
            when (actionId) {
                IME_ACTION_NEXT -> {
                    viewModel.onNext()
                    return@setOnEditorActionListener true
                }
                IME_ACTION_DONE -> {
                    v.closeKeyboard()
                    return@setOnEditorActionListener true
                }
                else -> return@setOnEditorActionListener false
            }
        }

        delete.setOnClickListener {
            setText(null)
            viewModel.onScanSelected(null)
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onTextChange(text.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun goToScanActivity() {
        subscribe()
        val intent = Intent(context, ScanActivity::class.java)
        intent.putExtra(Constants.OPTION_SET, optionSet)
        intent.putExtra(Constants.SCAN_RENDERING_TYPE, renderingType)
        if (context is SearchTEActivity) {
            (context as SearchTEActivity).initSearchNeeded = false
        }
        (context as FragmentActivity).startActivityForResult(intent, RQ_QR_SCANNER)
    }

    private fun updateScanResult(result: String?) {
        setText(result)
        viewModel.onScanSelected(result)
    }

    private fun updateEditable(isEditable: Boolean) {
        editText.isEnabled = isEditable
        editText.isFocusable = true
        editText.isClickable = isEditable
        qrIcon.isClickable = isEditable
        when {
            !isEditable -> delete.visibility = View.GONE
        }

        setEditable(
            isEditable,
            editText,
            labelText,
            descriptionLabel,
            qrIcon
        )
    }

    fun setText(text: String?) {
        text?.let {
            editText.setText(it)
        } ?: editText.text?.clear()

        editText.setSelection(editText.text?.length ?: 0)
        delete.visibility = when (text) {
            null -> View.GONE
            else -> View.VISIBLE
        }
    }

    fun setHint(hint: String?) {
        inputLayout.hint = hint
    }

    private fun setAlert(warning: String?, error: String?) {
        inputLayout.error = error.also {
            inputLayout.setErrorTextAppearance(R.style.error_appearance)
        } ?: warning.also {
            inputLayout.setErrorTextAppearance(R.style.warning_appearance)
        }
    }

    fun setLabel(label: String) {
        if (inputLayout.hint == null || inputLayout.hint!!.toString() != label) {
            this.label = label
            inputLayout.hint = this.label
            binding.setVariable(BR.label, label)
        }
    }

    fun setDescription(description: String?) {
        descriptionLabel.visibility =
            when {
                description != null -> View.VISIBLE
                else -> View.GONE
            }
        descriptionLabel.setOnClickListener { view ->
            CustomDialog(
                context,
                label,
                description ?: context.getString(R.string.empty_description),
                context.getString(R.string.action_close),
                null,
                Constants.DESCRIPTION_DIALOG,
                null
            ).show()
        }
    }

    private fun setRenderingType(type: ValueTypeRenderingType?) {
        renderingType = type
        when (type) {
            ValueTypeRenderingType.BAR_CODE -> {
                qrIcon.setImageResource(R.drawable.ic_form_barcode)
            }
            ValueTypeRenderingType.QR_CODE -> {
                qrIcon.setImageResource(R.drawable.ic_form_qr)
            }
            else -> {
            }
        }
    }

    override fun dispatchSetActivated(activated: Boolean) {
        super.dispatchSetActivated(activated)
        labelText.setTextColor(
            when {
                activated -> ColorUtils.getPrimaryColor(
                    context,
                    ColorUtils.ColorType.PRIMARY
                )
                else -> ResourcesCompat.getColor(
                    resources,
                    R.color.textPrimary,
                    null
                )
            }
        )
    }

    fun setViewModel(viewModel: ScanTextViewModel) {
        this.viewModel = viewModel
        setLayoutData(viewModel.isBackgroundTransparent())

        viewModel.apply {
            setText(value())
            setRenderingType(fieldRendering?.type())
            setLabel(formattedLabel)
            setHint(hint)
            setDescription(description())
            setAlert(warning(), error())
            updateEditable(editable() ?: false)
            optionSet = optionSet()
            binding.setVariable(BR.focus, activated())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == RQ_QR_SCANNER) {
            updateScanResult(data!!.getStringExtra(EXTRA_DATA))
        }
    }

    private fun subscribe() {
        (context as ActivityResultObservable).subscribe(this)
    }

    private fun valueHasChanged(): Boolean {
        return !equals(
            if (TextUtils.isEmpty(editText.text)) "" else editText.text.toString(),
            if (viewModel.value() == null) "" else viewModel.value().toString()
        ) || viewModel.error() != null
    }
}
