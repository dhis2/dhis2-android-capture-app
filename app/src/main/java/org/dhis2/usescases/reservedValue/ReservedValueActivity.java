package org.dhis2.usescases.reservedValue;

import android.os.Bundle;

import org.dhis2.App;
import org.dhis2.BR;
import org.dhis2.R;
import org.dhis2.databinding.ActivityReservedValueBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.utils.Constants;
import org.dhis2.utils.custom_views.CustomDialog;

import java.util.List;

import javax.inject.Inject;

import androidx.databinding.DataBindingUtil;

public class ReservedValueActivity extends ActivityGlobalAbstract implements ReservedValueContracts.View {

    private ActivityReservedValueBinding reservedBinding;
    private ReservedValueAdapter adapter;
    @Inject
    ReservedValueContracts.Presenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new ReservedValueModule(this)).inject(this);
        super.onCreate(savedInstanceState);

        reservedBinding = DataBindingUtil.setContentView(this, R.layout.activity_reserved_value);
        reservedBinding.setVariable(BR.presenter, presenter);
        adapter = new ReservedValueAdapter(presenter);
    }

    @Override
    public void setDataElements(List<ReservedValueModel> reservedValueModels) {
        if (reservedBinding.recycler.getAdapter() == null) {
            reservedBinding.recycler.setAdapter(adapter);
        }
        adapter.setDataElements(reservedValueModels);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.init(this);
    }

    @Override
    public void onBackClick() {
        super.onBackPressed();
    }

    @Override
    public void refreshAdapter() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void showReservedValuesError() {
        runOnUiThread(() -> new CustomDialog(
                getAbstracContext(),
                getString(R.string.error),
                getString(R.string.no_reserved_values),
                getString(R.string.action_accept),
                null,
                Constants.DESCRIPTION_DIALOG,
                null
        ).show());
    }
}
