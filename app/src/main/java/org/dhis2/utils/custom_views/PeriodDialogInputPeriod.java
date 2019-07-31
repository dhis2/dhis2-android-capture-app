package org.dhis2.utils.custom_views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.usescases.datasets.datasetInitial.DateRangeInputPeriodModel;
import org.dhis2.utils.DateUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

public class PeriodDialogInputPeriod extends PeriodDialog {

    private List<DateRangeInputPeriodModel> inputPeriod;
    private Integer openFuturePeriods;

    public PeriodDialogInputPeriod setInputPeriod(List<DateRangeInputPeriodModel> inputPeriod) {
        this.inputPeriod = sortInputPeriod(inputPeriod);
        return this;
    }

    public PeriodDialogInputPeriod setOpenFuturePeriods(Integer openFuturePeriods){
        this.openFuturePeriods = openFuturePeriods;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_period, container, false);

        binding.title.setText(getTitle());
        binding.acceptButton.setOnClickListener(view -> {

            getPossitiveListener().onDateSet(getCurrentDate());
            dismiss();
        });
        binding.clearButton.setOnClickListener(view -> dismiss());
        if(inputPeriod.size()==0)
            setDateWithOpenFuturePeriod(openFuturePeriods);

        boolean isEmpty = inputPeriod.isEmpty();

        deleteUnallowedPeriodForToday();

        binding.periodSubtitle.setText(getPeriod().name());
        boolean isAllowed = false;

        for (DateRangeInputPeriodModel inputPeriodModel : inputPeriod) {
            do{
                if ((getCurrentDate().after(inputPeriodModel.initialPeriodDate()) || getCurrentDate().equals(inputPeriodModel.initialPeriodDate())) && getCurrentDate().before(inputPeriodModel.endPeriodDate())
                        && (inputPeriodModel.openingDate() == null || (inputPeriodModel.openingDate() != null && (DateUtils.getInstance().getToday().after(inputPeriodModel.openingDate())))
                        || DateUtils.getInstance().getToday().equals(inputPeriodModel.openingDate()))
                        && (inputPeriodModel.closingDate() == null || (inputPeriodModel.closingDate() != null && DateUtils.getInstance().getToday().before(inputPeriodModel.closingDate()))))
                    isAllowed = true;
                else if(getCurrentDate().before(inputPeriod.get(inputPeriod.size()-1).initialPeriodDate()) ||
                        getCurrentDate().before(inputPeriodModel.initialPeriodDate()))
                    break;
                else
                    previousPeriod();
            }while (!isAllowed );
            if(isAllowed) break;
        }

        if(!isAllowed && !isEmpty) {
            binding.selectedPeriod.setText(getString(R.string.there_is_no_available_period));
            binding.acceptButton.setVisibility(View.GONE);
            binding.periodBefore.setVisibility(View.INVISIBLE);
        }else {
            binding.selectedPeriod.setText(DateUtils.getInstance().getPeriodUIString(getPeriod(), getCurrentDate(), Locale.getDefault()));

            binding.periodBefore.setOnClickListener(view -> {

                boolean isPreviousAllowed = false, isLast = false;
                Date currentDate =  getCurrentDate();
                previousPeriod();
                for (DateRangeInputPeriodModel inputPeriodModel : inputPeriod) {
                    do {
                        if (getCurrentDate().after(inputPeriodModel.initialPeriodDate()) && getCurrentDate().before(inputPeriodModel.endPeriodDate())
                                && (inputPeriodModel.openingDate() == null || (inputPeriodModel.openingDate() != null && DateUtils.getInstance().getToday().after(inputPeriodModel.openingDate())))
                                && (inputPeriodModel.closingDate() == null || (inputPeriodModel.closingDate() != null && DateUtils.getInstance().getToday().before(inputPeriodModel.closingDate())))) {
                            isPreviousAllowed = true;
                            if(checkIsLast(inputPeriodModel))
                                isLast = true;
                        }
                        else if (getCurrentDate().before(inputPeriodModel.initialPeriodDate()))
                            break;
                        else
                            previousPeriod();
                    } while (!isPreviousAllowed);
                    if (isPreviousAllowed)
                        break;
                }

                checkConstraintDates();
                if(isLast)
                    binding.periodBefore.setVisibility(View.INVISIBLE);

                if (inputPeriod.size() != 0 && !isPreviousAllowed) {
                    setRealCurrentDate(currentDate);
                    binding.selectedPeriod.setText(DateUtils.getInstance().getPeriodUIString(getPeriod(), currentDate, Locale.getDefault()));
                    binding.periodBefore.setEnabled(false);
                }
            });
            binding.periodNext.setOnClickListener(view -> {

                boolean isNextAllowed = false, isLast = false;
                Date currentDate =  getCurrentDate();
                nextPeriod();
                List<DateRangeInputPeriodModel> reversePeriods = new ArrayList<>(inputPeriod);
                Collections.reverse(reversePeriods);
                for (DateRangeInputPeriodModel inputPeriodModel : reversePeriods) {
                    do {
                        if (getCurrentDate().after(inputPeriodModel.initialPeriodDate()) && getCurrentDate().before(inputPeriodModel.endPeriodDate())
                                && (inputPeriodModel.openingDate() == null || (inputPeriodModel.openingDate() != null && DateUtils.getInstance().getToday().after(inputPeriodModel.openingDate())))
                                && (inputPeriodModel.closingDate() == null || (inputPeriodModel.closingDate() != null && DateUtils.getInstance().getToday().before(inputPeriodModel.closingDate())))) {
                            isNextAllowed = true;
                            if(checkIsLast(inputPeriodModel))
                                isLast = true;
                        }
                        else if (getCurrentDate().after(inputPeriodModel.endPeriodDate()))
                            break;
                        else
                            nextPeriod();
                    } while (!isNextAllowed);
                    if (isNextAllowed)
                        break;
                }
                checkConstraintDates();

                if(isLast)
                    binding.periodNext.setVisibility(View.INVISIBLE);

                if (inputPeriod.size() != 0 && !isNextAllowed) {
                    setRealCurrentDate(currentDate);
                    binding.selectedPeriod.setText(DateUtils.getInstance().getPeriodUIString(getPeriod(), currentDate, Locale.getDefault()));
                    binding.periodNext.setEnabled(false);
                }
            });
        }
        binding.periodNext.setVisibility(View.INVISIBLE);
        return binding.getRoot();
    }

    private List<DateRangeInputPeriodModel> sortInputPeriod(List<DateRangeInputPeriodModel> inputPeriod){
        Collections.sort(inputPeriod, (dateRangeInputPeriodModel, t1) -> dateRangeInputPeriodModel.initialPeriodDate().before(t1.initialPeriodDate()) ? 1:-1);
        return inputPeriod;
    }

    private void deleteUnallowedPeriodForToday(){
        List<DateRangeInputPeriodModel> helperList = new ArrayList<>();
        for(DateRangeInputPeriodModel inputPeriodModel: inputPeriod){
            if((inputPeriodModel.openingDate() == null || (inputPeriodModel.openingDate() != null && DateUtils.getInstance().getToday().after(inputPeriodModel.openingDate())))
                    && (inputPeriodModel.closingDate() == null || (inputPeriodModel.closingDate() != null && DateUtils.getInstance().getToday().before(inputPeriodModel.closingDate())))){
                helperList.add(inputPeriodModel);
            }
        }

        inputPeriod.clear();
        inputPeriod.addAll(helperList);

    }

    private boolean checkIsLast(DateRangeInputPeriodModel dateRangeInputPeriodModel){
        for(int i = 0; i < inputPeriod.size(); i++){
            if(inputPeriod.get(i).period().equals(dateRangeInputPeriodModel.period()) && (i == 0 || i == inputPeriod.size() - 1)){
                return true;
            }
        }

        return false;
    }

}
