package org.dhis2.utils;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.dhis2.data.service.files.FilesWorker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;

public class FileResourcesUtil {

    public static File getUploadDirectory(Context context) {
        return new File(context.getFilesDir(), "upload");
    }

    public static File getDownloadDirectory(Context context) {
        return new File(context.getFilesDir(), "images");
    }

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


    public static void initBulkFileUploadWork() {
        OneTimeWorkRequest.Builder fileBuilder = new OneTimeWorkRequest.Builder(FilesWorker.class);
        fileBuilder.addTag(FilesWorker.TAG_UPLOAD);
        fileBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        fileBuilder.setInputData(new Data.Builder()
                .putString(FilesWorker.MODE, FilesWorker.FileMode.UPLOAD.name())
                .build());
        OneTimeWorkRequest requestFile = fileBuilder.build();
        WorkManager.getInstance().beginUniqueWork(FilesWorker.TAG_UPLOAD, ExistingWorkPolicy.REPLACE, requestFile).enqueue();
    }

    public static void initDownloadWork() {
        OneTimeWorkRequest.Builder fileBuilder = new OneTimeWorkRequest.Builder(FilesWorker.class);
        fileBuilder.addTag(FilesWorker.TAG);
        fileBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        fileBuilder.setInputData(new Data.Builder()
                .putString(FilesWorker.MODE, FilesWorker.FileMode.DOWNLOAD.name())
                .build());
        OneTimeWorkRequest requestFile = fileBuilder.build();
        WorkManager.getInstance().beginUniqueWork(FilesWorker.TAG, ExistingWorkPolicy.REPLACE, requestFile).enqueue();
    }

    public static boolean saveImageToUpload(Context context, File file) {
        try {
            File futureUploadImage = new File(context.getFilesDir() + File.separator + "upload" + File.separator + file.getName());

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];
                long fileSize = file.length();
                long fileSizeDownloaded = 0;

                inputStream = new FileInputStream(file);
                outputStream = new FileOutputStream(futureUploadImage);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Timber.d("file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

}
