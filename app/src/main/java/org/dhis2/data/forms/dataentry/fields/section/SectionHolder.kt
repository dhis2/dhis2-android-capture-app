package org.dhis2.data.forms.dataentry.fields.section

import android.animation.Animator
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.databinding.ObservableField
import io.reactivex.processors.FlowableProcessor
import org.dhis2.Bindings.getThemePrimaryColor
import org.dhis2.R
import org.dhis2.data.forms.dataentry.fields.FormViewHolder
import org.dhis2.databinding.FormSectionBinding
import org.dhis2.utils.customviews.CustomDialog
import org.hisp.dhis.android.core.dataset.Section
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
    }

    fun update(viewModel: SectionViewModel) {
        this.viewModel = viewModel
        checkVisibility(viewModel.uid() == SectionViewModel.CLOSING_SECTION_UID)
        formBinding.apply {
            sectionName.text = viewModel.label()
            openIndicator.visibility = if (viewModel.isOpen) View.VISIBLE else View.GONE
            if (viewModel.completedFields() == viewModel.totalFields()) {
                sectionFieldsInfo.setTextColor(root.getThemePrimaryColor())
            } else {
                sectionFieldsInfo.setTextColor(
                    ResourcesCompat.getColor(root.resources, R.color.placeholder, null)
                )
            }
            sectionFieldsInfo.text = String.format(
                "%s/%s",
                viewModel.completedFields(),
                viewModel.totalFields()
            )
            sectionDetails.setBackgroundColor(
                when {
                    viewModel.error() != null -> ContextCompat.getColor(
                        binding.root.context,
                        R.color.error_color
                    )
                    else -> ContextCompat.getColor(
                        binding.root.context,
                        R.color.colorAccent
                    )
                }
            )
        }

        formBinding.descriptionIcon.visibility = if (viewModel.description().isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }

        formBinding.descriptionIcon.setOnClickListener {
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

        setShadows()
    }

    private fun checkVisibility(isClosingSection: Boolean) {
        formBinding.sectionDetails.visibility = if (isClosingSection) {
            View.GONE
        } else {
            View.VISIBLE
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
            formBinding.openIndicator.rotation = -45f
        }
        formBinding.openIndicator.animate()
            .scaleY(if (isSelected) 1f else 0f)
            .scaleX(if (isSelected) 1f else 0f)
            .rotation(if (isSelected) 0f else -45f)
            .setDuration(200)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    formBinding.openIndicator.visibility =
                        if (viewModel.isOpen) View.VISIBLE else View.GONE
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            .start()
    }

    fun setBottomShadow(showShadow: Boolean) {
        formBinding.shadowBottom.visibility = if (showShadow) View.VISIBLE else View.GONE
    }
}
