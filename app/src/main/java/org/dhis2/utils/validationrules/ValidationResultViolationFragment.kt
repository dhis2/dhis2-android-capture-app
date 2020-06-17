package org.dhis2.utils.validationrules

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.dhis2.R
import org.dhis2.databinding.FragmentValidationResultViolationBinding
import org.hisp.dhis.android.core.validation.engine.ValidationResultViolation

class ValidationResultViolationFragment : Fragment() {

    private lateinit var binding: FragmentValidationResultViolationBinding
    private lateinit var violation: ValidationResultViolation

    companion object {
        @JvmStatic
        fun create(): ValidationResultViolationFragment {
            return ValidationResultViolationFragment()
        }
    }

    fun setViolation(violation: ValidationResultViolation) {
        this.violation = violation
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_validation_result_violation,
            container,
            false
        )

        binding.violation = violation

        val formula = "${violation.leftSideEvaluation().displayExpression()} " +
            "${violation.validationRule().operator().mathematicalOperator} " +
            violation.rightSideEvaluation().displayExpression()

        val resultEquation = "${violation.leftSideEvaluation().regeneratedExpression()} " +
            "${violation.validationRule().operator().mathematicalOperator} " +
            violation.rightSideEvaluation().regeneratedExpression()

        binding.textValueEquation.text = formula
        binding.resultEquation.text = resultEquation

        return binding.root
    }
}
