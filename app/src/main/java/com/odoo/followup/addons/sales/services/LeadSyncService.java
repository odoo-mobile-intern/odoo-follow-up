package com.odoo.followup.addons.sales.services;

import android.content.Context;

import com.odoo.followup.orm.OModel;
import com.odoo.followup.addons.sales.models.CRMLead;
import com.odoo.followup.orm.sync.SyncService;

public class LeadSyncService extends SyncService {

    @Override
    public OModel getModel(Context context) {
        return new CRMLead(context);
    }
}
