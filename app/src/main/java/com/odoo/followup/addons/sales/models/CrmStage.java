package com.odoo.followup.addons.sales.models;

import android.content.Context;

import com.odoo.followup.orm.ColumnType;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;

public class CrmStage extends OModel {

    OColumn name = new OColumn("Name", ColumnType.VARCHAR);
    OColumn sequence = new OColumn("Sequence", ColumnType.INTEGER);

    public CrmStage(Context context) {
        super(context, "crm.stage");
    }
}
