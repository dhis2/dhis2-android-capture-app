package org.dhis2.data.forms.dataentry.fields.visualOptionSet

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
            addView(
                FrameLayout(context).apply {
                    layoutParams =
                        LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        ).apply {
                            topMargin = 16.dp
                        }
                    addOptionImages(this)
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
                FrameLayout.LayoutParams(
                    getWidth(numberOfColumns),
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = getTopMargin(index, numberOfColumns)
                    marginStart = getStartMargin(index, numberOfColumns)
                    marginEnd = getEndMargin(index, numberOfColumns)
                }
            viewModel?.let { setViewModel(it, option) }
        }
    }

    private fun getWidth(numberOfColumns: Int) = when (numberOfColumns) {
        2 -> (parentWidth / numberOfColumns) - 16.dp - 8.dp
        else -> FrameLayout.LayoutParams.WRAP_CONTENT
    }

    private fun getTopMargin(index: Int, numberOfColumns: Int): Int {
        return when (numberOfColumns) {
            2 -> {
                (index / numberOfColumns) * (190 + 16).dp
            }
            else -> {
                (index / numberOfColumns) * (96 + 16).dp
            }
        }
    }

    private fun getStartMargin(index: Int, numberOfColumns: Int): Int {
        return when (numberOfColumns) {
            2 -> {
                when (index % numberOfColumns) {
                    0 -> 16.dp
                    else -> 8.dp + (parentWidth / numberOfColumns)
                }
            }
            else -> 16.dp
        }
    }

    private fun getEndMargin(index: Int, numberOfColumns: Int): Int {
        return when (numberOfColumns) {
            2 -> {
                when (index % numberOfColumns) {
                    0 -> 8.dp
                    else -> 16.dp
                }
            }
            else -> 16.dp
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        parentHeight = MeasureSpec.getSize(heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}
