package org.dhis2.utils;

import org.dhis2.R;

/**
 * Created by ppajuelo on 16/01/2018.
 */

public enum Period {
    NONE(R.string.period),
    DAILY (R.string.DAILY),
    WEEKLY(R.string.WEEKLY),
    MONTHLY(R.string.MONTHLY),
    YEARLY(R.string.YEARLY);

    private final int name;

    private Period(int id){
        name = id;
    }

    public int getNameResouce(){
        return this.name;
    }

}
