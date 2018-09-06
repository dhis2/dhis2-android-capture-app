package org.dhis2.usescases.main.program;

import org.hisp.dhis.android.core.data.database.EnumColumnAdapter;

class HomeViewModelTypeColumnAdapter extends EnumColumnAdapter<HomeViewModel.Type> {
    @Override
    protected Class getEnumClass() {
        return HomeViewModel.Type.class;
    }
}
