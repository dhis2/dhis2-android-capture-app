package org.dhis2.usescases.datasets.datasetDetail;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;

public enum DataSetDetailPeriodEnum {

    DAILY("Daily") {
        public List<DataSetDetailModel> getListDataSetWithPeriods(int year, DataSetDetailModel dataset){
            List<DataSetDetailModel> listDataSet = new ArrayList<>();
            /*if(year <= maxDate().year().get()){

                for(int month = 1; month<= maxDate().monthOfYear().get(); month++){
                    DateTime currentMonth = new DateTime().withMonthOfYear(month);
                    for(int day = 1; day< currentMonth.dayOfMonth().withMaximumValue().getDayOfMonth(); day++){
                        if(month == maxDate().monthOfYear().get() && day == maxDate().dayOfMonth().get()){
                            break;
                        }

                        listDataSet.add(new DataSetDetailModel(dataset.getUidDataSet(),
                                dataset.getNameOrgUnit(),dataset.getNameCatCombo(),
                                new LocalDate( year, month, day).toString()));
                    }

                }
            }*/

            return listDataSet;
        }
    },

    WEEKLY("Weekly") {
        public List<DataSetDetailModel> getListDataSetWithPeriods(int year, DataSetDetailModel dataset){
            return getListDataSetWithPeriodsWeeks(year, dataset, DateTimeConstants.MONDAY);
        }
    },

    WEEKLYWEDNESDAY("WeeklyWednesday") {
        public List<DataSetDetailModel> getListDataSetWithPeriods(int year, DataSetDetailModel dataset){
            return getListDataSetWithPeriodsWeeks(year, dataset, DateTimeConstants.WEDNESDAY);
        }
    },

    WEEKLYTHURSDAY("WeeklyThursday") {
        public List<DataSetDetailModel> getListDataSetWithPeriods(int year, DataSetDetailModel dataset){
            return getListDataSetWithPeriodsWeeks(year, dataset, DateTimeConstants.THURSDAY);
        }
    },

    WEEKLYSATURDAY("WeeklySaturday") {
        public List<DataSetDetailModel> getListDataSetWithPeriods(int year, DataSetDetailModel dataset){
            return getListDataSetWithPeriodsWeeks(year, dataset, DateTimeConstants.SATURDAY);
        }
    },

    WEEKLYSUNDAY("WeeklySunday") {
        public List<DataSetDetailModel> getListDataSetWithPeriods(int year, DataSetDetailModel dataset){
            return getListDataSetWithPeriodsWeeks(year, dataset, DateTimeConstants.SUNDAY);
        }
    },

    BIWEEKLY("BiWeekly"){
        public List<DataSetDetailModel> getListDataSetWithPeriods(int year, DataSetDetailModel dataset){
            List<DataSetDetailModel> listDataSet = new ArrayList<>();
            /*if(year <= maxDate().year().get()){
                int biweek = 1;
                for(int week = 1; week < maxDate().getWeekOfWeekyear(); week++){
                    DateTime currentWeek = new DateTime().withWeekOfWeekyear(week);
                    String date = "W"+ biweek + " " + new LocalDate(year, currentWeek.getMonthOfYear(),currentWeek.getDayOfMonth()).toString() + " - "
                            + new LocalDate(year, currentWeek.plusWeeks(2).getMonthOfYear(),currentWeek.plusWeeks(2).getDayOfMonth()-1).toString();
                    if(currentWeek.plusWeeks(2).getWeekOfWeekyear() >= maxDate().getWeekOfWeekyear()){
                        break;
                    }
                    listDataSet.add(new DataSetDetailModel(dataset.getUidDataSet(),
                            dataset.getNameOrgUnit(),dataset.getNameCatCombo(),
                            date));
                    biweek ++;
                    week ++;
                }
            }*/
            return listDataSet;
        }
    },

    MONTHLY("Monthly") {
        public List<DataSetDetailModel> getListDataSetWithPeriods(int year, DataSetDetailModel dataset){
            return getListDataSetWithPeriodsMonth(year, dataset, DateTimeConstants.JANUARY, 0);
        }
    },

    BIMONTHLY("BiMonthly") {
        public List<DataSetDetailModel> getListDataSetWithPeriods(int year, DataSetDetailModel dataset){
            return getListDataSetWithPeriodsMonth(year, dataset, DateTimeConstants.JANUARY, 1);
        }
    },
    QUATERLY("Quarterly") {
        public List<DataSetDetailModel> getListDataSetWithPeriods(int year, DataSetDetailModel dataset){
            return getListDataSetWithPeriodsMonth(year, dataset, DateTimeConstants.JANUARY, 2);
        }
    },
    SIXMONTHLY("SixMonthly") {
        public List<DataSetDetailModel> getListDataSetWithPeriods(int year, DataSetDetailModel dataset){
            return getListDataSetWithPeriodsMonth(year, dataset, DateTimeConstants.JANUARY, 5);
        }
    },
    SIXMONTHLYAPRIL("SixMonthlyApril") {
        public List<DataSetDetailModel> getListDataSetWithPeriods(int year, DataSetDetailModel dataset){
            return getListDataSetWithPeriodsMonth(year, dataset, DateTimeConstants.APRIL, 5);
        }
    },
    YEARLY("Yearly"){
        public List<DataSetDetailModel> getListDataSetWithPeriods(int currentYear, DataSetDetailModel dataset){
            List<DataSetDetailModel> listDataSet = new ArrayList<>();
           /* if(currentYear <= maxDate().year().get()){

                for(int year = currentYear; year > 2008; year--){
                    DateTime currentMonth = new DateTime().withYear(year);
                    String date = currentMonth.toString(DateTimeFormat.forPattern("yyyy"));
                    listDataSet.add(new DataSetDetailModel(dataset.getUidDataSet(),
                            dataset.getNameOrgUnit(),dataset.getNameCatCombo(),
                            date));

                }
            }*/
            return listDataSet;
        }
    }
    ;



    public abstract List<DataSetDetailModel> getListDataSetWithPeriods(int year, DataSetDetailModel dataset);

    public static List<DataSetDetailModel> getListDataSetWithPeriodsWeeks(int year, DataSetDetailModel dataset, int day){
        List<DataSetDetailModel> listDataSet = new ArrayList<>();
        /*if(year <= maxDate().year().get()){

            for(int week = 1; week < maxDate().getWeekOfWeekyear(); week++){
                DateTime currentWeek = new DateTime().withWeekOfWeekyear(week).withDayOfWeek(day);
                String date = "W"+ week + " " + new LocalDate(year, currentWeek.getMonthOfYear(),currentWeek.getDayOfMonth()).toString() + " - "
                        + new LocalDate(year, currentWeek.plusDays(6).getMonthOfYear(),currentWeek.plusDays(6).getDayOfMonth()).toString();
                if(currentWeek.plusDays(6).getWeekOfWeekyear() == maxDate().getWeekOfWeekyear()){
                    break;
                }
                listDataSet.add(new DataSetDetailModel(dataset.getUidDataSet(),
                        dataset.getNameOrgUnit(),dataset.getNameCatCombo(),
                        date));
            }
        }*/
        return listDataSet;
    }

    public static List<DataSetDetailModel> getListDataSetWithPeriodsMonth(int year, DataSetDetailModel dataset, int startMonth, int period){
        List<DataSetDetailModel> listDataSet = new ArrayList<>();
        /*if(year <= maxDate().year().get()){

            for(int month = 1; month< maxDate().monthOfYear().get(); month++){
                DateTime currentMonth = new DateTime().withMonthOfYear(month).withYear(year).withMonthOfYear(startMonth + month - 1);
                String date = currentMonth.toString("MMMM") + " - "
                        + currentMonth.plusMonths(period).toString(DateTimeFormat.forPattern("MMMM yyyy"));
                date = period == 0 ? currentMonth.plusMonths(period).toString(DateTimeFormat.forPattern("MMMM yyyy")): date;
                if(currentMonth.plusMonths(period).monthOfYear().get() >= maxDate().monthOfYear().get()){
                    break;
                }
                listDataSet.add(new DataSetDetailModel(dataset.getUidDataSet(),
                        dataset.getNameOrgUnit(),dataset.getNameCatCombo(),
                        date));

                month = month+period;
            }
        }*/

        return listDataSet;
    }

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
