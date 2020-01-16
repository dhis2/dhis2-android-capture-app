package org.dhis2.utils.customviews

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Handler
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.zxing.BarcodeFormat
import kotlinx.android.synthetic.main.dialog_cascade_orgunit.view.results
import kotlinx.android.synthetic.main.scan_text_view.view.delete
import kotlinx.android.synthetic.main.scan_text_view.view.descIcon
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.dhis2.BR
import org.dhis2.Bindings.Bindings
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.databinding.FormScanBinding
import org.dhis2.databinding.ScanTextViewBinding
import org.dhis2.usescases.qrScanner.QRActivity
import org.dhis2.utils.Constants
import org.dhis2.utils.DhisTextUtils.Companion.isEmpty
import org.dhis2.utils.FileResourcesUtil
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueTypeRenderingType
import java.io.File

class ScanTextView : FieldLayout {

    private lateinit var iconView: ImageView
    private lateinit var editText: TextInputEditText
    private lateinit var inputLayout: TextInputLayout
    private lateinit var descriptionLabel: ImageView
    private lateinit var scannerView: ZXingScannerView
    private lateinit var binding: ViewDataBinding


    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context)
    }

    fun setLayoutData(isBgTransparent: Boolean) {
        binding = if (isBgTransparent)
            ScanTextViewBinding.inflate(inflater, this, true)
        else
            ScanTextViewBinding.inflate(inflater, this, true)

        this.editText = binding.root.findViewById(R.id.input_editText)
        this.iconView = binding.root.findViewById(R.id.renderImage)
        this.inputLayout = binding.root.findViewById(R.id.input_layout)
        this.descriptionLabel = binding.root.findViewById(R.id.descriptionLabel)

        editText.isFocusable = false
        scannerView = ZXingScannerView(context)
        scannerView.setFormats(listOf(BarcodeFormat.QR_CODE))
        editText.setOnClickListener {
            checkCameraPermision()
        }
    }

    private fun checkCameraPermision(){
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PERMISSION_GRANTED
        ) {
            scannerView.startCamera()
        }
    }

    fun setOnScannerListener(function: (String?) -> Unit) {
        scannerView.setResultHandler { result ->
            function.invoke(result.text)
            scannerView.stopCamera()
        }
        delete.setOnClickListener {
            function.invoke(null)
        }
    }

    fun setObjectStyle(objectStyle: ObjectStyle) {
        Bindings.setObjectStyle(iconView, this, objectStyle)
    }

    fun updateEditable(isEditable: Boolean) {
        editText.isEnabled = isEditable
        editText.isFocusable = false
        editText.isClickable = isEditable
    }

    fun setText(text: String?) {
        editText.setText(text)
        editText.setSelection(editText.text?.length ?: 0)
        delete.visibility = when(text){
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
            if (mandatory)
                labelBuilder.append("*")
            this.label = labelBuilder.toString()
            inputLayout.hint = this.label
            binding.setVariable(BR.label, this.label)
        }
    }

    fun setDescription(description: String?) {
        descriptionLabel.visibility =
            if (label.length > 16 || description != null)
                View.VISIBLE
            else
                View.GONE
    }

    fun setRenderingType(type: ValueTypeRenderingType?) {
        when(type){
            ValueTypeRenderingType.BAR_CODE -> {
                descIcon.setImageResource(R.drawable.ic_form_barcode)
            }
            ValueTypeRenderingType.QR -> {
                descIcon.setImageResource(R.drawable.ic_form_qr)
            }
            else -> {}
        }
    }



}