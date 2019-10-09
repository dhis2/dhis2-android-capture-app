package org.dhis2.utils.granular_sync;

import com.gabrielittner.auto.value.cursor.ColumnName;
import com.google.auto.value.AutoValue;

import java.util.Date;


@AutoValue
public abstract class StatusLogItem {
    public static class Columns {
        public static final String DATE = "DATE";
        public static final String DESCRIPTION = "DESCRIPTION";
    }

    public static StatusLogItem create(Date date, String description){
        return new AutoValue_StatusLogItem(date, description);
    }

    @ColumnName(Columns.DATE)
    public abstract Date date();

    @ColumnName(Columns.DESCRIPTION)
    public abstract String description();

}
