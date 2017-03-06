package com.odoo.followup.addons.meetings.service;

import android.content.Context;

import com.odoo.followup.addons.meetings.models.CalendarEvent;
import com.odoo.followup.orm.OModel;
import com.odoo.followup.orm.sync.SyncService;

public class meetingService extends SyncService {
    @Override
    public OModel getModel(Context context) {
        return new CalendarEvent(context);
    }
}
