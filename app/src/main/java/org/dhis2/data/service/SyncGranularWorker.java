/*
 * Copyright (c) 2004 - 2019, University of Oslo
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.dhis2.data.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.dhis2.App;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import javax.inject.Inject;

import static org.dhis2.utils.Constants.ATTRIBUTE_OPTION_COMBO;
import static org.dhis2.utils.Constants.CATEGORY_OPTION_COMBO;
import static org.dhis2.utils.Constants.CONFLICT_TYPE;
import static org.dhis2.utils.Constants.ORG_UNIT;
import static org.dhis2.utils.Constants.PERIOD_ID;
import static org.dhis2.utils.Constants.UID;
import static org.dhis2.utils.granularsync.SyncStatusDialog.ConflictType;

public class SyncGranularWorker extends Worker {

    @Inject
    SyncPresenter presenter;

    public SyncGranularWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NotNull
    @Override
    public Result doWork() {
        Objects.requireNonNull(((App) getApplicationContext()).userComponent()).plus(new SyncGranularRxModule()).inject(this);
        String uid = getInputData().getString(UID);
        ConflictType conflictType = ConflictType.valueOf(getInputData().getString(CONFLICT_TYPE));
        switch (conflictType) {
            case PROGRAM:
                return presenter.blockSyncGranularProgram(uid);
            case TEI:
                return presenter.blockSyncGranularTei(uid);
            case EVENT:
                return presenter.blockSyncGranularEvent(uid);
            case DATA_SET:
                return presenter.blockSyncGranularDataSet(uid);
            case DATA_VALUES:
                return presenter.blockSyncGranularDataValues(uid, getInputData().getString(ORG_UNIT),
                        getInputData().getString(ATTRIBUTE_OPTION_COMBO), getInputData().getString(PERIOD_ID), getInputData().getStringArray(CATEGORY_OPTION_COMBO));
            default:
                return Result.failure();
        }

    }
}
