package org.dhis2.utils.customviews

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomnavigation.LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED
import org.dhis2.Bindings.clipWithRoundedCorners

class DhisBottomNavigationBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : BottomNavigationView(context, attrs, defStyleAttr) {

    init {
        labelVisibilityMode = LABEL_VISIBILITY_UNLABELED
        setOnNavigationItemSelectedListener { true }
        this.clipWithRoundedCorners()
    }

    fun hide(withAnimation:Boolean = false){
        if(withAnimation){

        }else{
            visibility = View.GONE
        }
    }
}