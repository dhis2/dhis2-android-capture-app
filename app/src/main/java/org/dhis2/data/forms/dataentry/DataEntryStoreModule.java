package org.dhis2.data.forms.dataentry;

import android.support.annotation.NonNull;

import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.user.UserRepository;
import com.squareup.sqlbrite2.BriteDatabase;



import dagger.Module;
import dagger.Provides;

import static android.text.TextUtils.isEmpty;


@Module
@PerFragment
public final class DataEntryStoreModule {

    @NonNull
    private final DataEntryArguments arguments;

    public DataEntryStoreModule(@NonNull DataEntryArguments arguments) {
        this.arguments = arguments;
    }

    @Provides
    @PerFragment
    DataEntryStore dataEntryRepository(@NonNull BriteDatabase briteDatabase,
                                       @NonNull UserRepository userRepository/*, @NonNull CurrentDateProvider dateProvider*/) {
        if (!isEmpty(arguments.event())) { // NOPMD
            return new DataValueStore(briteDatabase,
                    userRepository, /*dateProvider,*/ arguments.event());
        } else if (!isEmpty(arguments.enrollment())) { //NOPMD
            return new AttributeValueStore(briteDatabase,
                    /*dateProvider,*/ arguments.enrollment());
        } else {
            throw new IllegalArgumentException("Unsupported entity type");
        }
    }
}
