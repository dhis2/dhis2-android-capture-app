package org.dhis2.utils.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.View
import org.dhis2.Bindings.Bindings
import org.dhis2.data.forms.dataentry.fields.image.ImageViewModel
import org.dhis2.databinding.FormImageBinding

class ImageCustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FieldLayout(context, attrs, defStyle) {

    private lateinit var binding: FormImageBinding
    private lateinit var viewModel: ImageViewModel

    init {
        init(context)
    }

    fun setLayout() {
        binding = FormImageBinding.inflate(inflater, this, true)

        binding.apply {
            errorMessage.apply {
                visibility = if (viewModel.shouldShowError()) View.VISIBLE else View.GONE
                text = viewModel.errorMessage
            }
            Bindings.setObjectStyle(icon, this@ImageCustomView, viewModel.objectStyle())
            Bindings.setObjectStyle(label, this@ImageCustomView, viewModel.objectStyle())
        }
    }

    fun setViewModel(viewModel: ImageViewModel) {
        this.viewModel = viewModel
        viewModel.apply {
            Bindings.setObjectStyle(binding.icon, this@ImageCustomView, objectStyle())
            Bindings.setObjectStyle(binding.label, this@ImageCustomView, objectStyle())
        }
    }
}
