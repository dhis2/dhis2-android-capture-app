package org.dhis2.data.forms.dataentry.fields.display

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.dhis2.R
import org.dhis2.databinding.FormDisplayBinding
import org.dhis2.utils.customviews.FieldLayout

class DisplayCustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FieldLayout(context, attrs, defStyle) {

    private val binding: FormDisplayBinding
    private lateinit var viewModel: DisplayViewModel
    private lateinit var params: ConstraintLayout.LayoutParams

    init {
        init(context)
        binding = FormDisplayBinding.inflate(inflater, this, true)
        binding.descriptionLabel.setOnClickListener {
            showDescription()
        }
    }

    fun setLabel() {
        params = binding.guideline.layoutParams as ConstraintLayout.LayoutParams
        if (viewModel.label().isEmpty()) {
            params.guidePercent = 0f
        } else {
            params.guidePercent = 0.6f
        }
        binding.guideline.layoutParams = params
    }

    fun setViewModel(viewModel: DisplayViewModel) {
        this.viewModel = viewModel
        binding.item = viewModel
        binding.colorBg = viewModel.colorBG()
        setLabel()
    }

    fun showDescription() {
        var description =  viewModel.description()

        if (viewModel.url() != null) {
            description = """
                $description
                ${viewModel.url()}
                """.trimIndent()
        }

        val dialog = MaterialAlertDialogBuilder(context, R.style.DhisMaterialDialog)
            .setMessage(description)
            .setPositiveButton(R.string.action_close) { _, _ ->
            }
        dialog.show()
    }
}
