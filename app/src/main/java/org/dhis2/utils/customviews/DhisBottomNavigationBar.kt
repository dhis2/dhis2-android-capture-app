package org.dhis2.utils.customviews

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
import org.dhis2.Bindings.clipWithRoundedCorners
import org.dhis2.Bindings.dp
import org.dhis2.R

const val EXPAND_BUTTON_TAG = "EXPAND_BUTTON_TAG"
class DhisBottomNavigationBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BottomNavigationView(context, attrs, defStyleAttr) {

    private val animations = DhisBottomNavigationBarAnimations(this)
    private var hidden = false

    init {
        labelVisibilityMode = LABEL_VISIBILITY_UNLABELED
        this.clipWithRoundedCorners()
        addExpandButton()
    }

    private fun addExpandButton(){
        addView(
            ImageView(context).apply {
                tag = EXPAND_BUTTON_TAG
                visibility = View.GONE
                scaleX = 0f
                scaleY = 0f
                setImageResource(R.drawable.ic_menu)
                DrawableCompat.setTint(DrawableCompat.wrap(this.drawable), Color.BLUE)
                setOnClickListener { animations.expand { onExpanded() } }
            },
            LayoutParams(24.dp, 24.dp).apply {
                gravity = Gravity.CENTER_VERTICAL
                marginStart = 14.dp
            }
        )
    }

    private fun onExpanded(){
        hidden = true
    }

    private fun onCollapsed(){
        hidden = false
    }

}