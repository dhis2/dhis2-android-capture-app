package org.dhis2.data.forms.dataentry.tablefields.image;

import android.view.View;

import org.dhis2.data.forms.dataentry.tablefields.FormViewHolder;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.databinding.FormImageBinding;

import java.util.ArrayList;
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
    private final FlowableProcessor<RowAction> processor;
    private final FormImageBinding binding;
    private boolean isEditable;

    ImageViewModel model;

    public ImageHolder(FormImageBinding mBinding, FlowableProcessor<RowAction> processor, boolean isBackgroundTransparent, String renderType, View rootView, FlowableProcessor<String> imageSelector) {
        super(mBinding);
        this.processor = processor;
        this.binding = mBinding;
        this.disposable = new CompositeDisposable();

        if (imageSelector != null)
            disposable.add(imageSelector
                    .debounce(1000, TimeUnit.MILLISECONDS, Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(selectedValue -> {

                    }, Timber::d));

        itemView.setOnClickListener(v -> {

            if (isEditable) {
                String value = null;
                String[] uids = model.uid().split("\\.");
                value = model.label();

                if (imageSelector != null)
                    imageSelector.onNext(value);
                processor.onNext(RowAction.create(uids[0], value, "", "","", 0, 0));

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
