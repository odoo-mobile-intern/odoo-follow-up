package com.odoo.followup.orm.models;

import android.content.Context;

import com.odoo.followup.orm.ColumnType;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;

public class ResPartner extends OModel {
    public static final String TAG = ResPartner.class.getSimpleName();

    OColumn name = new OColumn("Name", ColumnType.VARCHAR);
    OColumn company_type = new OColumn("Company Type", ColumnType.VARCHAR);
    OColumn street = new OColumn("Street", ColumnType.VARCHAR);
    OColumn street2 = new OColumn("Street2", ColumnType.VARCHAR);
    OColumn city = new OColumn("City", ColumnType.VARCHAR);
    OColumn state_id = new OColumn("State id", ColumnType.MANY2ONE, "res.state");
    OColumn country_id = new OColumn("Country id", ColumnType.MANY2ONE, "res.country");
    OColumn zip = new OColumn("Zip", ColumnType.VARCHAR);
    OColumn website = new OColumn("Website", ColumnType.VARCHAR);
    OColumn phone = new OColumn("Phone", ColumnType.VARCHAR);
    OColumn mobile = new OColumn("Mobile", ColumnType.VARCHAR);
    OColumn fax = new OColumn("Fax", ColumnType.VARCHAR);
    OColumn email = new OColumn("Email", ColumnType.VARCHAR);
    OColumn image_medium = new OColumn("Image", ColumnType.BLOB);
    OColumn customer = new OColumn("Customer", ColumnType.BOOLEAN);

    public ResPartner(Context context) {
        super(context, "res.partner");
    }

    @Override
    public String authority() {
        return "com.odoo.followup.contacts.sync";
    }
}
