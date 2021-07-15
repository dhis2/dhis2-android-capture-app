package org.dhis2.data.forms.dataentry.fields.scan

import android.content.Context
import android.util.AttributeSet
import androidx.databinding.ViewDataBinding
import org.dhis2.BR
import org.dhis2.databinding.ScanTextViewAccentBinding
import org.dhis2.databinding.ScanTextViewBinding
import org.dhis2.utils.customviews.FieldLayout

class ScanTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FieldLayout(context, attrs, defStyle) {

    private lateinit var binding: ViewDataBinding
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
    }

    fun setViewModel(viewModel: ScanTextViewModel) {
        this.viewModel = viewModel
        binding.setVariable(BR.item, viewModel)
        setLayoutData(viewModel.isBackgroundTransparent())
    }
}
