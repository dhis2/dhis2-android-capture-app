package org.dhis2.data.forms.dataentry.fields.image;

import android.databinding.ObservableField;
import android.support.v4.content.ContextCompat;
import android.view.View;

import org.dhis2.Bindings.Bindings;
import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FormViewHolder;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.databinding.FormImageBinding;

import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 31/05/2018.
 */

public class ImageHolder extends FormViewHolder {

    private final CompositeDisposable disposable;
    private final FormImageBinding binding;
    private final ObservableField<String> currentSelector;
    private boolean isEditable;
    private String valuePendingUpdate;

    ImageViewModel model;

    public ImageHolder(FormImageBinding mBinding, FlowableProcessor<RowAction> processor, ObservableField<String> imageSelector) {
        super(mBinding);
        this.binding = mBinding;
        this.currentSelector = imageSelector;
        this.disposable = new CompositeDisposable();
/*
        if (imageSelector != null)
            disposable.add(imageSelector
                    .debounce(1000, TimeUnit.MILLISECONDS, Schedulers.io())
                    .map(selectedValue -> selectedValue.equals(model.value()) || selectedValue.equals(valuePendingUpdate))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(isSelected -> binding.selectionBadge.setImageDrawable(
                            ContextCompat.getDrawable(binding.selectionBadge.getContext(),
                                    isSelected ? R.drawable.ic_check_circle : R.drawable.ic_unchecked_circle)
                    ), Timber::d));*/

        itemView.setOnClickListener(v -> {

            if (isEditable) {
                String value;
                String[] uids = model.uid().split("\\.");

                if(imageSelector.get().equals(model.label())) {
                    value = null;
                    imageSelector.set("");
                }else {
                    value = model.label();
                    imageSelector.set(value);
                }

                processor.onNext(RowAction.create(uids[0], value));
            }
        });

    }

    public void update(ImageViewModel viewModel) {
        this.model = viewModel;

        this.isEditable = viewModel.editable();
        descriptionText = viewModel.description();
        label = new StringBuilder(viewModel.label());
        if (viewModel.mandatory())
            label.append("*");
        binding.setLabel(label.toString());
        binding.setCurrentSelection(currentSelector);

        String[] uids = viewModel.uid().split("\\.");
        Bindings.setObjectStyle(binding.icon, itemView, uids[1]);

        if(viewModel.value()!=null && !viewModel.value().equals(currentSelector.get()))
            currentSelector.set(viewModel.value());

        if (viewModel.warning() != null) {
            binding.errorMessage.setVisibility(View.VISIBLE);
            binding.errorMessage.setText(viewModel.warning());
        } else if (viewModel.error() != null) {
            binding.errorMessage.setVisibility(View.VISIBLE);
            binding.errorMessage.setText(viewModel.error());
        } else {
            binding.errorMessage.setVisibility(View.GONE);
            binding.errorMessage.setText(null);
        }
    }

    public void dispose() {
        disposable.clear();
    }
}
