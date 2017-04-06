package com.odoo.followup.addons.meetings.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.odoo.followup.addons.meetings.models.CalendarEvent;
import com.odoo.followup.orm.data.ListRow;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;

public class AlarmUtils {

    private Context context;

    public AlarmUtils(Context context) {
        this.context = context;
    }

    public void setAlarmDateTime(Date date, String title, String desc, int requestCode) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        Intent myIntent = new Intent(context, ReminderBroadcast.class);
        myIntent.putExtra("id", requestCode);
        myIntent.putExtra("title", title);
        if (!desc.equals("false"))
            myIntent.putExtra("desc", desc);
        else myIntent.putExtra("desc", "Description not available");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, myIntent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
    }

    public void cancelAllAlarm() {
        CalendarEvent event = new CalendarEvent(context);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, CalendarEvent.class);

        List<ListRow> rows = event.select();
        if (rows != null) {
            for (ListRow row : rows) {
                PendingIntent sender = PendingIntent.getBroadcast(context, row.getInt("_id"), intent, 0);
                am.cancel(sender);
            }
        }
    }
}
