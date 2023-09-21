package org.dhis2.android.rtsm.ui.home.screens.components;

import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Modifier;
import androidx.fragment.app.FragmentManager;
import org.dhis2.android.rtsm.R;
import org.dhis2.android.rtsm.data.OperationState;
import org.dhis2.android.rtsm.data.TransactionType;
import org.dhis2.android.rtsm.data.models.TransactionItem;
import org.dhis2.android.rtsm.ui.home.HomeViewModel;
import org.dhis2.android.rtsm.ui.home.model.DataEntryUiState;
import org.dhis2.android.rtsm.ui.home.model.EditionDialogResult;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000j\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\u001a\u00e2\u0001\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2B\u0010\n\u001a>\u0012\u0013\u0012\u00110\f\u00a2\u0006\f\b\r\u0012\b\b\u000e\u0012\u0004\b\b(\u000f\u0012\u001f\u0012\u001d\u0012\u0013\u0012\u00110\u0011\u00a2\u0006\f\b\r\u0012\b\b\u000e\u0012\u0004\b\b(\u0012\u0012\u0004\u0012\u00020\u00010\u0010\u0012\u0004\u0012\u00020\u00010\u000b2!\u0010\u0013\u001a\u001d\u0012\u0013\u0012\u00110\u0014\u00a2\u0006\f\b\r\u0012\b\b\u000e\u0012\u0004\b\b(\u0015\u0012\u0004\u0012\u00020\u00010\u00102!\u0010\u0016\u001a\u001d\u0012\u0013\u0012\u00110\u0017\u00a2\u0006\f\b\r\u0012\b\b\u000e\u0012\u0004\b\b(\u0018\u0012\u0004\u0012\u00020\u00010\u00102!\u0010\u0019\u001a\u001d\u0012\u0013\u0012\u00110\u001a\u00a2\u0006\f\b\r\u0012\b\b\u000e\u0012\u0004\b\b(\u001b\u0012\u0004\u0012\u00020\u00010\u0010H\u0007\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u001c\u0010\u001d\u001a$\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00170\u001f2\u0014\u0010 \u001a\u0010\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u001f\u0018\u00010!H\u0002\u001a\u000e\u0010\"\u001a\b\u0012\u0004\u0012\u00020$0#H\u0002\u0082\u0002\u000b\n\u0005\b\u00a1\u001e0\u0001\n\u0002\b\u0019\u00a8\u0006%"}, d2 = {"FilterList", "", "viewModel", "Lorg/dhis2/android/rtsm/ui/home/HomeViewModel;", "dataEntryUiState", "Lorg/dhis2/android/rtsm/ui/home/model/DataEntryUiState;", "themeColor", "Landroidx/compose/ui/graphics/Color;", "supportFragmentManager", "Landroidx/fragment/app/FragmentManager;", "launchDialog", "Lkotlin/Function2;", "", "Lkotlin/ParameterName;", "name", "msg", "Lkotlin/Function1;", "Lorg/dhis2/android/rtsm/ui/home/model/EditionDialogResult;", "result", "onTransitionSelected", "Lorg/dhis2/android/rtsm/data/TransactionType;", "transition", "onFacilitySelected", "Lorg/hisp/dhis/android/core/organisationunit/OrganisationUnit;", "facility", "onDestinationSelected", "Lorg/hisp/dhis/android/core/option/Option;", "destination", "FilterList-T042LqI", "(Lorg/dhis2/android/rtsm/ui/home/HomeViewModel;Lorg/dhis2/android/rtsm/ui/home/model/DataEntryUiState;JLandroidx/fragment/app/FragmentManager;Lkotlin/jvm/functions/Function2;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)V", "getFacilities", "", "ou", "Lorg/dhis2/android/rtsm/data/OperationState;", "mapTransaction", "", "Lorg/dhis2/android/rtsm/data/models/TransactionItem;", "psm-v2.9-DEV_debug"})
public final class FilterComponentsKt {
    
    private static final java.util.List<org.dhis2.android.rtsm.data.models.TransactionItem> mapTransaction() {
        return null;
    }
    
    private static final java.util.List<org.hisp.dhis.android.core.organisationunit.OrganisationUnit> getFacilities(org.dhis2.android.rtsm.data.OperationState<? extends java.util.List<? extends org.hisp.dhis.android.core.organisationunit.OrganisationUnit>> ou) {
        return null;
    }
}