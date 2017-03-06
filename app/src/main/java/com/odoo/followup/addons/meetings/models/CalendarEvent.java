package com.odoo.followup.addons.meetings.models;

import android.content.Context;

import com.odoo.followup.R;
import com.odoo.followup.orm.ColumnType;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;

public class CalendarEvent extends OModel {

    OColumn name = new OColumn("Meeting Subject", ColumnType.VARCHAR);
    OColumn start_datetime = new OColumn("start date", ColumnType.DATETIME);
    OColumn duration = new OColumn("Duration", ColumnType.FLOAT);
    OColumn allday = new OColumn("All day", ColumnType.VARCHAR);
    OColumn description = new OColumn("Description", ColumnType.TEXT);
    OColumn privacy = new OColumn("Privacy", ColumnType.VARCHAR);

    public CalendarEvent(Context context) {
        super(context, "calendar.event");
    }

    @Override
    public String authority() {
        return getAuthority(R.string.authority_meetings);
    }
}
