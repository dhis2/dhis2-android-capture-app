package org.dhis2.android.rtsm.ui.managestock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.jakewharton.rxrelay2.PublishRelay;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.disposables.CompositeDisposable;
import kotlinx.coroutines.FlowPreview;
import kotlinx.coroutines.flow.StateFlow;
import org.dhis2.android.rtsm.R;
import org.dhis2.android.rtsm.data.AppConfig;
import org.dhis2.android.rtsm.data.RowAction;
import org.dhis2.android.rtsm.data.TransactionType;
import org.dhis2.android.rtsm.data.models.SearchParametersModel;
import org.dhis2.android.rtsm.data.models.StockEntry;
import org.dhis2.android.rtsm.data.models.StockItem;
import org.dhis2.android.rtsm.data.models.Transaction;
import org.dhis2.android.rtsm.services.SpeechRecognitionManager;
import org.dhis2.android.rtsm.services.StockManager;
import org.dhis2.android.rtsm.services.StockTableDimensionStore;
import org.dhis2.android.rtsm.services.rules.RuleValidationHelper;
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider;
import org.dhis2.android.rtsm.ui.base.OnQuantityValidated;
import org.dhis2.android.rtsm.ui.base.SpeechRecognitionAwareViewModel;
import org.dhis2.android.rtsm.ui.home.model.ButtonUiState;
import org.dhis2.android.rtsm.ui.home.model.DataEntryStep;
import org.dhis2.android.rtsm.ui.home.model.DataEntryUiState;
import org.dhis2.android.rtsm.ui.home.model.SnackBarUiState;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.commons.viewmodel.DispatcherProvider;
import org.dhis2.composetable.TableConfigurationState;
import org.dhis2.composetable.TableScreenState;
import org.dhis2.composetable.actions.Validator;
import org.dhis2.composetable.model.KeyboardInputType;
import org.dhis2.composetable.model.TableCell;
import org.dhis2.composetable.model.TextInputModel;
import org.dhis2.composetable.model.ValidationResult;
import org.hisp.dhis.rules.models.RuleActionAssign;
import org.hisp.dhis.rules.models.RuleEffect;
import org.jetbrains.annotations.NotNull;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0094\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0013\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010!\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u00012\u00020\u0002BO\b\u0007\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\n\u0012\u0006\u0010\u000b\u001a\u00020\f\u0012\u0006\u0010\r\u001a\u00020\u000e\u0012\u0006\u0010\u000f\u001a\u00020\u0010\u0012\u0006\u0010\u0011\u001a\u00020\u0012\u0012\u0006\u0010\u0013\u001a\u00020\u0014\u00a2\u0006\u0002\u0010\u0015J,\u0010W\u001a\u0002082\u0006\u0010X\u001a\u00020%2\b\u0010Y\u001a\u0004\u0018\u00010 2\b\u0010Z\u001a\u0004\u0018\u00010 2\b\u0010[\u001a\u0004\u0018\u00010 J\"\u0010\\\u001a\u0002082\u0006\u0010]\u001a\u00020^2\u0006\u0010_\u001a\u00020%2\b\u0010`\u001a\u0004\u0018\u00010 H\u0002J\u0006\u0010a\u001a\u000208J\b\u0010b\u001a\u00020\u0018H\u0002J\u0006\u0010c\u001a\u000208J\b\u0010d\u001a\u000208H\u0002J\b\u0010e\u001a\u000208H\u0002J\b\u0010f\u001a\u000208H\u0002J\u0010\u0010g\u001a\u00020\u00182\u0006\u0010U\u001a\u00020+H\u0002J\u0010\u0010h\u001a\u0004\u0018\u00010 2\u0006\u0010X\u001a\u00020%J,\u0010i\u001a&\u0012\f\u0012\n <*\u0004\u0018\u00010A0A <*\u0012\u0012\f\u0012\n <*\u0004\u0018\u00010A0A\u0018\u00010$0jH\u0002J\u0012\u0010k\u001a\u0004\u0018\u00010A2\u0006\u0010l\u001a\u00020mH\u0002J\u0014\u0010n\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020%0o01H\u0002J\u0010\u0010p\u001a\u0002082\u0006\u0010`\u001a\u00020\u0018H\u0002J\b\u0010q\u001a\u000208H\u0002J\u0006\u0010r\u001a\u000208J\u0006\u0010s\u001a\u000208J\u000e\u0010t\u001a\u00020u2\u0006\u0010l\u001a\u00020mJ\u001c\u0010v\u001a\u0002082\u0006\u0010w\u001a\u00020\u00182\f\u0010x\u001a\b\u0012\u0004\u0012\u00020807J\u0006\u0010y\u001a\u000208J\u000e\u0010z\u001a\u0002082\u0006\u0010l\u001a\u00020mJ\u000e\u0010{\u001a\u0002082\u0006\u0010|\u001a\u00020 J\b\u0010}\u001a\u000208H\u0002J\b\u0010~\u001a\u00020 H\u0002J\u0006\u0010\u007f\u001a\u000208J\u0007\u0010\u0080\u0001\u001a\u000208J\t\u0010\u0081\u0001\u001a\u00020\'H\u0002J\u001d\u0010\u0082\u0001\u001a\u0004\u0018\u0001082\u0006\u0010l\u001a\u00020mH\u0082@\u00f8\u0001\u0000\u00a2\u0006\u0003\u0010\u0083\u0001J\u000f\u0010\u0084\u0001\u001a\u0002082\u0006\u00100\u001a\u00020\u001bJ?\u0010\u0085\u0001\u001a\u0002082\f\u0010X\u001a\b0%\u00a2\u0006\u0003\b\u0086\u00012\u000e\u0010\u0087\u0001\u001a\t0\u0088\u0001\u00a2\u0006\u0003\b\u0086\u00012\f\u0010Y\u001a\b0 \u00a2\u0006\u0003\b\u0086\u00012\n\u0010\u0089\u0001\u001a\u0005\u0018\u00010\u008a\u0001J\u001e\u0010\u008b\u0001\u001a\u0002082\u0006\u0010S\u001a\u00020)\u00f8\u0001\u0001\u00f8\u0001\u0000\u00a2\u0006\u0006\b\u008c\u0001\u0010\u008d\u0001J\u000f\u0010\u008e\u0001\u001a\u0002082\u0006\u0010U\u001a\u00020+J\u0013\u0010\u008f\u0001\u001a\u0004\u0018\u00010 2\u0006\u0010l\u001a\u00020mH\u0002J\t\u0010\u0090\u0001\u001a\u000208H\u0002J\u0011\u0010\u0091\u0001\u001a\u0002082\b\u0010\u0092\u0001\u001a\u00030\u0093\u0001J\u0013\u0010\u0094\u0001\u001a\u00030\u0095\u00012\u0007\u0010\u0096\u0001\u001a\u00020mH\u0016R\u0014\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00180\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u001b0\u001aX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u001d0\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u00180\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020 0\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010!\u001a\b\u0012\u0004\u0012\u00020\"0\u001aX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010#\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020%0$0\u001aX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010&\u001a\b\u0012\u0004\u0012\u00020\'0\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010(\u001a\b\u0012\u0004\u0012\u00020)0\u0017X\u0082\u0004\u00f8\u0001\u0000\u00a2\u0006\u0002\n\u0000R\u0016\u0010*\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010+0\u001aX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010,\u001a\b\u0012\u0004\u0012\u00020\u00180-\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010/R\u0017\u00100\u001a\b\u0012\u0004\u0012\u00020\u001b01\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u00103R\u0017\u00104\u001a\b\u0012\u0004\u0012\u00020\u001d0-\u00a2\u0006\b\n\u0000\u001a\u0004\b5\u0010/R\u001a\u00106\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u000208070\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0012X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001c\u00109\u001a\u0010\u0012\f\u0012\n <*\u0004\u0018\u00010;0;0:X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010=\u001a\b\u0012\u0004\u0012\u00020\u00180-\u00a2\u0006\b\n\u0000\u001a\u0004\b>\u0010/R\u001a\u0010?\u001a\u000e\u0012\u0004\u0012\u00020 \u0012\u0004\u0012\u00020A0@X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010B\u001a\b\u0012\u0004\u0012\u00020 0-\u00a2\u0006\b\n\u0000\u001a\u0004\bC\u0010/R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010D\u001a\b\u0012\u0004\u0012\u00020\"01\u00a2\u0006\b\n\u0000\u001a\u0004\bE\u00103R\u0014\u0010F\u001a\b\u0012\u0004\u0012\u00020G0\u001aX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001c\u0010H\u001a\u0010\u0012\f\u0012\n <*\u0004\u0018\u00010 0 0:X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010I\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010J0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\bK\u0010LR\u0019\u0010M\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010J0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\bN\u0010LR\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010O\u001a\b\u0012\u0004\u0012\u00020\'0-\u00a2\u0006\b\n\u0000\u001a\u0004\bP\u0010/R\u0011\u0010\u0013\u001a\u00020\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\bQ\u0010RR\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010S\u001a\b\u0012\u0004\u0012\u00020)0-\u00f8\u0001\u0000\u00a2\u0006\b\n\u0000\u001a\u0004\bT\u0010/R\u0019\u0010U\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010+01\u00a2\u0006\b\n\u0000\u001a\u0004\bV\u00103\u0082\u0002\u000b\n\u0002\b\u0019\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u0097\u0001"}, d2 = {"Lorg/dhis2/android/rtsm/ui/managestock/ManageStockViewModel;", "Lorg/dhis2/composetable/actions/Validator;", "Lorg/dhis2/android/rtsm/ui/base/SpeechRecognitionAwareViewModel;", "disposable", "Lio/reactivex/disposables/CompositeDisposable;", "schedulerProvider", "Lorg/dhis2/android/rtsm/services/scheduler/BaseSchedulerProvider;", "stockManagerRepository", "Lorg/dhis2/android/rtsm/services/StockManager;", "ruleValidationHelper", "Lorg/dhis2/android/rtsm/services/rules/RuleValidationHelper;", "speechRecognitionManager", "Lorg/dhis2/android/rtsm/services/SpeechRecognitionManager;", "resources", "Lorg/dhis2/commons/resources/ResourceManager;", "tableModelMapper", "Lorg/dhis2/android/rtsm/ui/managestock/TableModelMapper;", "dispatcherProvider", "Lorg/dhis2/commons/viewmodel/DispatcherProvider;", "tableDimensionStore", "Lorg/dhis2/android/rtsm/services/StockTableDimensionStore;", "(Lio/reactivex/disposables/CompositeDisposable;Lorg/dhis2/android/rtsm/services/scheduler/BaseSchedulerProvider;Lorg/dhis2/android/rtsm/services/StockManager;Lorg/dhis2/android/rtsm/services/rules/RuleValidationHelper;Lorg/dhis2/android/rtsm/services/SpeechRecognitionManager;Lorg/dhis2/commons/resources/ResourceManager;Lorg/dhis2/android/rtsm/ui/managestock/TableModelMapper;Lorg/dhis2/commons/viewmodel/DispatcherProvider;Lorg/dhis2/android/rtsm/services/StockTableDimensionStore;)V", "_bottomSheetState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_config", "Landroidx/lifecycle/MutableLiveData;", "Lorg/dhis2/android/rtsm/data/AppConfig;", "_dataEntryUiState", "Lorg/dhis2/android/rtsm/ui/home/model/DataEntryUiState;", "_hasData", "_scanText", "", "_screenState", "Lorg/dhis2/composetable/TableScreenState;", "_stockItems", "", "Lorg/dhis2/android/rtsm/data/models/StockItem;", "_tableConfigurationState", "Lorg/dhis2/composetable/TableConfigurationState;", "_themeColor", "Landroidx/compose/ui/graphics/Color;", "_transaction", "Lorg/dhis2/android/rtsm/data/models/Transaction;", "bottomSheetState", "Lkotlinx/coroutines/flow/StateFlow;", "getBottomSheetState", "()Lkotlinx/coroutines/flow/StateFlow;", "config", "Landroidx/lifecycle/LiveData;", "getConfig", "()Landroidx/lifecycle/LiveData;", "dataEntryUiState", "getDataEntryUiState", "debounceState", "Lkotlin/Function0;", "", "entryRelay", "Lcom/jakewharton/rxrelay2/PublishRelay;", "Lorg/dhis2/android/rtsm/data/RowAction;", "kotlin.jvm.PlatformType", "hasData", "getHasData", "itemsCache", "Ljava/util/LinkedHashMap;", "Lorg/dhis2/android/rtsm/data/models/StockEntry;", "scanText", "getScanText", "screenState", "getScreenState", "search", "Lorg/dhis2/android/rtsm/data/models/SearchParametersModel;", "searchRelay", "shouldCloseActivity", "Ljava/lang/Void;", "getShouldCloseActivity", "()Landroidx/lifecycle/MutableLiveData;", "shouldNavigateBack", "getShouldNavigateBack", "tableConfigurationState", "getTableConfigurationState", "getTableDimensionStore", "()Lorg/dhis2/android/rtsm/services/StockTableDimensionStore;", "themeColor", "getThemeColor", "transaction", "getTransaction", "addItem", "item", "qty", "stockOnHand", "errorMessage", "applyRuleEffectOnItem", "ruleEffect", "Lorg/hisp/dhis/rules/models/RuleEffect;", "stockItem", "value", "backToListing", "canReview", "cleanItemsFromCache", "clearTransaction", "commitTransaction", "configureRelays", "didTransactionParamsChange", "getItemQuantity", "getPopulatedEntries", "", "getStockEntry", "cell", "Lorg/dhis2/composetable/model/TableCell;", "getStockItems", "Landroidx/paging/PagedList;", "hasUnsavedData", "loadStockItems", "onBottomSheetClosed", "onButtonClick", "onCellClick", "Lorg/dhis2/composetable/model/TextInputModel;", "onEditingCell", "isEditing", "onEditionStart", "onHandleBackNavigation", "onSaveValueChange", "onSearchQueryChanged", "query", "populateTable", "provideQuantityLabel", "refreshConfig", "refreshData", "refreshTableConfiguration", "saveValue", "(Lorg/dhis2/composetable/model/TableCell;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "setConfig", "setQuantity", "Lorg/jetbrains/annotations/NotNull;", "position", "", "callback", "Lorg/dhis2/android/rtsm/ui/base/OnQuantityValidated;", "setThemeColor", "setThemeColor-8_81llA", "(J)V", "setup", "tableCellId", "updateReviewButton", "updateStep", "step", "Lorg/dhis2/android/rtsm/ui/home/model/DataEntryStep;", "validate", "Lorg/dhis2/composetable/model/ValidationResult;", "tableCell", "psm-v2.9-DEV_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class ManageStockViewModel extends org.dhis2.android.rtsm.ui.base.SpeechRecognitionAwareViewModel implements org.dhis2.composetable.actions.Validator {
    @org.jetbrains.annotations.NotNull
    private final io.reactivex.disposables.CompositeDisposable disposable = null;
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider schedulerProvider = null;
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.android.rtsm.services.StockManager stockManagerRepository = null;
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.android.rtsm.services.rules.RuleValidationHelper ruleValidationHelper = null;
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.commons.resources.ResourceManager resources = null;
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.android.rtsm.ui.managestock.TableModelMapper tableModelMapper = null;
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.commons.viewmodel.DispatcherProvider dispatcherProvider = null;
    @org.jetbrains.annotations.NotNull
    private final org.dhis2.android.rtsm.services.StockTableDimensionStore tableDimensionStore = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<org.dhis2.android.rtsm.data.AppConfig> _config = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.LiveData<org.dhis2.android.rtsm.data.AppConfig> config = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<org.dhis2.android.rtsm.data.models.Transaction> _transaction = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.LiveData<org.dhis2.android.rtsm.data.models.Transaction> transaction = null;
    @org.jetbrains.annotations.NotNull
    private androidx.lifecycle.MutableLiveData<org.dhis2.android.rtsm.data.models.SearchParametersModel> search;
    @org.jetbrains.annotations.NotNull
    private final com.jakewharton.rxrelay2.PublishRelay<java.lang.String> searchRelay = null;
    @org.jetbrains.annotations.NotNull
    private final com.jakewharton.rxrelay2.PublishRelay<org.dhis2.android.rtsm.data.RowAction> entryRelay = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.LinkedHashMap<java.lang.String, org.dhis2.android.rtsm.data.models.StockEntry> itemsCache = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _hasData = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> hasData = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<org.dhis2.composetable.TableScreenState> _screenState = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.LiveData<org.dhis2.composetable.TableScreenState> screenState = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<java.util.List<org.dhis2.android.rtsm.data.models.StockItem>> _stockItems = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<org.dhis2.android.rtsm.ui.home.model.DataEntryUiState> _dataEntryUiState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<org.dhis2.android.rtsm.ui.home.model.DataEntryUiState> dataEntryUiState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<androidx.compose.ui.graphics.Color> _themeColor = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<androidx.compose.ui.graphics.Color> themeColor = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _scanText = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> scanText = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<kotlin.jvm.functions.Function0<kotlin.Unit>> debounceState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _bottomSheetState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> bottomSheetState = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<java.lang.Void> shouldCloseActivity = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.MutableLiveData<java.lang.Void> shouldNavigateBack = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<org.dhis2.composetable.TableConfigurationState> _tableConfigurationState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<org.dhis2.composetable.TableConfigurationState> tableConfigurationState = null;
    
    @javax.inject.Inject
    public ManageStockViewModel(@org.jetbrains.annotations.NotNull
    io.reactivex.disposables.CompositeDisposable disposable, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider schedulerProvider, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.services.StockManager stockManagerRepository, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.services.rules.RuleValidationHelper ruleValidationHelper, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.services.SpeechRecognitionManager speechRecognitionManager, @org.jetbrains.annotations.NotNull
    org.dhis2.commons.resources.ResourceManager resources, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.ui.managestock.TableModelMapper tableModelMapper, @org.jetbrains.annotations.NotNull
    org.dhis2.commons.viewmodel.DispatcherProvider dispatcherProvider, @org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.services.StockTableDimensionStore tableDimensionStore) {
        super(null, null);
    }
    
    @org.jetbrains.annotations.NotNull
    public final org.dhis2.android.rtsm.services.StockTableDimensionStore getTableDimensionStore() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<org.dhis2.android.rtsm.data.AppConfig> getConfig() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<org.dhis2.android.rtsm.data.models.Transaction> getTransaction() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getHasData() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.LiveData<org.dhis2.composetable.TableScreenState> getScreenState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<org.dhis2.android.rtsm.ui.home.model.DataEntryUiState> getDataEntryUiState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<androidx.compose.ui.graphics.Color> getThemeColor() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getScanText() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getBottomSheetState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.MutableLiveData<java.lang.Void> getShouldCloseActivity() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final androidx.lifecycle.MutableLiveData<java.lang.Void> getShouldNavigateBack() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<org.dhis2.composetable.TableConfigurationState> getTableConfigurationState() {
        return null;
    }
    
    public final void setup(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.models.Transaction transaction) {
    }
    
    private final boolean didTransactionParamsChange(org.dhis2.android.rtsm.data.models.Transaction transaction) {
        return false;
    }
    
    public final void refreshData() {
    }
    
    public final void setConfig(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.AppConfig config) {
    }
    
    public final void refreshConfig() {
    }
    
    private final void loadStockItems() {
    }
    
    private final androidx.lifecycle.LiveData<androidx.paging.PagedList<org.dhis2.android.rtsm.data.models.StockItem>> getStockItems() {
        return null;
    }
    
    private final void configureRelays() {
    }
    
    private final void populateTable() {
    }
    
    private final java.lang.String provideQuantityLabel() {
        return null;
    }
    
    private final void commitTransaction() {
    }
    
    private final org.dhis2.android.rtsm.data.models.StockEntry getStockEntry(org.dhis2.composetable.model.TableCell cell) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final org.dhis2.composetable.model.TextInputModel onCellClick(@org.jetbrains.annotations.NotNull
    org.dhis2.composetable.model.TableCell cell) {
        return null;
    }
    
    public final void onSaveValueChange(@org.jetbrains.annotations.NotNull
    org.dhis2.composetable.model.TableCell cell) {
    }
    
    private final java.lang.String tableCellId(org.dhis2.composetable.model.TableCell cell) {
        return null;
    }
    
    private final java.lang.Object saveValue(org.dhis2.composetable.model.TableCell cell, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final void applyRuleEffectOnItem(org.hisp.dhis.rules.models.RuleEffect ruleEffect, org.dhis2.android.rtsm.data.models.StockItem stockItem, java.lang.String value) {
    }
    
    public final void onSearchQueryChanged(@org.jetbrains.annotations.NotNull
    java.lang.String query) {
    }
    
    public final void setQuantity(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.models.StockItem item, int position, @org.jetbrains.annotations.NotNull
    java.lang.String qty, @org.jetbrains.annotations.Nullable
    org.dhis2.android.rtsm.ui.base.OnQuantityValidated callback) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getItemQuantity(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.models.StockItem item) {
        return null;
    }
    
    public final void addItem(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.data.models.StockItem item, @org.jetbrains.annotations.Nullable
    java.lang.String qty, @org.jetbrains.annotations.Nullable
    java.lang.String stockOnHand, @org.jetbrains.annotations.Nullable
    java.lang.String errorMessage) {
    }
    
    public final void cleanItemsFromCache() {
    }
    
    private final void hasUnsavedData(boolean value) {
    }
    
    private final boolean canReview() {
        return false;
    }
    
    private final java.util.List<org.dhis2.android.rtsm.data.models.StockEntry> getPopulatedEntries() {
        return null;
    }
    
    public final void onEditingCell(boolean isEditing, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onEditionStart) {
    }
    
    public final void updateStep(@org.jetbrains.annotations.NotNull
    org.dhis2.android.rtsm.ui.home.model.DataEntryStep step) {
    }
    
    private final void updateReviewButton() {
    }
    
    public final void onButtonClick() {
    }
    
    @kotlin.OptIn(markerClass = {kotlinx.coroutines.FlowPreview.class})
    public final void onHandleBackNavigation() {
    }
    
    private final void clearTransaction() {
    }
    
    public final void backToListing() {
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public org.dhis2.composetable.model.ValidationResult validate(@org.jetbrains.annotations.NotNull
    org.dhis2.composetable.model.TableCell tableCell) {
        return null;
    }
    
    public final void onBottomSheetClosed() {
    }
    
    private final org.dhis2.composetable.TableConfigurationState refreshTableConfiguration() {
        return null;
    }
}