package org.dhis2.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import org.dhis2.data.service.files.FilesWorker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

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

    public static Bitmap getSmallImage(Context context, String filePath) {
        File file = new File(filePath);

        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());

        int desired = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, context.getResources().getDisplayMetrics());

        Bitmap dstBitmap;
        if (bitmap.getWidth() >= bitmap.getHeight()) {
            dstBitmap = Bitmap.createBitmap(bitmap, bitmap.getWidth() / 2 - bitmap.getHeight() / 2, 0, bitmap.getHeight(), bitmap.getHeight());
        } else {
            dstBitmap = Bitmap.createBitmap(bitmap, 0, bitmap.getHeight() / 2 - bitmap.getWidth() / 2, bitmap.getWidth(), bitmap.getWidth());
        }
        return Bitmap.createScaledBitmap(dstBitmap, desired, desired, false);

    }

    public static String generateFileName(String primaryUid, String secundaryUid) {
        return String.format("%s_%s.png", primaryUid, secundaryUid);
    }

    public static boolean writeToFile(@NonNull String content, @Nullable String secretToEncode) {
        // Get the directory for the user's public pictures directory.
        final File path = new File(Environment.getExternalStorageDirectory(), "DHIS2");

        // Make sure the path directory exists.
        if (!path.exists()) {
            // Make it, if it doesn't exit
            path.mkdirs();
        }

        final File file = new File(path, "dhisDataBuckUp.txt");
        if (file.exists())
            file.delete();

        // Save your stream, don't forget to flush() it before closing it.

        try {
            boolean fileCreated = file.createNewFile();
            try (FileOutputStream fOut = new FileOutputStream(file)) {
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(content);

                myOutWriter.close();

                fOut.flush();
            }
            return fileCreated;
        } catch (IOException e) {
            Timber.e("File write failed: %s", e.toString());
            return false;
        }
    }

    public static String readFromFile() throws IOException {
        File directory = new File(Environment.getExternalStorageDirectory(), "DHIS2");
        File file = new File(directory, "dhisDataBuckUp.txt");
        FileInputStream fin = new FileInputStream(file);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    private static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }
}
