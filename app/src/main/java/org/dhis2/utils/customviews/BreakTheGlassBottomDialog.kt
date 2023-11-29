package org.dhis2.utils.customviews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.dhis2.R
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.BreakTheGlassBottomDialogBindingImpl

class BreakTheGlassBottomDialog : BottomSheetDialogFragment() {

    val colorUtils: ColorUtils = ColorUtils()

    fun setPositiveButton(onClick: ((String) -> Unit)? = null) = apply {
        this.positiveOnclick = onClick
    }

    private var positiveOnclick: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BreakTheGlassBottomSheetDialogTheme)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return BreakTheGlassBottomDialogBindingImpl.inflate(inflater, container, false).apply {
            positive.apply {
                setOnClickListener {
                    positiveOnclick?.invoke(inputEditText.text.toString())
                    dismiss()
                }
            }
            negative.apply {
                setOnClickListener {
                    dismiss()
                }
            }
            inputEditText.doOnTextChanged { _, _, _, _ ->
                positive.isEnabled = inputEditText.text.toString().isNotEmpty()
                clearButton.visibility = if (inputEditText.text.toString().isEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            }
            inputEditText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    val focusColor =
                        colorUtils.getPrimaryColor(requireContext(), ColorType.PRIMARY)
                    label.setTextColor(focusColor)
                    selectionView.setBackgroundColor(focusColor)
                } else {
                    val unFocusColor = ContextCompat.getColor(
                        requireContext(),
                        R.color.text_black_A63,
                    )
                    label.setTextColor(unFocusColor)
                    selectionView.setBackgroundColor(unFocusColor)
                }
            }
            clearButton.setOnClickListener {
                inputEditText.text?.clear()
            }
        }.root
    }

    // This is necessary to show the bottomSheet dialog with full height on landscape
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog

            val bottomSheet =
                dialog.findViewById<FrameLayout>(
                    com.google.android.material.R.id.design_bottom_sheet,
                )
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.peekHeight = 0

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    /*NoUse*/
                }
            })
        }
    }
}
