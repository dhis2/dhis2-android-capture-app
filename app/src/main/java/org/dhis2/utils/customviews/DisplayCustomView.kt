package org.dhis2.utils.customviews

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import org.dhis2.BR
import org.dhis2.data.forms.dataentry.fields.display.DisplayViewModel
import org.dhis2.databinding.ItemIndicatorBinding

class DisplayCustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FieldLayout(context, attrs, defStyle) {

    private lateinit var binding: ItemIndicatorBinding
    private lateinit var viewModel: DisplayViewModel
    private lateinit var params: ConstraintLayout.LayoutParams

    init {
        init(context)
    }

    fun setLayout() {
        binding = ItemIndicatorBinding.inflate(inflater, this, true)
    }

    fun setLabel(label: String) {
        params = binding.guideline.layoutParams as ConstraintLayout.LayoutParams
        if (viewModel.label().isEmpty()) {
            params.guidePercent = 0f
        } else {
            params.guidePercent = 0.6f
            binding.setVariable(BR.label, label)
        }
        binding.guideline.layoutParams = params
    }

    fun setViewModel(viewModel: DisplayViewModel) {
        this.viewModel = viewModel
        setLabel(viewModel.formattedLabel)

        // setFormFieldBackground()
    }
}
