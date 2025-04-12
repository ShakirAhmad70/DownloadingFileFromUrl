package com.shak.downloadanduploadfiles;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends Service {

    public static final String EXTRA_FILE_URL = "file_url";
    public static final String EXTRA_FILE_NAME = "file_name";
    private static final String CHANNEL_ID = "download_channel";
    private static final int NOTIFICATION_ID = 101;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String fileUrl = intent.getStringExtra(EXTRA_FILE_URL);
        final String fileName = intent.getStringExtra(EXTRA_FILE_NAME);

        createNotificationChannel();

        new Thread(() -> {
            boolean isSuccess = downloadFile(fileUrl, fileName);

            // Show final notification after download completes (or fails)
            if (isSuccess) {
                showFinalNotification("Download completed", "File downloaded successfully.");
            } else {
                showFinalNotification("Download failed", "Unable to download the file.");
            }

            stopSelf();
        }).start();

        return START_STICKY;
    }


    private boolean downloadFile(String fileUrl, String fileName) {
        HttpURLConnection connection = null;
        InputStream is = null;
        FileOutputStream fos = null;
        boolean isSuccess;

        try {
            URL url = new URL(fileUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            //Check is connection is OK
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e("Download File", "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage());
                return false;
            }

            //get file length for progress calculation
            int fileLength = connection.getContentLength();

            is = connection.getInputStream();
            File file = new File(getExternalFilesDir(null), fileName);
            fos = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;
            int totalBytesRead = 0;

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Downloading File")
                    .setContentText("Download in progress")
                    .setSmallIcon(R.drawable.ic_download)
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if(fileLength > 0) {
                builder.setProgress(fileLength, 0, false);
            } else {
                builder.setProgress(0, 0, true);
            }

            notificationManager.notify(NOTIFICATION_ID, builder.build());


            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                //update notification with progress if file length is known
                if(fileLength > 0) {
                    builder.setProgress(fileLength, totalBytesRead, false);
                    notificationManager.notify(NOTIFICATION_ID, builder.build());
                }
            }

            fos.flush();
            isSuccess = true;
            Log.d("Download File", "File Downloaded Successfully to: " + file.getAbsolutePath());

        } catch (Exception e) {
            Log.e("DownloadService", "Error downloading file" + e.getMessage());
            isSuccess = false;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (is != null) {
                    is.close();
                }
                if(connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                Log.e("DownloadService", "Error closing streams" + e.getMessage());
            }
        }

        return isSuccess;
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Download Channel";
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if(manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        startForeground(NOTIFICATION_ID, new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Preparing download")
                .setContentText("Download starting...")
                .setSmallIcon(R.drawable.ic_download)
                .setOnlyAlertOnce(true)
                .setProgress(0, 0, true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build());
    }

    private void showFinalNotification(String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_download)
                .setAutoCancel(true)
                .setProgress(0, 0, false);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        stopForeground(true);

        notificationManager.notify(NOTIFICATION_ID + 1, builder.build());  // Notice Notification ID is different here
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}