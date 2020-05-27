package org.dhis2.utils.validationrules

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.hisp.dhis.android.core.validation.engine.ValidationResultViolation

class ValidationResultViolationsAdapter(
    fragment: Fragment,
    private val violations: List<ValidationResultViolation>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = violations.size

    override fun createFragment(position: Int): Fragment {
        val fragment = ValidationResultViolationFragment()
        fragment.setViolation(violations[position])
        return fragment
    }
}
