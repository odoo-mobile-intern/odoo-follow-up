package com.odoo.followup.orm.models;

import android.content.Context;

import com.odoo.followup.orm.ColumnType;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;

public class IrModel extends OModel {

    OColumn model = new OColumn("Model", ColumnType.VARCHAR);
    OColumn last_sync_on = new OColumn("Last Sync Datetime", ColumnType.DATETIME);

    public IrModel(Context context) {
        super(context, "ir.model");
    }
}
