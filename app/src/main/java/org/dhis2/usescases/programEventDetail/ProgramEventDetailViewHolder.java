package org.dhis2.usescases.programEventDetail;

import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.BR;

import org.dhis2.databinding.ItemProgramEventBinding;
import org.hisp.dhis.android.core.event.EventModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by Cristian on 13/02/2018.
 */

public class ProgramEventDetailViewHolder extends RecyclerView.ViewHolder {

    private ItemProgramEventBinding binding;

    public ProgramEventDetailViewHolder(ItemProgramEventBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(ProgramEventDetailContract.Presenter presenter, ProgramEventViewModel event) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.event, event);
        binding.executePendingBindings();

        StringBuilder stringBuilder = new StringBuilder("");
        int valuesSize = event.eventDisplayData().size() > 3 ? 3 : event.eventDisplayData().size();
        for (int i = 0; i < valuesSize; i++) {
            if (event.eventDisplayData().get(i) != null)
                stringBuilder.append(event.eventDisplayData().get(i).val1());
            if (i != valuesSize - 1)
                stringBuilder.append("\n");
        }
        binding.dataValue.setText(stringBuilder);

        itemView.setOnClickListener(view -> presenter.onEventClick(event.uid(), event.orgUnitUid()));
    }


}
