package org.dhis2.data.forms.dataentry.fields.visualOptionSet

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import org.dhis2.utils.customviews.FieldLayout
import org.dhis2.utils.customviews.ImageCustomView

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
                        LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    viewModel?.let {
                        it.options().forEachIndexed { index, option ->
                            addView(
                                ImageCustomView(context).apply {
                                    layoutParams =
                                        LayoutParams(parentWidth / it.numberOfColumns(), 500).apply {
                                            topMargin = (index/it.numberOfColumns()) * 500
                                            marginStart =
                                                (index % it.numberOfColumns()) * (parentWidth / it.numberOfColumns())
                                        }
                                    setViewModel(it, option)
                                }
                            )
                        }
                    }
                }
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        parentWidth = MeasureSpec.getSize(widthMeasureSpec)
        parentHeight = MeasureSpec.getSize(heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}