package utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;


import filesscan.codingexercise.MainActivity;
import filesscan.codingexercise.R;

/**
 * Created by sunil on 9/9/17.
 */

public class Commons {
    public static final String UPDATE = "update";
    public static final String SAVE = "save";

    public static void showNotifcation(String message, Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        Notification notification = mBuilder
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(false)
                .build();

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_view);
        remoteViews.setTextViewText(R.id.tv_msg, message);
        remoteViews.setProgressBar(R.id.progressBar, 0, 0, true);
        notification.contentView = remoteViews;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);

    }
}
