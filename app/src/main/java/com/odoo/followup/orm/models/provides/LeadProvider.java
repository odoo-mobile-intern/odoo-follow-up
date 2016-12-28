package com.odoo.followup.orm.models.provides;

import android.content.Context;

import com.odoo.followup.orm.OModel;
import com.odoo.followup.orm.models.CRMLead;
import com.odoo.followup.orm.sync.provider.BaseContentProvider;

public class LeadProvider extends BaseContentProvider {

    @Override
    public OModel getModel(Context context) {
        return new CRMLead(context);
    }
}
