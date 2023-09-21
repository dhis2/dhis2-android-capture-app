package org.dhis2.android.rtsm.services;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import org.hisp.dhis.android.core.D2;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class StockTableDimensionStore_Factory implements Factory<StockTableDimensionStore> {
  private final Provider<D2> d2Provider;

  public StockTableDimensionStore_Factory(Provider<D2> d2Provider) {
    this.d2Provider = d2Provider;
  }

  @Override
  public StockTableDimensionStore get() {
    return newInstance(d2Provider.get());
  }

  public static StockTableDimensionStore_Factory create(Provider<D2> d2Provider) {
    return new StockTableDimensionStore_Factory(d2Provider);
  }

  public static StockTableDimensionStore newInstance(D2 d2) {
    return new StockTableDimensionStore(d2);
  }
}
