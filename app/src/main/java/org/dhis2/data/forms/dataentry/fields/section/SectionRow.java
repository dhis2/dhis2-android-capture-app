package org.dhis2.data.forms.dataentry.fields.section;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableField;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.Row;
import org.dhis2.databinding.FormSectionBinding;

import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by frodriguez on 1/24/2018.
 */

public class SectionRow implements Row<SectionHolder, SectionViewModel> {

    private final LayoutInflater inflater;
    private final ObservableField<String> selectedSection;
    private final FlowableProcessor<String> sectionProcessor;

    public SectionRow(LayoutInflater layoutInflater,
                      ObservableField<String> selectedSection,
                      FlowableProcessor<String> sectionProcessor) {
        this.inflater = layoutInflater;
        this.selectedSection = selectedSection;
        this.sectionProcessor = sectionProcessor;
    }

    @NonNull
    @Override
    public SectionHolder onCreate(@NonNull ViewGroup parent) {
        FormSectionBinding binding = DataBindingUtil.inflate(inflater, R.layout.form_section, parent, false);
        return new SectionHolder(binding, selectedSection, sectionProcessor);
    }

    @Override
    public void onBind(@NonNull SectionHolder viewHolder, @NonNull SectionViewModel viewModel) {
        viewHolder.update(viewModel);
    }

    @Override
    public void deAttach(@NonNull SectionHolder viewHolder) {
        viewHolder.dispose();
    }

}
