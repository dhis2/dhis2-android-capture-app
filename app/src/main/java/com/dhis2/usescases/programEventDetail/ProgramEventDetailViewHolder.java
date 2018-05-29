package com.dhis2.usescases.programEventDetail;

import android.support.v7.widget.RecyclerView;

import com.android.databinding.library.baseAdapters.BR;
import com.dhis2.databinding.ItemProgramEventBinding;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Cristian on 13/02/2018.
 */

public class ProgramEventDetailViewHolder extends RecyclerView.ViewHolder {

    private ItemProgramEventBinding binding;
    private CompositeDisposable disposable;

    public ProgramEventDetailViewHolder(ItemProgramEventBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        disposable = new CompositeDisposable();
    }

    public void bind(ProgramEventDetailContract.Presenter presenter, EventModel event) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.event, event);
        binding.executePendingBindings();

        disposable.add(presenter.getEventDataValue(event)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        trackedEntityDataValueModels -> {
                            StringBuilder stringBuilder = new StringBuilder("");
                            for (TrackedEntityDataValueModel dataValue : trackedEntityDataValueModels) {
                                if (dataValue.value() != null)
                                    stringBuilder.append(dataValue.value()).append("\n");
                            }
                            binding.dataValue.setText(stringBuilder);
                        },
                        Timber::d
                ));

        itemView.setOnClickListener(view -> presenter.onEventClick(event.uid()));
    }


}
