package org.dhis2.utils.customviews

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.scan_text_view.view.delete
import kotlinx.android.synthetic.main.scan_text_view.view.descIcon
import org.dhis2.BR
import org.dhis2.Bindings.Bindings
import org.dhis2.R
import org.dhis2.databinding.ScanTextViewAccentBinding
import org.dhis2.databinding.ScanTextViewBinding
import org.dhis2.usescases.qrScanner.ScanActivity
import org.dhis2.utils.Constants
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueTypeRenderingType

class ScanTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FieldLayout(context, attrs, defStyle) {

    private lateinit var iconView: ImageView
    private lateinit var editText: TextInputEditText
    private lateinit var inputLayout: TextInputLayout
    private lateinit var descriptionLabel: ImageView
    private lateinit var binding: ViewDataBinding
    private lateinit var onScanClick: OnScanClick
    private lateinit var onScanResult: (String?) -> Unit
    var optionSet: String? = null

    init {
        init(context)
    }

    fun setLayoutData(isBgTransparent: Boolean) {
        binding = when {
            isBgTransparent -> ScanTextViewBinding.inflate(inflater, this, true)
            else -> ScanTextViewAccentBinding.inflate(inflater, this, true)
        }

        this.editText = binding.root.findViewById(R.id.input_editText)
        this.iconView = binding.root.findViewById(R.id.renderImage)
        this.inputLayout = binding.root.findViewById(R.id.input_layout)
        this.descriptionLabel = binding.root.findViewById(R.id.descriptionLabel)

        editText.setOnClickListener {
            checkCameraPermission()
        }
        onScanClick = context as OnScanClick
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PERMISSION_GRANTED
        ) {
            val intent = Intent(context, ScanActivity::class.java)
            intent.putExtra(Constants.OPTION_SET, optionSet)
            this.onScanClick.onsScanClicked(intent, this)
        }
    }

    fun setOnScannerListener(function: (String?) -> Unit) {
        this.onScanResult = function
        delete.setOnClickListener {
            function.invoke(null)
        }
    }

    fun updateScanResult(result: String?) {
        onScanResult.invoke(result)
    }

    fun setObjectStyle(objectStyle: ObjectStyle) {
        Bindings.setObjectStyle(iconView, this, objectStyle)
    }

    fun updateEditable(isEditable: Boolean) {
        editText.isEnabled = isEditable
        editText.isFocusable = false
        editText.isClickable = isEditable
        when {
            !isEditable -> delete.visibility = View.GONE
        }
    }

    fun setText(text: String?) {
        editText.setText(text)
        editText.setSelection(editText.text?.length ?: 0)
        delete.visibility = when (text) {
            null -> View.GONE
            else -> View.VISIBLE
        }
    }

    fun setAlert(warning: String?, error: String?) {
        inputLayout.error = error.also {
            inputLayout.setErrorTextAppearance(R.style.error_appearance)
        } ?: warning.also {
            inputLayout.setErrorTextAppearance(R.style.warning_appearance)
        }
    }

    fun setLabel(label: String, mandatory: Boolean) {
        if (inputLayout.hint == null || inputLayout.hint!!.toString() != label) {
            val labelBuilder = StringBuilder(label)
            if (mandatory) {
                labelBuilder.append("*")
            }
            this.label = labelBuilder.toString()
            inputLayout.hint = this.label
            binding.setVariable(BR.label, this.label)
        }
    }

    fun setDescription(description: String?) {
        descriptionLabel.visibility =
            when {
                label.length > 16 || description != null -> View.VISIBLE
                else -> View.GONE
            }
    }

    fun setRenderingType(type: ValueTypeRenderingType?) {
        when (type) {
            ValueTypeRenderingType.BAR_CODE -> {
                descIcon.setImageResource(R.drawable.ic_form_barcode)
            }
            ValueTypeRenderingType.QR_CODE -> {
                descIcon.setImageResource(R.drawable.ic_form_qr)
            }
            else -> {}
        }
    }
    interface OnScanClick {
        fun onsScanClicked(intent: Intent, scanTextView: ScanTextView)
    }
}
