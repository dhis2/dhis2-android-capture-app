package org.dhis2.data.forms.dataentry.fields

import androidx.databinding.ObservableField
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import io.reactivex.processors.FlowableProcessor
import org.dhis2.R
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

class FormViewHolderFactory(
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
        binding: ViewDataBinding,
        viewType: Int
    ): FormViewHolder? {
        return when (viewType) {
            R.layout.form_age_custom -> provideAgeViewHolder(binding)
            R.layout.custom_form_coordinate -> provideCoordinateViewHolder(binding)
            R.layout.form_time_text -> provideTimeViewHolder(binding)
            R.layout.form_date_text -> provideDateViewHolder(binding)
            R.layout.form_date_time_text -> provideDateTimeViewHolder(binding)
            R.layout.item_indicator -> provideDisplayViewHolder(binding)
            R.layout.form_edit_text_custom -> provideEditTextViewHolder(binding)
//            DataEntryViewHolderTypes.LONG_TEXT -> provideLongTextViewHolder(binding)
            R.layout.form_button -> provideFileViewHolder(binding)
            R.layout.form_image -> provideImageViewHolder(binding)
            R.layout.form_option_set_selector -> provideOptionSetViewHolder(binding)
            R.layout.form_org_unit -> provideOrgUnitViewHolder(binding)
            R.layout.custom_form_picture -> providePictureViewHolder(binding)
            R.layout.form_yes_no -> provideYesNoViewHolder(binding)
            R.layout.form_scan -> provideScanViewHolder(binding)
            R.layout.form_section -> provideSectionViewHolder(binding)
            R.layout.form_option_set -> provideSpinnerViewHolder(binding)
            else -> provideUnsupportedViewHolder(binding)
        }
    }


    private fun provideAgeViewHolder(binding: ViewDataBinding): AgeHolder {
        return AgeHolder(binding, currentFocusUid)
    }

    private fun provideCoordinateViewHolder(binding: ViewDataBinding): CoordinateHolder {
        return CoordinateHolder(binding, currentFocusUid)
    }

    private fun provideTimeViewHolder(binding: ViewDataBinding): DateTimeHolder {
        return DateTimeHolder(binding, currentFocusUid)
    }

    private fun provideDateViewHolder(binding: ViewDataBinding): DateTimeHolder {
        return DateTimeHolder(binding, currentFocusUid)
    }

    private fun provideDateTimeViewHolder(binding: ViewDataBinding): DateTimeHolder {
        return DateTimeHolder(binding, currentFocusUid)
    }

    private fun provideDisplayViewHolder(binding: ViewDataBinding): DisplayHolder {
        return DisplayHolder(binding)
    }

    private fun provideEditTextViewHolder(binding: ViewDataBinding): EditTextCustomHolder {
        return EditTextCustomHolder(binding, currentFocusUid)
    }

    private fun provideLongTextViewHolder(binding: ViewDataBinding): EditTextCustomHolder {
//        binding.customEdittext.setLayoutData(true, true)
//        binding.customEdittext.setRenderType(renderType)
        return EditTextCustomHolder(binding, currentFocusUid)
    }

    private fun provideFileViewHolder(binding: ViewDataBinding): FileHolder {
        return FileHolder(binding, currentFocusUid)
    }

    private fun provideImageViewHolder(binding: ViewDataBinding): ImageHolder {
        return ImageHolder(binding)
    }

    private fun provideOptionSetViewHolder(binding: ViewDataBinding): OptionSetHolder {
        return OptionSetHolder(binding, currentFocusUid)
    }

    private fun provideOrgUnitViewHolder(binding: ViewDataBinding): OrgUnitHolder {
        return OrgUnitHolder(binding, currentFocusUid)
    }

    private fun providePictureViewHolder(binding: ViewDataBinding): PictureHolder {
        return PictureHolder(binding)
    }

    private fun provideYesNoViewHolder(binding: ViewDataBinding): RadioButtonHolder {
        return RadioButtonHolder(binding, currentFocusUid)
    }

    private fun provideScanViewHolder(binding: ViewDataBinding): ScanTextHolder {
        return ScanTextHolder(binding, currentFocusUid)
    }

    private fun provideSectionViewHolder(binding: ViewDataBinding): SectionHolder {
        return SectionHolder(binding, selectedSection, sectionProcessor)
    }

    private fun provideSpinnerViewHolder(binding: ViewDataBinding): SpinnerHolder {
        return SpinnerHolder(binding, currentFocusUid)
    }

    private fun provideUnsupportedViewHolder(binding: ViewDataBinding): UnsupportedHolder {
        return UnsupportedHolder(binding)
    }
}
