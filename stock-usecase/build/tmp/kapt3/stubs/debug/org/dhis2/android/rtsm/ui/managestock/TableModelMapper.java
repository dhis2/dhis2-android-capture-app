package org.dhis2.android.rtsm.ui.managestock;

import org.dhis2.android.rtsm.R;
import org.dhis2.android.rtsm.data.models.StockEntry;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.composetable.model.RowHeader;
import org.dhis2.composetable.model.TableCell;
import org.dhis2.composetable.model.TableHeader;
import org.dhis2.composetable.model.TableHeaderCell;
import org.dhis2.composetable.model.TableHeaderRow;
import org.dhis2.composetable.model.TableModel;
import org.dhis2.composetable.model.TableRowModel;
import org.hisp.dhis.android.core.arch.helpers.Result;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.valuetype.validation.failures.IntegerZeroOrPositiveFailure;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0002J*\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\n2\u0006\u0010\u000e\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u0006J\u0012\u0010\u0010\u001a\u0004\u0018\u00010\u00062\b\u0010\u0011\u001a\u0004\u0018\u00010\u0006R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lorg/dhis2/android/rtsm/ui/managestock/TableModelMapper;", "", "resources", "Lorg/dhis2/commons/resources/ResourceManager;", "(Lorg/dhis2/commons/resources/ResourceManager;)V", "getIntegerZeroOrPositiveErrorMessage", "", "error", "Lorg/hisp/dhis/android/core/common/valuetype/validation/failures/IntegerZeroOrPositiveFailure;", "map", "", "Lorg/dhis2/composetable/model/TableModel;", "entries", "Lorg/dhis2/android/rtsm/data/models/StockEntry;", "stockLabel", "qtdLabel", "validate", "value", "psm-v2.9-DEV_debug"})
public final class TableModelMapper {
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.commons.resources.ResourceManager resources = null;
    
    @javax.inject.Inject
    public TableModelMapper(@org.jetbrains.annotations.NotNull
    org.dhis2.commons.resources.ResourceManager resources) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<org.dhis2.composetable.model.TableModel> map(@org.jetbrains.annotations.NotNull
    java.util.List<org.dhis2.android.rtsm.data.models.StockEntry> entries, @org.jetbrains.annotations.NotNull
    java.lang.String stockLabel, @org.jetbrains.annotations.NotNull
    java.lang.String qtdLabel) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String validate(@org.jetbrains.annotations.Nullable
    java.lang.String value) {
        return null;
    }
    
    private final java.lang.String getIntegerZeroOrPositiveErrorMessage(org.hisp.dhis.android.core.common.valuetype.validation.failures.IntegerZeroOrPositiveFailure error) {
        return null;
    }
}