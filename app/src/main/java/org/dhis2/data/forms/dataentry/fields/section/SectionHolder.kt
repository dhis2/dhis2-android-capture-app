package org.dhis2.data.forms.dataentry.fields.section

import android.animation.ValueAnimator
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.databinding.ObservableField
import io.reactivex.processors.FlowableProcessor
import org.dhis2.Bindings.dp
import org.dhis2.Bindings.getThemePrimaryColor
import org.dhis2.R
import org.dhis2.data.forms.dataentry.fields.FormViewHolder
import org.dhis2.databinding.FormSectionBinding
import org.dhis2.utils.customviews.CustomDialog
import org.jetbrains.annotations.NotNull

class SectionHolder(
    private val formBinding: @NotNull FormSectionBinding,
    private val selectedSection: @NotNull ObservableField<String>,
    private val sectionProcessor: @NotNull FlowableProcessor<String>
) : FormViewHolder(formBinding), View.OnClickListener {

    private lateinit var viewModel: SectionViewModel

    init {
        selectedSection.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(
                sender: Observable,
                propertyId: Int
            ) {
                setShadows()
                animateArrow()
            }
        })
        formBinding.root.setOnClickListener(this)
        formBinding.descriptionIcon.setOnClickListener {
            showDescription()
        }
    }

    fun update(viewModel: SectionViewModel) {
        this.viewModel = viewModel
        checkVisibility(viewModel.uid() == SectionViewModel.CLOSING_SECTION_UID)
        formBinding.apply {
            sectionName.text = viewModel.label()
            openIndicator.scaleY = if (viewModel.isOpen) 1f else -1f
            when (viewModel.errors()) {
                null, 0 -> sectionFieldsInfo.apply {
                    text = String.format(
                        "%s/%s",
                        viewModel.completedFields(),
                        viewModel.totalFields()
                    )
                    background = null
                    setTextColor(
                        when {
                            viewModel.completedFields() == viewModel.totalFields() ->
                                root.getThemePrimaryColor()
                            else ->
                                ResourcesCompat.getColor(root.resources, R.color.placeholder, null)
                        }
                    )

                }
                else -> sectionFieldsInfo.apply {
                    text = String.format(
                        "%s %s",
                        viewModel.errors(),
                        itemView.context.getString(R.string.errors)
                    )
                    background =
                        ContextCompat.getDrawable(itemView.context, R.drawable.bg_section_error)
                    setTextColor(
                        ResourcesCompat.getColor(root.resources, R.color.white, null)
                    )
                }
            }
        }

        formBinding.descriptionIcon.visibility = if (viewModel.description().isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }

        setShadows()
    }

    private fun checkVisibility(isClosingSection: Boolean) {
        formBinding.sectionDetails.visibility = if (isClosingSection) {
            View.GONE
        } else {
            View.VISIBLE
        }
        formBinding.lastSectionDetails.visibility = if (isClosingSection) {
            View.VISIBLE
        } else {
            View.GONE
        }
        formBinding.shadowEnd.visibility = if (isClosingSection) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun onClick(v: View) {
        if (viewModel.uid() != SectionViewModel.CLOSING_SECTION_UID) {
            sectionProcessor.onNext(viewModel.uid())
        }
    }

    private fun setShadows() {
        val isSelected = viewModel.isOpen
        if (isSelected) {
            formBinding.shadowTop.visibility = View.VISIBLE
        } else {
            formBinding.shadowTop.visibility = View.GONE
        }
    }

    private fun animateArrow() {
        val isSelected = selectedSection.get() == viewModel.uid()
        if (isSelected) {
            formBinding.openIndicator.scaleY = 1f
        }
        formBinding.openIndicator.animate()
            .scaleY(if (isSelected) 1f else -1f)
            .setDuration(200)
            .start()
    }

    fun setBottomShadow(showShadow: Boolean) {
        formBinding.shadowBottom.visibility = if (showShadow) View.VISIBLE else View.GONE
    }

    fun setLastSectionHeight(previousSectionIsOpened: Boolean) {
        val params = formBinding.lastSectionDetails.layoutParams
        val finalHeight = if (previousSectionIsOpened) {
            48.dp
        } else {
            1.dp
        }
        ValueAnimator.ofInt(params.height, finalHeight).apply {
            duration = 120
            addUpdateListener {
                params.height = it.animatedValue as Int
                formBinding.lastSectionDetails.layoutParams = params
            }
            start()
        }
    }

    private fun showDescription() {
        CustomDialog(
            itemView.context,
            viewModel.label(),
            viewModel.description() ?: "",
            itemView.context.getString(R.string.action_close),
            null,
            201,
            null
        ).show()
    }

    fun handleHeaderClick(x: Float) {
        val hasDescription = formBinding.descriptionIcon.visibility == View.VISIBLE;
        val descriptionClicked =
            formBinding.descriptionIcon.x <= x &&
                    formBinding.descriptionIcon.x + formBinding.descriptionIcon.width >= x;
        if (hasDescription && descriptionClicked) {
            showDescription()
        } else {
            onClick(itemView)
        }
    }
}
