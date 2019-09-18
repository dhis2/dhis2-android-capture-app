package org.dhis2.data.service.files;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.Gson;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.utils.FileResourcesUtil;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Completable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import timber.log.Timber;

public class FilesWorker extends Worker {

    public static final String TAG = "FILE_DOWNLOADER";
    public static final String TAG_UPLOAD = "FILE_UPLOADER";
    public static final String MODE = "MODE";
    public static final String TEIUID = "teiuid";
    public static final String ATTRUID = "attruid";
    private D2 d2;
    private FileService fileService;
    private final static String file_upload_channel = "upload_file_notification";
    private final static String file_download_channel = "upload_file_notification";
    private final static int FILE_ID = 26061987;

    public enum FileMode {
        UPLOAD, DOWNLOAD
    }

    public FilesWorker(@NonNull Context context,
                       @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        d2 = Objects.requireNonNull(((App) getApplicationContext()).serverComponent().userManager().getD2());
        fileService = d2.retrofit().create(FileService.class);

        if (getInputData().getString(MODE) == null || FileMode.valueOf(getInputData().getString(MODE)) == FileMode.DOWNLOAD)
            downloadFileResources();
        else if (getInputData().getString(TEIUID) != null)
            uploadFileResources(getInputData().getString(TEIUID), getInputData().getString(ATTRUID));
        else
            uploadBulkResources();

        cancelNotification();
        return Result.success();
    }

    private void uploadBulkResources() {
        triggerNotification("File processor", "Uploading files...", file_upload_channel);
        File[] filesToUpload = FileResourcesUtil.getUploadDirectory(getApplicationContext()).listFiles();
        int count = 1;
        for (File file : filesToUpload) {
            triggerNotification("File processor", String.format("Uploading file %s/%s", count++, filesToUpload.length), file_upload_channel);
            String[] fileName = file.getName().split("_"); //tei/event, attr/de, extension
            upload(file, fileName[0], fileName[1].split("\\.")[0]);
        }
    }

    private void uploadFileResources(String teiUid, String attrUid) {
        triggerNotification("File processor", "Uploading file...", file_upload_channel);

        String fileName = d2.trackedEntityModule().trackedEntityAttributeValues
                .byTrackedEntityAttribute().eq(attrUid)
                .byTrackedEntityInstance().eq(teiUid).one().blockingGet().value();
        File file = new File(FileResourcesUtil.getUploadDirectory(getApplicationContext()), fileName);
        upload(file, teiUid, attrUid);
    }

    private void upload(File file, String teiOrEvent, String attrUid) {
        Completable.fromCallable(() -> {
            Response<ResponseBody> response = fileService.uploadFile(getFilePart(file)).execute();

            if (response.isSuccessful()) {
                String jsonResponse = response.body().string();
                FileResourceResponse fileResourceResponse = new Gson().fromJson(jsonResponse, FileResourceResponse.class);

                if (d2.trackedEntityModule().trackedEntityAttributes.uid(attrUid).blockingExists())
                    d2.trackedEntityModule().trackedEntityAttributeValues.value(attrUid, teiOrEvent).set(
                            fileResourceResponse.getResponse().getFileResource().getId()
                    );
                else
                    d2.trackedEntityModule().trackedEntityDataValues.value(teiOrEvent, attrUid).set(
                            fileResourceResponse.getResponse().getFileResource().getId()
                    );
                return file.delete();

            } else
                return false;

        }).blockingAwait();
    }

    private void downloadFileResources() {

        triggerNotification("File processor", "Downloading files...", file_download_channel);

        List<TrackedEntityAttribute> imageAttr = d2.trackedEntityModule().trackedEntityAttributes
                .byValueType().eq(ValueType.IMAGE).blockingGet();

        List<String> attrUids = new ArrayList<>();
        for (TrackedEntityAttribute attribute : imageAttr)
            attrUids.add(attribute.uid());

        List<TrackedEntityAttributeValue> imageValues = d2.trackedEntityModule().trackedEntityAttributeValues
                .byTrackedEntityAttribute().in(attrUids).blockingGet();

        int downloadCount = 1;
        for (TrackedEntityAttributeValue attributeValue : imageValues) {
            triggerNotification("File processor", String.format("Downloading file %s/%s", downloadCount++, imageValues.size()), file_download_channel);
            Completable.fromCallable(() -> {
                Response<ResponseBody> response = fileService.getFile(attributeValue.trackedEntityInstance(),
                        attributeValue.trackedEntityAttribute()).execute();
                if (response.isSuccessful()) {
                    return writeResponseBodyToDisk(response.body(), FileResourcesUtil.generateFileName(attributeValue.trackedEntityInstance(), attributeValue.trackedEntityAttribute()));
                } else
                    return false;
            }).blockingAwait();

        }
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String generatedFileName) {
        try {
            File futureStudioIconFile = new File(FileResourcesUtil.getDownloadDirectory(getApplicationContext()), generatedFileName);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

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

    private MultipartBody.Part getFilePart(File file) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getPath());
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (type == null)
            type = "image/*";
        return MultipartBody.Part.createFormData("file", file.getName(), RequestBody.create(MediaType.parse(type), file));
    }

    private interface FileService {
        @GET("trackedEntityInstances/{teiUid}/{attrUid}/image")
        Call<ResponseBody> getFile(@Path("teiUid") String teiUid, @Path("attrUid") String attrUid);

        @Multipart
        @POST("fileResources")
        Call<ResponseBody> uploadFile(@Part MultipartBody.Part filePart);

    }

    private void triggerNotification(String title, String content, String channel) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(channel, "File processor", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), channel)
                        .setSmallIcon(R.drawable.ic_file_upload)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setAutoCancel(false)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager.notify(FILE_ID, notificationBuilder.build());
    }

    private void cancelNotification() {
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());
        notificationManager.cancel(FILE_ID);
    }
}
