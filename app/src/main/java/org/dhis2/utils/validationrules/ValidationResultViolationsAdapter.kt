package org.dhis2.utils.validationrules

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ValidationResultViolationsAdapter(
    fa: FragmentActivity,
    private val violations: List<Violation>,
) : FragmentStateAdapter(fa) {

    override fun getItemCount() = violations.size

    override fun createFragment(position: Int): Fragment {
        val fragment = ValidationResultViolationFragment()
        fragment.setViolation(violations[position])
        return fragment
    }
}
