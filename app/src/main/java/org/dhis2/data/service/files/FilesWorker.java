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
    private D2 d2;
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

        if (getInputData().getString(MODE) == null || FileMode.valueOf(getInputData().getString(MODE)) == FileMode.DOWNLOAD)
            downloadFileResources();
        else
            uploadBulkResources();

        cancelNotification();
        return Result.success();
    }

    private void uploadBulkResources() {
        triggerNotification("File processor", "Uploading files...", file_upload_channel);
        Completable.fromObservable(
                d2.fileResourceModule().fileResources.upload()
        ).blockingAwait();
    }

    private void downloadFileResources() {

        triggerNotification("File processor", "Downloading files...", file_download_channel);

        Completable.fromObservable(d2.fileResourceModule().download()
                .doOnNext(d2Progress -> triggerNotification("File processor", String.format("Downloading file %s/%s", d2Progress.doneCalls(), d2Progress.totalCalls()), file_download_channel))
        ).blockingAwait();
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
