package org.dhis2.utils;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.dhis2.data.service.files.FilesWorker;

public class FileResourcesUtil {

    public static void initFileUploadWork(String teiUid, String attrUid) {
        OneTimeWorkRequest.Builder fileBuilder = new OneTimeWorkRequest.Builder(FilesWorker.class);
        fileBuilder.addTag(teiUid.concat(".").concat(attrUid));
        fileBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        fileBuilder.setInputData(new Data.Builder()
                .putString(FilesWorker.MODE, FilesWorker.FileMode.UPLOAD.name())
                .putString(FilesWorker.TEIUID, teiUid)
                .putString(FilesWorker.ATTRUID, attrUid)
                .build());
        OneTimeWorkRequest requestFile = fileBuilder.build();
        WorkManager.getInstance().beginUniqueWork(teiUid.concat(".").concat(attrUid), ExistingWorkPolicy.REPLACE, requestFile).enqueue();
    }

}
