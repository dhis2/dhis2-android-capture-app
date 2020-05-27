package org.dhis2.utils.validationrules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.dhis2.R
import org.dhis2.databinding.ValidationRulesDialogBinding
import org.hisp.dhis.android.core.validation.engine.ValidationResultViolation

class ValidationRulesBottomDialog : BottomSheetDialogFragment() {

    private lateinit var binding: ValidationRulesDialogBinding
    private lateinit var positiveText: String
    private lateinit var adapter: ValidationResultViolationsAdapter

    private var violations: List<ValidationResultViolation> = mutableListOf()

    private var positiveOnclick: (() -> Unit)? = null
    private var negativeText: String? = null
    private var negativeOnclick: (() -> Unit)? = null
    private var title: String? = null

    private var message: String? = null

    companion object {
        val instance: ValidationRulesBottomDialog
            get() = ValidationRulesBottomDialog()
    }

    fun setTitle(title: String) = apply { this.title = title }

    fun setMessage(message: String) = apply { this.message = message }

    fun setPositiveButton(text: String, onClick: (() -> Unit)? = null) = apply {
        this.positiveText = text
        this.positiveOnclick = onClick
    }

    fun setNegativeButton(text: String? = null, onClick: (() -> Unit)? = null) = apply {
        this.negativeText = text
        this.negativeOnclick = onClick
    }

    fun setViolations(violations: List<ValidationResultViolation>) = apply {
        this.violations = violations
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.validation_rules_dialog, container, false)
        binding.setTitle(title)
        binding.setMessage(message)
        binding.setErrorCount(violations.size)

        val colorRes = ContextCompat.getColor(binding.root.context, R.color.colorPrimary)

        binding.positiveBtn.apply {
            setTextColor(colorRes)
            text = positiveText
            setOnClickListener {
                positiveOnclick?.invoke()
                dismiss()
            }
        }

        negativeText.let {
            binding.negativeBtn.apply {
                setTextColor(colorRes)
                visibility = View.VISIBLE
                text = it
                setOnClickListener {
                    negativeOnclick?.invoke()
                    dismiss()
                }
            }
        }

        if (!violations.isNullOrEmpty()) {
            adapter = ValidationResultViolationsAdapter(this, violations)
            binding.violationsViewPager.adapter = adapter
            binding.dotsIndicator.setViewPager(binding.violationsViewPager)
        }

        return binding.root
    }

    // This is necessary to show the bottomSheet dialog with full height on landscape
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.viewTreeObserver.addOnGlobalLayoutListener {
            val dialog = dialog as BottomSheetDialog

            val bottomSheet =
                dialog.findViewById<FrameLayout>(
                    com.google.android.material.R.id.design_bottom_sheet
                )
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.setPeekHeight(0)
        }
    }
}
