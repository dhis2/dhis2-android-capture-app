package com.dhis2.usescases.searchTrackEntity.tableHolder;

import android.view.View;

import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.databinding.ItemTableCellAttrBinding;
import com.dhis2.databinding.ItemTableCellProgramBinding;
import com.dhis2.usescases.searchTrackEntity.SearchTEContractsModule;
import com.evrencoskun.tableview.adapter.recyclerview.holder.AbstractViewHolder;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ppajuelo on 07/03/2018.
 */

public class CellHolder extends AbstractViewHolder {

    private ItemTableCellAttrBinding attrBinding;
    private ItemTableCellProgramBinding programbinding;
    private CompositeDisposable compositeDisposable;

    public CellHolder(ItemTableCellAttrBinding binding) {
        super(binding.getRoot());
        this.attrBinding = binding;
        compositeDisposable = new CompositeDisposable();

    }

    public CellHolder(ItemTableCellProgramBinding binding) {
        super(binding.getRoot());
        this.programbinding = binding;
        compositeDisposable = new CompositeDisposable();

    }

    public void bind(SearchTEContractsModule.Presenter presenter, TrackedEntityInstanceModel trackedEntityInstanceModel, MetadataRepository metadata, int p_nYPosition) {
        attrBinding.setPosition(p_nYPosition);
        if (presenter.getProgramModel() == null)
            compositeDisposable.add(
                    metadata.getTEIAttributeValues(trackedEntityInstanceModel.uid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::setTEIData)

            );
        else
            compositeDisposable.add(
                    metadata.getTEIAttributeValues(presenter.getProgramModel().uid(), trackedEntityInstanceModel.uid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::setTEIData)
            );
    }

    public void bind(TrackedEntityInstanceModel trackedEntityInstanceModel, ProgramModel programModel, MetadataRepository metadata, int p_nYPosition) {
        programbinding.setPosition(p_nYPosition);
        //--------------------------
        //region ENROLLMENTS

        compositeDisposable.add(
                metadata.getTEIEnrollments(trackedEntityInstanceModel.uid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(data -> setEnrollment(data, programModel))

        );

        //endregion

    }

    private void setEnrollment(List<EnrollmentModel> enrollments, ProgramModel program) {

        for (EnrollmentModel enrollment : enrollments) {
            if (enrollment.program().equals(program.uid()) &&
                    (enrollment.enrollmentStatus() == EnrollmentStatus.ACTIVE))
                programbinding.setEnrollment(enrollment);
            else if (enrollment.program().equals(program.uid()) &&
                    (enrollment.enrollmentStatus() != EnrollmentStatus.ACTIVE && !program.onlyEnrollOnce())) {
                programbinding.setEnrollment(enrollment);
            } else {
                programbinding.addProgram.setVisibility(View.VISIBLE);
                programbinding.enrollmentLayout.getRoot().setVisibility(View.GONE);
            }
        }

        programbinding.executePendingBindings();

    }

    private void setTEIData(List<TrackedEntityAttributeValueModel> trackedEntityAttributeValueModels) {

        String attrText = trackedEntityAttributeValueModels.get(0).value();
        if (trackedEntityAttributeValueModels.size() > 1)
            attrText += "\n" + trackedEntityAttributeValueModels.get(1).value();
        if (trackedEntityAttributeValueModels.size() > 2)
            attrText += "\n" + trackedEntityAttributeValueModels.get(2).value();

        attrBinding.setAttr(attrText);
        attrBinding.executePendingBindings();
    }


}
