package org.dhis2.android.rtsm.services.rules;

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
public final class RuleValidationHelperImpl_Factory implements Factory<RuleValidationHelperImpl> {
  private final Provider<D2> d2Provider;

  public RuleValidationHelperImpl_Factory(Provider<D2> d2Provider) {
    this.d2Provider = d2Provider;
  }

  @Override
  public RuleValidationHelperImpl get() {
    return newInstance(d2Provider.get());
  }

  public static RuleValidationHelperImpl_Factory create(Provider<D2> d2Provider) {
    return new RuleValidationHelperImpl_Factory(d2Provider);
  }

  public static RuleValidationHelperImpl newInstance(D2 d2) {
    return new RuleValidationHelperImpl(d2);
  }
}
