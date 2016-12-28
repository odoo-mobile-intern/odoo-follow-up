package com.odoo.followup.orm.models.provides;

import android.content.Context;

import com.odoo.followup.orm.OModel;
import com.odoo.followup.orm.models.ResPartner;
import com.odoo.followup.orm.sync.provider.BaseContentProvider;

public class ContactsProvider extends BaseContentProvider {

    @Override
    public OModel getModel(Context context) {
        return new ResPartner(context);
    }
}
