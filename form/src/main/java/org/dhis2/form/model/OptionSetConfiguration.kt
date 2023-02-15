package org.dhis2.form.model

import org.hisp.dhis.android.core.option.Option

sealed class OptionSetConfiguration(
    open val options: List<Option> = emptyList(),
    open val optionsToHide: List<String>,
    open val optionsToShow: List<String>
) {
    data class DefaultOptionSet(
        override val options: List<Option>,
        override val optionsToHide: List<String> = emptyList(),
        override val optionsToShow: List<String> = emptyList()
    ) : OptionSetConfiguration(
        options = options,
        optionsToHide = optionsToHide,
        optionsToShow = optionsToShow
    )

    data class BigOptionSet(
        override val optionsToHide: List<String> = emptyList(),
        override val optionsToShow: List<String> = emptyList()
    ) : OptionSetConfiguration(
        optionsToHide = optionsToHide,
        optionsToShow = optionsToShow
    )

    fun optionsToDisplay() = options.filter { option ->
        when {
            optionsToShow.isNotEmpty() ->
                optionsToShow.contains(option.uid())
            else ->
                !optionsToHide.contains(option.uid())
        }
    }.sortedBy { it.sortOrder() }

    companion object {
        fun config(
            optionCount: Int,
            optionRequestCallback: () -> List<Option>
        ): OptionSetConfiguration {
            return when {
                optionCount > 15 -> BigOptionSet()
                else -> DefaultOptionSet(
                    options = optionRequestCallback()
                )
            }
        }
    }

    fun updateOptionsToHideAndShow(
        optionsToHide: List<String>,
        optionsToShow: List<String>
    ): OptionSetConfiguration {
        return setOptionsToShow(optionsToShow).setOptionsToHide(optionsToHide)
    }

    private fun setOptionsToHide(optionsToHide: List<String>): OptionSetConfiguration {
        return when (this) {
            is BigOptionSet -> copy(optionsToHide = optionsToHide)
            is DefaultOptionSet -> copy(optionsToHide = optionsToHide)
        }
    }

    private fun setOptionsToShow(optionsToShow: List<String>): OptionSetConfiguration {
        return when (this) {
            is BigOptionSet -> copy(optionsToShow = optionsToShow)
            is DefaultOptionSet -> copy(optionsToShow = optionsToShow)
        }
    }
}
