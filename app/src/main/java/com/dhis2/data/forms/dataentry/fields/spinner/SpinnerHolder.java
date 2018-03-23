package com.dhis2.data.forms.dataentry.fields.spinner;

import android.view.View;
import android.widget.AdapterView;

import com.dhis2.BR;
import com.dhis2.R;
import com.dhis2.data.forms.dataentry.OptionAdapter;
import com.dhis2.data.forms.dataentry.fields.FormViewHolder;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormSpinnerBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;

import org.hisp.dhis.android.core.option.OptionModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by ppajuelo on 07/11/2017.
 */

public class SpinnerHolder extends FormViewHolder implements AdapterView.OnItemSelectedListener {

    SearchTEContractsModule.Presenter presenter;
    TrackedEntityAttributeModel bindableOnject;
    FormSpinnerBinding binding;
    private SpinnerViewModel model;


    public SpinnerHolder(FormSpinnerBinding binding, FlowableProcessor<RowAction> processor) {
        super(binding);
        this.binding = binding;
        /*
        model = BehaviorProcessor.create();

        model.subscribe(spinnerViewModel -> {
            binding.setLabel(spinnerViewModel.label());
            binding.setOptionSet(spinnerViewModel.optionSet());
            binding.executePendingBindings();
        });*/

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position > 0) {
                    processor.onNext(
                            RowAction.create(model.uid(), ((OptionModel) adapterView.getItemAtPosition(position - 1)).displayName())
                    );
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityAttributeModel bindableOnject) {
        this.presenter = presenter;
        this.bindableOnject = bindableOnject;

        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.attribute, bindableOnject);

        binding.spinner.setOnItemSelectedListener(this);
        presenter.getOptions(bindableOnject.optionSet())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> setAdapter(data, bindableOnject),
                        Timber::d);

        binding.executePendingBindings();
    }

    void setAdapter(List<OptionModel> optionModels, TrackedEntityAttributeModel bindableOnject) {
        OptionAdapter adapter = new OptionAdapter(binding.spinner.getContext(),
                R.layout.spinner_layout,
                R.id.spinner_text,
                optionModels,
                bindableOnject.displayShortName());
        binding.spinner.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        if (position > 0)
            presenter.query(String.format("%s:EQ:%s", bindableOnject.uid(), ((OptionModel) adapterView.getItemAtPosition(position - 1)).displayName()), true);
        else
            presenter.clearFilter(bindableOnject.uid());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void update(SpinnerViewModel viewModel) {
        this.model = viewModel;
        binding.setLabel(viewModel.label());
        binding.setOptionSet(viewModel.optionSet());
        binding.executePendingBindings();
    }
}
