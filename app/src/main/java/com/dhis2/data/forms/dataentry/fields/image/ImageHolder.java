package com.dhis2.data.forms.dataentry.fields.image;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.dhis2.Bindings.Bindings;
import com.dhis2.data.forms.dataentry.fields.RowAction;
import com.dhis2.databinding.FormImageBinding;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.processors.FlowableProcessor;

/**
 * QUADRAM. Created by ppajuelo on 31/05/2018.
 */

public class ImageHolder extends RecyclerView.ViewHolder {

    private final CompositeDisposable disposable;
    private final FlowableProcessor<RowAction> processor;
    private final FormImageBinding binding;

    @NonNull
    private BehaviorProcessor<ImageViewModel> model;

    public ImageHolder(FormImageBinding mBinding, FlowableProcessor<RowAction> processor, boolean isBackgroundTransparent, String renderType, View rootView) {
        super(rootView != null ? rootView : mBinding.getRoot());
        this.processor = processor;
        this.binding = mBinding;
        this.disposable = new CompositeDisposable();
        model = BehaviorProcessor.create();
        disposable.add(model.subscribe(viewModel -> {
                    binding.setLabel(viewModel.label());
                    String[] uids = viewModel.uid().split("\\.");
                    Bindings.setObjectStyle(binding.icon, itemView, uids[1]);
                    if (viewModel.value() != null && viewModel.value().equals(viewModel.label()))
                        binding.frame.setVisibility(View.VISIBLE);
                    else
                        binding.frame.setVisibility(View.GONE);
                }
                , t -> Log.d("DHIS_ERROR", t.getMessage())));

        itemView.setOnClickListener(v -> {
            binding.frame.setVisibility(View.VISIBLE);
            String[] uids = model.getValue().uid().split("\\.");
            String value = model.getValue().label();
            processor.onNext(RowAction.create(uids[0], value));
        });

    }

    public void update(ImageViewModel viewModel) {
        model.onNext(viewModel);
    }
}
