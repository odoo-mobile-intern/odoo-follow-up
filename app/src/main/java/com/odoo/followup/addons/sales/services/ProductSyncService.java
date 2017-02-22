package com.odoo.followup.addons.sales.services;

import android.content.Context;

import com.odoo.followup.addons.sales.models.ProductTemplate;
import com.odoo.followup.orm.OModel;
import com.odoo.followup.orm.sync.SyncService;

public class ProductSyncService extends SyncService {

    @Override
    public OModel getModel(Context context) {
        return new ProductTemplate(context);
    }
}
