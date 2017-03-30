package com.odoo.followup.addons.sales.models;

import android.content.Context;

import com.odoo.core.rpc.helper.ODomain;
import com.odoo.followup.R;
import com.odoo.followup.orm.ColumnType;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;

public class CRMLead extends OModel {

    OColumn name = new OColumn("Name", ColumnType.VARCHAR);
    OColumn partner_id = new OColumn("Customer Id", ColumnType.MANY2ONE, "res.partner");
    OColumn email_from = new OColumn("Email", ColumnType.VARCHAR);
    OColumn phone = new OColumn("Phone", ColumnType.VARCHAR);
    OColumn next_activity_id = new OColumn("Next Activity id", ColumnType.MANY2ONE, "crm.activity");
    OColumn planned_revenue = new OColumn("Planned revenue", ColumnType.FLOAT);
    OColumn kanban_state = new OColumn("Kanban State", ColumnType.VARCHAR);
    OColumn probability = new OColumn("Probability", ColumnType.FLOAT);
    OColumn title_action = new OColumn("Title action", ColumnType.VARCHAR);
    OColumn date_deadline = new OColumn("closing date", ColumnType.DATETIME);
    OColumn description = new OColumn("Description", ColumnType.TEXT);
    OColumn date_action = new OColumn("Date action", ColumnType.DATETIME); //next activity date
    OColumn type = new OColumn("Type", ColumnType.VARCHAR);
    OColumn stage_id = new OColumn("Stage", ColumnType.MANY2ONE, "crm.stage");
    OColumn user_id = new OColumn("User", ColumnType.MANY2ONE, "res.users");

    public CRMLead(Context context) {
        super(context, "crm.lead");
    }

    @Override
    public ODomain syncDomain() {
        ODomain domain = new ODomain();
        domain.add("user_id", "=", getUser().getUserId());
        domain.add("type", "=", "opportunity");
        return domain;
    }

    @Override
    public String authority() {
        return getAuthority(R.string.authority_crm_lead);
    }
}
