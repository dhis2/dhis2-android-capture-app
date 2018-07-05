package com.dhis2.usescases.about;

import com.dhis2.data.metadata.MetadataRepository;

/**
 * QUADRAM. Created by ppajuelo on 05/07/2018.
 */

public class AboutPresenterImpl implements AboutPresenter {
    private final MetadataRepository metadata;

    public AboutPresenterImpl(MetadataRepository metadataRepository) {
        this.metadata = metadataRepository;
    }

    @Override
    public void init(AboutFragment aboutFragment) {

    }

    @Override
    public void dispose() {

    }
}
