package com.odoo.followup.addons.sales.models;

import android.content.Context;

import com.odoo.core.rpc.helper.ODomain;
import com.odoo.followup.orm.ColumnType;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;

public class CRMLead extends OModel {

    OColumn name = new OColumn("Name", ColumnType.VARCHAR);

    public CRMLead(Context context) {
        super(context, "crm.lead");
    }

    @Override
    public ODomain syncDomain() {
        ODomain domain = new ODomain();
        domain.add("user_id", "=", getUser().getUserId());
        return domain;
    }

    @Override
    public String authority() {
        return "com.odoo.followup.leads.sync";
    }
}
