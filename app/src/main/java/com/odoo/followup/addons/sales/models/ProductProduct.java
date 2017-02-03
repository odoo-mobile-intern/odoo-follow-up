package com.odoo.followup.addons.sales.models;

import android.content.Context;

import com.odoo.followup.orm.ColumnType;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;

public class ProductProduct extends OModel {

    OColumn name = new OColumn("Name", ColumnType.VARCHAR);

    public ProductProduct(Context context) {
        super(context, "product.product");
    }


    @Override
    public String authority() {
        return "com.odoo.followup.products.sync";
    }
}
