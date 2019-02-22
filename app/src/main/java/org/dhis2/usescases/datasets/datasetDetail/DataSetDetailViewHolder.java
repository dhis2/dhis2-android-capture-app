package org.dhis2.usescases.datasets.datasetDetail;

import org.dhis2.BR;
import org.dhis2.databinding.ItemDatasetBinding;

import androidx.recyclerview.widget.RecyclerView;

public class DataSetDetailViewHolder extends RecyclerView.ViewHolder {

    private ItemDatasetBinding binding;

    public DataSetDetailViewHolder(ItemDatasetBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    @SuppressWarnings("squid:CommentedOutCodeLine")
    public void bind(DataSetDetailContract.DataSetDetailPresenter presenter, DataSetDetailModel dataset) {
        binding.setVariable(BR.presenter, presenter);
        binding.setVariable(BR.dataset, dataset);
        binding.executePendingBindings();
        //TODO revisar para que sirve esto
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
