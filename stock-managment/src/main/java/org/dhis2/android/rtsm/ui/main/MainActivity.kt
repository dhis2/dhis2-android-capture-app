package org.dhis2.android.rtsm.ui.main

import android.os.Bundle
import android.util.TypedValue
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.ui.home.HomeViewModel
import org.dhis2.android.rtsm.ui.main.screens.Backdrop
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()
    private var data = mutableListOf<OrganisationUnit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                updateTheme(TransactionType.DISCARD)
                Backdrop(viewModel) { finish() }
            }
        }
    }

    private fun updateTheme(type: TransactionType) {
        val color: Int
        val theme: Int
        viewModel.setToolbarTitle(type)
        when (type) {
            TransactionType.DISTRIBUTION -> {
                color = R.color.colorPrimary
                theme = R.style.AppTheme
            }
            TransactionType.DISCARD -> {
                color = R.color.discard_color
                theme = R.style.RedTheme
            }
            TransactionType.CORRECTION -> {
                color = R.color.colorPrimary_fbc
                theme = R.style.colorPrimary_fbc
            }
            else -> {
                theme = R.style.AppTheme
                color = -1
            }
        }
        if (color != -1) {

            getTheme().applyStyle(theme, true)
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            val typedValue = TypedValue()
            val a = obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorPrimaryDark))
            val colorToReturn = a.getColor(0, 0)
            a.recycle()
            window.statusBarColor = colorToReturn
        }
    }
}
