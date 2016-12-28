package com.odoo.followup.orm.models.services;

import android.content.Context;

import com.odoo.followup.orm.OModel;
import com.odoo.followup.orm.models.ResPartner;
import com.odoo.followup.orm.sync.SyncService;

public class ContactSyncService extends SyncService {

    @Override
    public OModel getModel(Context context) {
        return new ResPartner(context);
    }
}
