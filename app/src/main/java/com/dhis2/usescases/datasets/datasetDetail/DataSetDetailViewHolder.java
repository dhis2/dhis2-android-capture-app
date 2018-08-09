package com.dhis2.usescases.datasets.datasetDetail;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.android.databinding.library.baseAdapters.BR;
import com.dhis2.databinding.ItemDatasetBinding;
import com.dhis2.usescases.programEventDetail.ProgramEventDetailContract;

import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.event.EventModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DataSetDetailViewHolder extends RecyclerView.ViewHolder{

    private ItemDatasetBinding binding;
    private CompositeDisposable disposable;

    public DataSetDetailViewHolder(ItemDatasetBinding itemView) {
        super(itemView);
        binding = itemView;
        disposable = new CompositeDisposable();
    }

    public void bind(DataSetDetailContract.Presenter presenter, DataSetModel dataset) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.dataset, dataset);
        binding.executePendingBindings();
        //FIXME revisar para que sirve esto
        disposable.add(presenter.getDataSetDataValueNew(dataset)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        values -> {
                            StringBuilder stringBuilder = new StringBuilder("");
                            int valuesSize = values.size() > 3 ? 3 : values.size();
                            for (int i = 0; i < valuesSize; i++) {
                                if (values.get(i) != null)
                                    stringBuilder.append(values.get(i)).append("\n");
                            }
                            binding.dataValue.setText(stringBuilder);
                        },
                        Timber::d
                ));

        itemView.setOnClickListener(view -> presenter.onDataSetClick(dataset.uid(),/* dataset.organisationUnit() viene de otro lllaaaauuu*/null));
    }
}
