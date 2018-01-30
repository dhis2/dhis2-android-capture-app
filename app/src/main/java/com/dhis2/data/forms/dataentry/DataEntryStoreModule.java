package com.dhis2.data.forms.dataentry;

import android.support.annotation.NonNull;

import com.dhis2.data.dagger.PerFragment;
import com.dhis2.data.user.UserRepository;
import com.squareup.sqlbrite.BriteDatabase;

import dagger.Module;
import dagger.Provides;


@Module
@PerFragment
public final class DataEntryStoreModule {

    @NonNull
    private final DataEntryArguments arguments;

    @Provides
    @PerFragment
    DataEntryStore dataEntryRepository(@NonNull BriteDatabase briteDatabase,
                                       @NonNull UserRepository userRepository) {
      /*  if (!isEmpty(arguments.event())) { // NOPMD
            return new DataValueStore(briteDatabase,
                    userRepository, dateProvider, arguments.event());
        } else if (!isEmpty(arguments.enrollment())) { //NOPMD
            return new AttributeValueStore(briteDatabase,
                    dateProvider, arguments.enrollment());
        } else {
            throw new IllegalArgumentException("Unsupported entity type");
        }*/
      return null;
    }

    public DataEntryStoreModule(@NonNull DataEntryArguments arguments) {
        this.arguments = arguments;
    }
}
