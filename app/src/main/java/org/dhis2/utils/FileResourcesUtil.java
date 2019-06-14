package org.dhis2.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import org.dhis2.data.service.files.FilesWorker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import timber.log.Timber;

public class FileResourcesUtil {

    public static File getUploadDirectory(Context context) {
        File uploadDirectory = new File(context.getFilesDir(), "upload");
        if (!uploadDirectory.exists())
            uploadDirectory.mkdirs();
        return uploadDirectory;
    }

    public static File getDownloadDirectory(Context context) {
        File downloadDirectory = new File(context.getFilesDir(), "download");
        if (!downloadDirectory.exists())
            downloadDirectory.mkdirs();
        return downloadDirectory;
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


    public static WorkContinuation initBulkFileUploadWork() {

        return WorkManager.getInstance().beginUniqueWork(FilesWorker.TAG_UPLOAD, ExistingWorkPolicy.REPLACE, initBulkFileUploadRequest());
    }

    public static OneTimeWorkRequest initBulkFileUploadRequest() {
        OneTimeWorkRequest.Builder fileBuilder = new OneTimeWorkRequest.Builder(FilesWorker.class);
        fileBuilder.addTag(FilesWorker.TAG_UPLOAD);
        fileBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        fileBuilder.setInputData(new Data.Builder()
                .putString(FilesWorker.MODE, FilesWorker.FileMode.UPLOAD.name())
                .build());
        return fileBuilder.build();
    }

    public static WorkContinuation initDownloadWork() {
        return WorkManager.getInstance().beginUniqueWork(FilesWorker.TAG, ExistingWorkPolicy.REPLACE, initDownloadRequest());
    }

    public static OneTimeWorkRequest initDownloadRequest() {
        OneTimeWorkRequest.Builder fileBuilder = new OneTimeWorkRequest.Builder(FilesWorker.class);
        fileBuilder.addTag(FilesWorker.TAG);
        fileBuilder.setConstraints(new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build());
        fileBuilder.setInputData(new Data.Builder()
                .putString(FilesWorker.MODE, FilesWorker.FileMode.DOWNLOAD.name())
                .build());
        return fileBuilder.build();
    }

    public static void saveImageToUpload(File src, File dst) {
        try {
            try (InputStream in = new FileInputStream(src)) {
                try (OutputStream out = new FileOutputStream(dst)) {
                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Timber.e(e);
        } catch (Exception e) {
            Timber.e(e);
        }

    }

    public static File getFileFromGallery(Context context, Uri imageUri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(imageUri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return new File(s);
    }

    public static File getFileForAttribute(Context context, String fileName) {
        File fromUpload = new File(FileResourcesUtil.getUploadDirectory(context), fileName);
        File fromDownload = new File(FileResourcesUtil.getDownloadDirectory(context), fileName);

        return fromUpload.exists() ? fromUpload : fromDownload;
    }

    public static String generateFileName(String primaryUid, String secundaryUid) {
        return String.format("%s.%s.png", primaryUid, secundaryUid);
    }
}
