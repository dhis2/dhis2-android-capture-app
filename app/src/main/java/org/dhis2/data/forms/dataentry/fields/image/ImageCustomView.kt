package org.dhis2.data.forms.dataentry.fields.image

import android.content.Context
import android.util.AttributeSet
import org.dhis2.Bindings.Bindings
import org.dhis2.data.forms.dataentry.fields.visualOptionSet.MatrixOptionSetModel
import org.dhis2.databinding.FormImageBinding
import org.dhis2.utils.customviews.FieldLayout
import org.hisp.dhis.android.core.option.Option

class ImageCustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FieldLayout(context, attrs, defStyle) {

    private lateinit var binding: FormImageBinding

    init {
        init(context)
    }

    fun setLayout(
        viewModel: MatrixOptionSetModel,
        option: Option
    ) {
        binding = FormImageBinding.inflate(inflater, this, true).apply {
            this.viewModel = viewModel
            this.option = option
        }
    }

    fun setViewModel(viewModel: MatrixOptionSetModel, option: Option) {
        setLayout(viewModel, option)
        option.apply {
            Bindings.setObjectStyle(binding.icon, this@ImageCustomView, style())
            Bindings.setObjectStyle(binding.label, this@ImageCustomView, style())
        }
    }
}
