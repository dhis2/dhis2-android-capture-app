package org.dhis2.data.forms.dataentry.fields

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import io.reactivex.processors.FlowableProcessor
import org.dhis2.R
import org.dhis2.data.forms.dataentry.DataEntryViewHolderTypes
import org.dhis2.data.forms.dataentry.fields.age.AgeHolder
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateHolder
import org.dhis2.data.forms.dataentry.fields.datetime.DateTimeHolder
import org.dhis2.data.forms.dataentry.fields.display.DisplayHolder
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextCustomHolder
import org.dhis2.data.forms.dataentry.fields.file.FileHolder
import org.dhis2.data.forms.dataentry.fields.image.ImageHolder
import org.dhis2.data.forms.dataentry.fields.optionset.OptionSetHolder
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitHolder
import org.dhis2.data.forms.dataentry.fields.picture.PictureHolder
import org.dhis2.data.forms.dataentry.fields.radiobutton.RadioButtonHolder
import org.dhis2.data.forms.dataentry.fields.scan.ScanTextHolder
import org.dhis2.data.forms.dataentry.fields.section.SectionHolder
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerHolder
import org.dhis2.data.forms.dataentry.fields.unsupported.UnsupportedHolder
import org.dhis2.databinding.CustomFormCoordinateBinding
import org.dhis2.databinding.CustomFormPictureBinding
import org.dhis2.databinding.FormAgeCustomBinding
import org.dhis2.databinding.FormButtonBinding
import org.dhis2.databinding.FormDateTextBinding
import org.dhis2.databinding.FormDateTimeTextBinding
import org.dhis2.databinding.FormEditTextCustomBinding
import org.dhis2.databinding.FormImageBinding
import org.dhis2.databinding.FormOptionSetBinding
import org.dhis2.databinding.FormOptionSetSelectorBinding
import org.dhis2.databinding.FormOrgUnitBinding
import org.dhis2.databinding.FormScanBinding
import org.dhis2.databinding.FormSectionBinding
import org.dhis2.databinding.FormTimeTextBinding
import org.dhis2.databinding.FormUnsupportedCustomBinding
import org.dhis2.databinding.FormYesNoBinding
import org.dhis2.databinding.ItemIndicatorBinding
import org.dhis2.utils.customviews.PictureView.OnIntentSelected
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType

class FormViewHolderFactory(
    private val layoutInflater: LayoutInflater,
    private val processor: FlowableProcessor<RowAction>,
    private val renderType: String?,
    private val currentFocusUid: MutableLiveData<String>,
    private val totalFields: Int,
    private val imageSelector: ObservableField<String>,
    private val sectionRendering: String?,
    private val fragmentManager: FragmentManager,
    private val sectionProcessor: FlowableProcessor<String>,
    private val selectedSection: ObservableField<String>
) {

    fun provideHolder(
        parent: ViewGroup,
        type: DataEntryViewHolderTypes
    ): FormViewHolder? {
        return when (type) {
            DataEntryViewHolderTypes.AGE_VIEW -> provideAgeViewHolder(parent)
            DataEntryViewHolderTypes.COORDINATES -> provideCoordinateViewHolder(parent)
            DataEntryViewHolderTypes.TIME -> provideTimeViewHolder(parent)
            DataEntryViewHolderTypes.DATE -> provideDateViewHolder(parent)
            DataEntryViewHolderTypes.DATETIME -> provideDateTimeViewHolder(parent)
            DataEntryViewHolderTypes.DISPLAY -> provideDisplayViewHolder(parent)
            DataEntryViewHolderTypes.EDIT_TEXT -> provideEditTextViewHolder(parent)
            DataEntryViewHolderTypes.LONG_TEXT -> provideLongTextViewHolder(parent)
            DataEntryViewHolderTypes.BUTTON -> provideFileViewHolder(parent)
            DataEntryViewHolderTypes.IMAGE -> provideImageViewHolder(parent)
            DataEntryViewHolderTypes.OPTION_SET_SELECT -> provideOptionSetViewHolder(parent)
            DataEntryViewHolderTypes.ORG_UNIT -> provideOrgUnitViewHolder(parent)
            DataEntryViewHolderTypes.PICTURE -> providePictureViewHolder(parent)
            DataEntryViewHolderTypes.YES_NO -> provideYesNoViewHolder(parent)
            DataEntryViewHolderTypes.SCAN_CODE -> provideScanViewHolder(parent)
            DataEntryViewHolderTypes.SECTION -> provideSectionViewHolder(parent)
            DataEntryViewHolderTypes.OPTION_SET_SPINNER -> provideSpinnerViewHolder(parent)
            DataEntryViewHolderTypes.UNSUPPORTED -> provideUnsupportedViewHolder(parent)
        }
    }


    private fun provideAgeViewHolder(parent: ViewGroup): AgeHolder {
        val binding: FormAgeCustomBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.form_age_custom,
            parent,
            false
        )
        binding.customAgeview.setIsBgTransparent(true)
        return AgeHolder(binding, processor, false, currentFocusUid)
    }

    private fun provideCoordinateViewHolder(parent: ViewGroup): CoordinateHolder {
        val binding: CustomFormCoordinateBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.custom_form_coordinate,
            parent,
            false
        )
        binding.formCoordinates.setIsBgTransparent(true)

        return CoordinateHolder(binding, processor, false, currentFocusUid)
    }

    private fun provideTimeViewHolder(parent: ViewGroup): DateTimeHolder {
        val binding: FormTimeTextBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.form_time_text,
            parent,
            false
        )
        binding.timeView.setIsBgTransparent(true)
        return DateTimeHolder(binding, processor, false, currentFocusUid)
    }

    private fun provideDateViewHolder(parent: ViewGroup): DateTimeHolder {
        val binding: FormDateTextBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.form_date_text,
            parent,
            false
        )
        binding.dateView.setIsBgTransparent(true)
        return DateTimeHolder(binding, processor, false, currentFocusUid)
    }

    private fun provideDateTimeViewHolder(parent: ViewGroup): DateTimeHolder {
        val binding: FormDateTimeTextBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.form_date_time_text,
            parent,
            false
        )
        binding.dateTimeView.setIsBgTransparent(true)
        return DateTimeHolder(binding, processor, false, currentFocusUid)
    }

    private fun provideDisplayViewHolder(parent: ViewGroup): DisplayHolder {
        val binding: ItemIndicatorBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.item_indicator,
            parent,
            false
        )

        return DisplayHolder(binding)
    }

    private fun provideEditTextViewHolder(parent: ViewGroup): EditTextCustomHolder {
        val binding: FormEditTextCustomBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.form_edit_text_custom,
            parent,
            false
        )
        binding.customEdittext.setLayoutData(true, false)
        binding.customEdittext.setRenderType(renderType)
        return EditTextCustomHolder(binding, processor, false, currentFocusUid)
    }

    private fun provideLongTextViewHolder(parent: ViewGroup): EditTextCustomHolder {
        val binding: FormEditTextCustomBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.form_edit_text_custom,
            parent,
            false
        )
        binding.customEdittext.setLayoutData(true, true)
        binding.customEdittext.setRenderType(renderType)
        return EditTextCustomHolder(binding, processor, false, currentFocusUid)
    }

    private fun provideFileViewHolder(parent: ViewGroup): FileHolder {
        val binding: FormButtonBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.form_button,
            parent,
            false
        )
        binding.formButton.setTextColor(
            ContextCompat.getColor(
                parent.context,
                R.color.colorPrimary
            )
        )
        return FileHolder(binding, currentFocusUid)
    }

    private fun provideImageViewHolder(parent: ViewGroup): ImageHolder {
        val binding: FormImageBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.form_image,
            parent,
            false
        )

        var height: Int? = null
        val parentHeight: Int = parent.height
        sectionRendering?.let {
            when (it) {
                ProgramStageSectionRenderingType.SEQUENTIAL.name -> {
                    height = parentHeight / if (totalFields > 2) 3 else totalFields
                }
                ProgramStageSectionRenderingType.MATRIX.name -> {
                    height = parentHeight / (totalFields / 2 + 1)
                }
            }
        }

        height?.let {
            val rootView = binding.root
            val layoutParams = rootView.layoutParams
            layoutParams.height = it
            rootView.layoutParams = layoutParams
        }

        return ImageHolder(binding, processor, imageSelector)
    }

    private fun provideOptionSetViewHolder(parent: ViewGroup): OptionSetHolder {
        val binding: FormOptionSetSelectorBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.form_option_set_selector,
            parent,
            false
        )
        binding.optionSetSelectionView.setLayoutData(true, renderType)
        return OptionSetHolder(binding, processor, true, currentFocusUid)
    }

    private fun provideOrgUnitViewHolder(parent: ViewGroup): OrgUnitHolder {
        val binding: FormOrgUnitBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.form_org_unit,
            parent,
            false
        )
        binding.orgUnitView.setLayoutData(true, renderType)
        binding.orgUnitView.setFragmentManager(fragmentManager)
        return OrgUnitHolder(binding, processor, false, currentFocusUid)
    }

    private fun providePictureViewHolder(parent: ViewGroup): PictureHolder {
        val binding: CustomFormPictureBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.custom_form_picture,
            parent,
            false
        )
        binding.formPictures.setIsBgTransparent(true)
        binding.formPictures.setFragmentManager(fragmentManager)
        val onIntentSelected = binding.formPictures.context as OnIntentSelected
        return PictureHolder(onIntentSelected, binding, processor, false)
    }

    private fun provideYesNoViewHolder(parent: ViewGroup): RadioButtonHolder {
        val binding: FormYesNoBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.form_yes_no,
            parent,
            false
        )
        binding.customYesNo.setIsBgTransparent(true)
        return RadioButtonHolder(binding, processor, false, currentFocusUid)
    }

    private fun provideScanViewHolder(parent: ViewGroup): ScanTextHolder {
        val binding: FormScanBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.form_scan,
            parent,
            false
        )
        binding.scanTextView.setLayoutData(true)
        return ScanTextHolder(binding, processor, false, currentFocusUid)
    }

    private fun provideSectionViewHolder(parent: ViewGroup): SectionHolder {
        val binding: FormSectionBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.form_section,
            parent,
            false
        )
        return SectionHolder(binding, selectedSection, sectionProcessor)
    }

    private fun provideSpinnerViewHolder(parent: ViewGroup): SpinnerHolder {
        val binding: FormOptionSetBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.form_option_set,
            parent,
            false
        )
        binding.optionSetView.setLayoutData(true, renderType)
        return SpinnerHolder(binding, processor, false, currentFocusUid)
    }

    private fun provideUnsupportedViewHolder(parent: ViewGroup): UnsupportedHolder {
        val binding: FormUnsupportedCustomBinding = DataBindingUtil.inflate(
            layoutInflater,
            R.layout.form_unsupported_custom,
            parent,
            false
        )
        return UnsupportedHolder(binding)
    }
}
