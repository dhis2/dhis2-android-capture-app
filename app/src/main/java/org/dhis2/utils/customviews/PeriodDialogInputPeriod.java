package org.dhis2.utils.customviews;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import org.dhis2.R;
import org.dhis2.databinding.DialogPeriodDatesBinding;
import org.dhis2.usescases.datasets.datasetInitial.DateRangeInputPeriodModel;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import kotlin.Unit;

public class PeriodDialogInputPeriod extends PeriodDialog {

    private List<DateRangeInputPeriodModel> inputPeriod;
    private Integer openFuturePeriods;
    DialogPeriodDatesBinding binding;
    private OrganisationUnit organisationUnit;

    public PeriodDialogInputPeriod setInputPeriod(List<DateRangeInputPeriodModel> inputPeriod) {
        this.inputPeriod = sortInputPeriod(inputPeriod);
        return this;
    }

    public PeriodDialogInputPeriod setOpenFuturePeriods(Integer openFuturePeriods) {
        this.openFuturePeriods = openFuturePeriods;
        return this;
    }

    public PeriodDialogInputPeriod setOrgUnit(OrganisationUnit organisationUnit) {
        this.organisationUnit = organisationUnit;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_period_dates, container, false);

        binding.title.setText(getTitle());
        binding.clearButton.setOnClickListener(view -> {
            getNegativeListener().onClick(view);
            dismiss();
        });
        binding.cancelButton.setOnClickListener(view -> dismiss());

        boolean isEmpty = inputPeriod.isEmpty();

        deleteUnallowedPeriodForToday();

        binding.periodSubtitle.setText(getPeriod().name());
        boolean isAllowed = false;

        for (DateRangeInputPeriodModel inputPeriodModel : inputPeriod) {
            do {
                if ((getCurrentDate().after(inputPeriodModel.initialPeriodDate()) || getCurrentDate ().equals(inputPeriodModel.initialPeriodDate())) && getCurrentDate().before(inputPeriodModel.endPeriodDate())
                        && (inputPeriodModel.openingDate() == null || (inputPeriodModel.openingDate() != null && (DateUtils.getInstance().getToday().after(inputPeriodModel.openingDate())))
                        || DateUtils.getInstance().getToday().equals(inputPeriodModel.openingDate()))
                        && (inputPeriodModel.closingDate() == null || (inputPeriodModel.closingDate() != null && DateUtils.getInstance().getToday().before(inputPeriodModel.closingDate())))
                        && (organisationUnit == null || (organisationUnit.openingDate() == null || getCurrentDate().after(organisationUnit.openingDate())) &&
                                (organisationUnit.closedDate() == null || getCurrentDate().before(organisationUnit.closedDate()))))
                    isAllowed = true;
                else if (getCurrentDate().before(inputPeriod.get(inputPeriod.size() - 1).initialPeriodDate()) ||
                        getCurrentDate().before(inputPeriodModel.initialPeriodDate()))
                    break;
                else
                    previousInputPeriod();
            } while (!isAllowed);
            if (isAllowed) break;
        }

        if (!isAllowed && !isEmpty) {
            binding.noPeriods.setText(getString(R.string.there_is_no_available_period));
        }else{
            boolean withInputPeriod = false;

            if(isAllowed && !isEmpty){
                withInputPeriod = true;
            }

            PeriodAdapter periodAdapter = new PeriodAdapter(
                    getPeriod(),
                    openFuturePeriods != null ? openFuturePeriods : 0,
                    date -> {
                        getPossitiveListener().onDateSet(date);
                        return Unit.INSTANCE;
                    },
                    withInputPeriod,
                    organisationUnit,
                    inputPeriod);
            binding.recyclerDate.setAdapter(periodAdapter);

        }

        return binding.getRoot();
    }

    private List<DateRangeInputPeriodModel> sortInputPeriod(List<DateRangeInputPeriodModel> inputPeriod) {
        Collections.sort(inputPeriod, (dateRangeInputPeriodModel, t1) -> dateRangeInputPeriodModel.initialPeriodDate().before(t1.initialPeriodDate()) ? 1 : -1);
        return inputPeriod;
    }

    private void deleteUnallowedPeriodForToday() {
        List<DateRangeInputPeriodModel> helperList = new ArrayList<>();
        for (DateRangeInputPeriodModel inputPeriodModel : inputPeriod) {
            if ((inputPeriodModel.openingDate() == null || (inputPeriodModel.openingDate() != null && DateUtils.getInstance().getToday().after(inputPeriodModel.openingDate())))
                    && (inputPeriodModel.closingDate() == null || (inputPeriodModel.closingDate() != null && DateUtils.getInstance().getToday().before(inputPeriodModel.closingDate())))) {
                helperList.add(inputPeriodModel);
            }
        }

        inputPeriod.clear();
        inputPeriod.addAll(helperList);

    }
}
