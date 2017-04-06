package com.odoo.followup.addons.meetings.models;

import android.content.Context;

import com.odoo.core.utils.ODateUtils;
import com.odoo.followup.R;
import com.odoo.followup.addons.meetings.reminder.AlarmUtils;
import com.odoo.followup.orm.ColumnType;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;
import com.odoo.followup.orm.data.ListRow;

import java.util.Date;
import java.util.List;

public class CalendarEvent extends OModel {

    CalendarEvent calendarEvent;
    AlarmUtils alarm = new AlarmUtils(getContext());

    OColumn name = new OColumn("Meeting Subject", ColumnType.VARCHAR);
    OColumn start = new OColumn("Start date", ColumnType.DATETIME);
    OColumn stop = new OColumn("End date", ColumnType.DATETIME);
    OColumn duration = new OColumn("Duration", ColumnType.FLOAT);
    OColumn allday = new OColumn("All day", ColumnType.VARCHAR);
    OColumn location = new OColumn("Location", ColumnType.VARCHAR);
    OColumn description = new OColumn("Description", ColumnType.TEXT);
    OColumn privacy = new OColumn("Privacy", ColumnType.VARCHAR);

    public CalendarEvent(Context context) {
        super(context, "calendar.event");
    }

    @Override
    public String authority() {
        return getAuthority(R.string.authority_meetings);
    }

    @Override
    public void onSyncFinished() {
        alarm.cancelAllAlarm();
        calendarEvent = new CalendarEvent(getContext());
        List<ListRow> rows = calendarEvent.select();

        if (rows != null)
            for (ListRow row : rows) {
                String nextActivityDate = row.getString("start");

                Date now = ODateUtils.getDate();
                Date date = ODateUtils.createDateObject(nextActivityDate, ODateUtils.DEFAULT_FORMAT, false);

                if (now.compareTo(date) < 0) {
                    alarm.setAlarmDateTime(date, row.getString("name"),
                            row.getString("description"), row.getInt("_id"));
                }
            }
    }
}
