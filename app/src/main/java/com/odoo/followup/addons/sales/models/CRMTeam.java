package com.odoo.followup.addons.sales.models;

import android.content.Context;

import com.odoo.followup.R;
import com.odoo.followup.orm.ColumnType;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;

public class CRMTeam extends OModel {

    OColumn name = new OColumn("Name", ColumnType.VARCHAR);
    OColumn invoiced_target = new OColumn("Invoice target", ColumnType.INTEGER);
    OColumn invoiced = new OColumn("Invoiced", ColumnType.INTEGER);

    public CRMTeam(Context context) {
        super(context, "crm.team");
    }

    @Override
    public String authority() {
        return getAuthority(R.string.crm_team_authority);
    }
}
