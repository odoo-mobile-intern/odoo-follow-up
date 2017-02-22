package com.odoo.followup.addons.sales.models;

import android.content.Context;

import com.odoo.followup.orm.ColumnType;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;

public class ProductTemplate extends OModel {

    OColumn name = new OColumn("Name", ColumnType.VARCHAR);
    OColumn image_medium = new OColumn("Product Image", ColumnType.BLOB);
    OColumn sale_ok = new OColumn("Can be Sold", ColumnType.VARCHAR);
    OColumn list_price = new OColumn("Sale Price", ColumnType.FLOAT);
    OColumn default_code = new OColumn("Internal Reference", ColumnType.VARCHAR);
    OColumn description_sale = new OColumn("Description", ColumnType.VARCHAR);

    public ProductTemplate(Context context) {
        super(context, "product.template");
    }

    @Override
    public String authority() {
        return "com.odoo.followup.products.sync";
    }
}
