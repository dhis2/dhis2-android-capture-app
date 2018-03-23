package com.dhis2.usescases.eventsWithoutRegistration.eventInfoSections;

import android.support.annotation.Nullable;

import com.dhis2.Bindings.Bindings;
import com.dhis2.data.metadata.MetadataRepository;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventInfoSectionsInteractor implements EventInfoSectionsContract.Interactor {

    private final MetadataRepository metadataRepository;
    private final EventInfoSectionsRepository eventInitialRepository;
    private EventInfoSectionsContract.View view;
    private CompositeDisposable compositeDisposable;
    private ProgramModel programModel;
    private CategoryComboModel catCombo;


    EventInfoSectionsInteractor(EventInfoSectionsRepository eventInitialRepository, MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
        this.eventInitialRepository = eventInitialRepository;
        Bindings.setMetadataRepository(metadataRepository);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(EventInfoSectionsContract.View view, @Nullable String eventId) {
        this.view = view;
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }
}
