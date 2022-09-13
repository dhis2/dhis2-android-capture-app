package org.dhis2.commons.data;

public interface DataEntryStore {

    enum valueType {
        ATTR, DATA_ELEMENT
    }

    enum EntryMode {
        DE, ATTR,DV
    }
}
