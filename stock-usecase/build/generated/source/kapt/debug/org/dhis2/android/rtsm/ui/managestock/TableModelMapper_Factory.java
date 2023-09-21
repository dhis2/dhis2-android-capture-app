package org.dhis2.android.rtsm.ui.managestock;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import org.dhis2.commons.resources.ResourceManager;

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
public final class TableModelMapper_Factory implements Factory<TableModelMapper> {
  private final Provider<ResourceManager> resourcesProvider;

  public TableModelMapper_Factory(Provider<ResourceManager> resourcesProvider) {
    this.resourcesProvider = resourcesProvider;
  }

  @Override
  public TableModelMapper get() {
    return newInstance(resourcesProvider.get());
  }

  public static TableModelMapper_Factory create(Provider<ResourceManager> resourcesProvider) {
    return new TableModelMapper_Factory(resourcesProvider);
  }

  public static TableModelMapper newInstance(ResourceManager resources) {
    return new TableModelMapper(resources);
  }
}
