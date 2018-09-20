package org.dhis2.usescases.datasets.datasetDetail;

public class DataSetDetailModel {

    private String uidDataSet;
    private String nameOrgUnit;
    private String nameCatCombo;
    private String namePeriod;

    public DataSetDetailModel(String uidDataSet, String nameOrgUnit, String nameCatCombo, String namePeriod) {
        this.uidDataSet = uidDataSet;
        this.nameOrgUnit = nameOrgUnit;
        this.nameCatCombo = nameCatCombo;
        this.namePeriod = namePeriod;
    }

    public String getUidDataSet() {
        return uidDataSet;
    }

    public void setUidDataSet(String uidDataSet) {
        this.uidDataSet = uidDataSet;
    }

    public String getNameOrgUnit() {
        return nameOrgUnit;
    }

    public void setNameOrgUnit(String nameOrgUnit) {
        this.nameOrgUnit = nameOrgUnit;
    }

    public String getNameCatCombo() {
        return nameCatCombo;
    }

    public void setNameCatCombo(String nameCatCombo) {
        this.nameCatCombo = nameCatCombo;
    }

    public String getNamePeriod() {
        return namePeriod;
    }

    public void setNamePeriod(String namePeriod) {
        this.namePeriod = namePeriod;
    }
}
