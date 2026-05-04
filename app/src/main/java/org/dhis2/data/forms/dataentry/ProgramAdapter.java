package org.dhis2.data.forms.dataentry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import org.dhis2.R;
import org.dhis2.databinding.SpinnerProgramItemLayoutBinding;
import org.dhis2.databinding.SpinnerProgramLayoutBinding;
import org.dhis2.usescases.searchTrackEntity.ProgramSpinnerModel;

import java.util.List;

public class ProgramAdapter extends ArrayAdapter<ProgramSpinnerModel> {

    private List<ProgramSpinnerModel> programs;
    private String trackedEntityName;

    public ProgramAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<ProgramSpinnerModel> programs, String trackedEntityName) {
        super(context, resource, textViewResourceId, programs);
        this.programs = programs;
        this.trackedEntityName = trackedEntityName;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            SpinnerProgramLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.spinner_program_layout, parent, false);
            convertView = binding.getRoot();
            setProgram(binding, position);
        }
        return convertView;

    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SpinnerProgramItemLayoutBinding binding = DataBindingUtil.inflate(inflater, R.layout.spinner_program_item_layout, parent, false);
        setProgram(binding, position);
        return binding.getRoot();
    }

    private void setProgram(SpinnerProgramLayoutBinding binding, int position) {
        if (position != 0) {
            binding.setProgram(programs.get(position - 1));
        } else {
            binding.setProgram(new ProgramSpinnerModel(
                    trackedEntityName,
                    String.format(getContext().getString(R.string.all_tei_type), trackedEntityName),
                    false
            ));
        }
    }

    private void setProgram(SpinnerProgramItemLayoutBinding binding, int position) {
        if (position != 0) {
            binding.setProgram(programs.get(position - 1));
        } else {
            binding.setProgram(new ProgramSpinnerModel(
                    trackedEntityName,
                    String.format(getContext().getString(R.string.all_tei_type), trackedEntityName),
                    false
            ));
        }
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

}
