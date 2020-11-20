package org.dhis2.utils.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.View
import org.dhis2.Bindings.Bindings
import org.dhis2.data.forms.dataentry.fields.image.ImageViewModel
import org.dhis2.databinding.FormImageBinding
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType

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
        }

        var height: Int?
        val parentHeight: Int = this.height

        viewModel.renderingType.let {
            height = when (it) {
                ProgramStageSectionRenderingType.SEQUENTIAL.name -> {
                    150
                    // height = parentHeight / if (totalFields > 2) 3 else totalFields
                }
                ProgramStageSectionRenderingType.MATRIX.name -> {
                    150
                    // height = parentHeight / (totalFields / 2 + 1)
                }
                else -> -1
            }
        }

        height?.let {
            val rootView = binding.root
            val layoutParams = rootView.layoutParams
            layoutParams.height = it
            rootView.layoutParams = layoutParams
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
