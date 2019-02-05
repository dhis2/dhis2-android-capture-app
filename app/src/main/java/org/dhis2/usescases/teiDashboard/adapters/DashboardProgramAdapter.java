package org.dhis2.usescases.teiDashboard.adapters;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.ItemDashboardProgramBinding;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts;

/**
 * Created by ppajuelo on 27/02/2018.
 *
 */

public class DashboardProgramAdapter extends RecyclerView.Adapter<DashboardProgramViewHolder> {

    private final TeiDashboardContracts.Presenter presenter;
    private DashboardProgramModel dashboardProgramModel;

    public DashboardProgramAdapter(TeiDashboardContracts.Presenter presenter, DashboardProgramModel program) {
        this.dashboardProgramModel = program;
        this.presenter = presenter;
    }

    @NonNull
    @Override
    public DashboardProgramViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDashboardProgramBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_dashboard_program, parent, false);
        return new DashboardProgramViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardProgramViewHolder holder, int position) {
        holder.bind(presenter, dashboardProgramModel, position);
    }

    @Override
    public int getItemCount() {
        return dashboardProgramModel.getEnrollmentProgramModels().size();
    }
}
