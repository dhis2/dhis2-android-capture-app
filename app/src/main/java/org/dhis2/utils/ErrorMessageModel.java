package org.dhis2.utils;

import java.util.Date;

/**
 * QUADRAM. Created by ppajuelo on 25/10/2018.
 */


public class ErrorMessageModel {

    public static final String TABLE = "ErrorMessage";

    private Integer errorCode;
    private String errorMessage;
    private String errorDescription;
    private Date errorDate;

    public ErrorMessageModel(Integer errorCode, String errorMessage, String errorDescription, Date date) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorDescription = errorDescription;
        this.errorDate = date;
    }


    public Integer getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public Date getErrorDate() {
        return errorDate;
    }

    public String getFormattedDate() {
        return DateUtils.dateTimeFormat().format(errorDate);
    }
}
