package com.dhis2.data.forms.dataentry.fields.image;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.dhis2.Bindings.Bindings;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormImageBinding;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 31/05/2018.
 */

public class ImageHolder extends RecyclerView.ViewHolder {

    private final CompositeDisposable disposable;
    private final FlowableProcessor<RowAction> processor;
    private final FormImageBinding binding;
    private boolean isEditable;
    private String valuePendingUpdate;
    @NonNull
    private BehaviorProcessor<ImageViewModel> model;

    public ImageHolder(FormImageBinding mBinding, FlowableProcessor<RowAction> processor, boolean isBackgroundTransparent, String renderType, View rootView, FlowableProcessor<String> imageSelector) {
        super(rootView != null ? rootView : mBinding.getRoot());
        this.processor = processor;
        this.binding = mBinding;
        this.disposable = new CompositeDisposable();
        model = BehaviorProcessor.create();

        if (imageSelector != null)
            disposable.add(imageSelector
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(selectedValue -> {
                        if (selectedValue.equals(model.getValue().value())||selectedValue.equals(valuePendingUpdate))
                            binding.frame.setVisibility(View.VISIBLE);
                        else
                            binding.frame.setVisibility(View.GONE);
                    }, Timber::d));

        disposable.add(model.subscribe(viewModel -> {
                    this.isEditable = viewModel.editable();
                    StringBuilder label = new StringBuilder(viewModel.label());
                    if (viewModel.mandatory())
                        label.append("*");
                    binding.setLabel(label.toString());
                    String[] uids = viewModel.uid().split("\\.");
                    Bindings.setObjectStyle(binding.icon, itemView, uids[1]);
                    if (viewModel.value() != null && viewModel.value().equals(viewModel.label()))
                        binding.frame.setVisibility(View.VISIBLE);
                    else
                        binding.frame.setVisibility(View.GONE);

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
                , t -> Log.d("DHIS_ERROR", t.getMessage())));

        itemView.setOnClickListener(v -> {

            if (isEditable) {
                String value = null;
                String[] uids = model.getValue().uid().split("\\.");
                /*if (binding.frame.getVisibility() == View.GONE) {
                    binding.frame.setVisibility(View.VISIBLE);
                    value = model.getValue().label();
                } else {
                    binding.frame.setVisibility(View.GONE);
                }*/
                value = model.getValue().label();
                valuePendingUpdate = value;
                binding.frame.setVisibility(View.VISIBLE);
                if (imageSelector != null)
                    imageSelector.onNext(value);
                processor.onNext(RowAction.create(uids[0], value));
            }
        });

    }

    public void update(ImageViewModel viewModel) {
        model.onNext(viewModel);
    }

    public void dispose() {
        disposable.clear();
    }
}
