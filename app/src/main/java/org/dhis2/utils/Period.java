package org.dhis2.utils;

import org.dhis2.R;

public enum Period {
    NONE(R.string.period),
    DAILY (R.string.DAILY),
    WEEKLY(R.string.WEEKLY),
    MONTHLY(R.string.MONTHLY),
    YEARLY(R.string.YEARLY);

    /**
     * QUADRAM. Created by ppajuelo on 16/01/2018.
     */
    private final int name;

    Period(int id){
        name = id;
    }

    public int getNameResouce(){
        return this.name;
    }
}
