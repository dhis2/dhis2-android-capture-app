package com.dhis2.usescases.main.home;

import org.hisp.dhis.android.core.data.database.EnumColumnAdapter;

class HomeViewModelTypeColumnAdapter extends EnumColumnAdapter<HomeViewModel.Type> {
    @Override
    protected Class getEnumClass() {
        return HomeViewModel.Type.class;
    }
}
