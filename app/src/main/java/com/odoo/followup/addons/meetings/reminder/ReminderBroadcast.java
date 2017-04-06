package com.odoo.followup.addons.meetings.reminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.odoo.followup.HomeActivity;
import com.odoo.followup.R;

public class ReminderBroadcast extends BroadcastReceiver {
    public NotificationManager mManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            mManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            generateNotification(context, intent.getStringExtra("title"),
                    intent.getStringExtra("desc"), intent.getIntExtra("id", 0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateNotification(Context context, String title, String desc, int id) {
        Intent intent1 = new Intent(context, HomeActivity.class);
        intent1.putExtra("base_id", id);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setContentText(desc);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setAutoCancel(true);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent1, 0);
        builder.setContentIntent(pendingIntent);

        mManager.notify(id, builder.build());
    }
}
