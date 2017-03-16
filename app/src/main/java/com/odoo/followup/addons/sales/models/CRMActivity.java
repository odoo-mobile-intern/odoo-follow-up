package com.odoo.followup.addons.sales.models;

import android.content.Context;

import com.odoo.followup.orm.ColumnType;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;

public class CRMActivity extends OModel {

    OColumn name = new OColumn("Activity name", ColumnType.VARCHAR);

    public CRMActivity(Context context) {
        super(context, "crm.activity");
    }
}
