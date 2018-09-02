package com.dhis2.usescases.datasets.datasetDetail;

import android.content.Context;

import com.dhis2.utils.Period;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

public enum DataSetDetailPeriodEnum {

    MONTHLY("Monthly") {
        public List<DataSetDetailModel> getListDataSetWithPeriods(int year, DataSetDetailModel dataset){
            List<DataSetDetailModel> listDataSet = new ArrayList<>();
            if(year <= maxDate().year().get()){
                int index = 0;
                for(int i = 1; i<= maxDate().monthOfYear().get(); i++){
                    listDataSet.add(new DataSetDetailModel(dataset.getUidDataSet(),
                            dataset.getNameOrgUnit(),dataset.getNameCatCombo(),
                            new LocalDate(year, i,1).toString()));

                }
            }

            return listDataSet;
        }
    };

    public abstract List<DataSetDetailModel> getListDataSetWithPeriods(int year, DataSetDetailModel dataset);

    private String periodTypeName;

    DataSetDetailPeriodEnum(String periodTypeName) {
        this.periodTypeName = periodTypeName;
    }

    public String getPeriodTypeName() {
        return periodTypeName;
    }

    public static DataSetDetailPeriodEnum getDataSetPeriod(String namePeriod){

        for(DataSetDetailPeriodEnum dataSetDetailPeriodEnum: DataSetDetailPeriodEnum.values()){
            if(dataSetDetailPeriodEnum.getPeriodTypeName().equals(namePeriod)){
                return dataSetDetailPeriodEnum;
            }
        }

        return null;
    }

    private static DateTime maxDate(){
        return new LocalDate().toDateTimeAtCurrentTime();
    }


}
