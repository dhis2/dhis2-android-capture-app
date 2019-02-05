package org.dhis2.usescases.datasets.datasetDetail;

import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.BR;
import org.dhis2.databinding.ItemDatasetBinding;

import io.reactivex.disposables.CompositeDisposable;

public class DataSetDetailViewHolder extends RecyclerView.ViewHolder{

    private ItemDatasetBinding binding;
    private CompositeDisposable disposable;

    public DataSetDetailViewHolder(ItemDatasetBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        disposable = new CompositeDisposable();
    }

    public void bind(DataSetDetailContract.Presenter presenter, DataSetDetailModel dataset) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.dataset, dataset);
        binding.executePendingBindings();
        //FIXME revisar para que sirve esto
        /*disposable.add(presenter.getDataSetDataValueNew(dataset)
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
                ));*/

//        itemView.setOnClickListener(view -> presenter.onDataSetClick(dataset.getUidDataSet(),null));
    }
}
