package org.dhis2.utils;

import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;

import java.util.Comparator;

public class CustomComparator implements Comparator<ProgramTrackedEntityAttribute> {
    @Override
    public int compare(ProgramTrackedEntityAttribute o1, ProgramTrackedEntityAttribute o2) {
        return o1.sortOrder().compareTo(o2.sortOrder());
    }
}