package org.dhis2.data.forms.dataentry.fields.visualOptionSet

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.core.view.updatePadding
import org.dhis2.Bindings.dp
import org.dhis2.data.forms.dataentry.fields.image.ImageCustomView
import org.dhis2.utils.customviews.FieldLayout
import org.hisp.dhis.android.core.option.Option

class VisualOptionSetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FieldLayout(context, attrs, defStyle) {

    private var viewModel: MatrixOptionSetModel? = null
    private var parentHeight = 0
    private var parentWidth = 0

    init {
        init(context)
    }

    fun setMatrixViewModel(viewModel: MatrixOptionSetModel) {
        this.viewModel = viewModel
        setLayoutData()
    }

    private fun setLayoutData() {
        post {
            removeAllViews()
            createLabel()
            addView(
                FrameLayout(context).apply {
                    layoutParams =
                        LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        ).apply {
                            topMargin = 36.dp
                        }
                    addOptionImages(this)
                }
            )
            createWarningErrorMessage()
        }
    }

    private fun createLabel() {
        addView(
            TextView(context).apply {
                tag = viewModel?.labelTag()
                layoutParams =
                    LayoutParams(LayoutParams.MATCH_PARENT, 36.dp)
                text = viewModel?.formattedLabel
                updatePadding(left = 16.dp)
            }
        )
    }

    private fun createWarningErrorMessage() {
        viewModel?.takeIf {
            it.warning() != null || it.error() != null
        }?.let {
            val labelView = findViewWithTag<TextView>(viewModel?.labelTag())
            addView(
                TextView(context).apply {
                    setTextAppearance(context, it.errorAppearance)
                    text = it.errorMessage
                    layoutParams =
                        LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                            addRule(RelativeLayout.BELOW, labelView.id)
                            setPadding(16.dp)
                        }
                }
            )
        }
    }

    private fun addOptionImages(frameLayout: FrameLayout) {
        viewModel?.let {
            it.optionsToShow().forEachIndexed { index, option ->
                frameLayout.addView(
                    createOptionImage(index, option, it.numberOfColumns())
                )
            }
        }
    }

    private fun createOptionImage(
        index: Int,
        option: Option,
        numberOfColumns: Int
    ): View {
        return ImageCustomView(context).apply {
            tag = viewModel?.optionTag(option)
            layoutParams =
                FrameLayout.LayoutParams(parentWidth / numberOfColumns, 500).apply {
                    topMargin = (index / numberOfColumns) * 500
                    marginStart =
                        (index % numberOfColumns) * (parentWidth / numberOfColumns)
                }
            viewModel?.let { setViewModel(it, option) }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        parentHeight = MeasureSpec.getSize(heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}
