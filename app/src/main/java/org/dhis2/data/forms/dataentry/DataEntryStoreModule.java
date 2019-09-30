package org.dhis2.data.forms.dataentry;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.user.UserRepository;
import org.hisp.dhis.android.core.D2;

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
                                       @NonNull UserRepository userRepository, D2 d2) {
        if (!isEmpty(arguments.event())) { // NOPMD
            return new DataValueStore(d2,briteDatabase, userRepository, arguments.event());
        } else if (!isEmpty(arguments.enrollment())) { //NOPMD
            return new AttributeValueStore(d2, arguments.enrollment());
        } else {
            throw new IllegalArgumentException("Unsupported entity type");
        }
    }
}
