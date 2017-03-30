package com.odoo.followup.orm.models;

import android.content.Context;

import com.odoo.followup.orm.ColumnType;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;

public class ResUsers extends OModel {

    OColumn name = new OColumn("Name", ColumnType.VARCHAR);
    OColumn image_medium = new OColumn("Image", ColumnType.BLOB);

    public ResUsers(Context context) {
        super(context, "res.users");
    }
}
