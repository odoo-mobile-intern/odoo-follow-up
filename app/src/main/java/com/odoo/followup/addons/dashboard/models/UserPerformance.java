package com.odoo.followup.addons.dashboard.models;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

import com.odoo.core.rpc.Odoo;
import com.odoo.core.rpc.handler.OdooVersionException;
import com.odoo.core.rpc.helper.OArguments;
import com.odoo.core.rpc.helper.utils.gson.OdooResult;
import com.odoo.followup.orm.ColumnType;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;
import com.odoo.followup.orm.data.ListRow;

import java.util.HashMap;

public class UserPerformance extends OModel {

    OColumn type = new OColumn("Type", ColumnType.VARCHAR);
    OColumn field_name = new OColumn("Field", ColumnType.VARCHAR);
    OColumn field_value = new OColumn("Value", ColumnType.VARCHAR);

    public UserPerformance(Context context) {
        super(context, "user.performance");
    }

    public void syncPerformance() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                updateData();
                return null;
            }
        }.execute();
    }

    private void updateData() {
        try {
            Odoo odoo = Odoo.createWithUser(getContext(), getUser());
            OdooResult result = odoo.callMethod("crm.lead", "retrieve_sales_dashboard", new OArguments(), null);
            if (result != null && !result.containsKey("error")) {
                for (String key : result.keySet()) {
                    if (result.get(key) instanceof Double) {
                        ContentValues values = new ContentValues();
                        values.put("type", key);
                        values.put("field_name", key);
                        values.put("field_value", result.getInt(key));
                        updateOrCreate(values, "field_name = ? and type = ?", key, key);
                    } else {
                        OdooResult map = result.getMap(key);
                        for (String subKey : map.keySet()) {
                            ContentValues values = new ContentValues();
                            values.put("type", key);
                            values.put("field_name", subKey);
                            values.put("field_value", map.getInt(subKey));
                            updateOrCreate(values, "field_name = ? and type = ?", subKey, key);
                        }
                    }
                }
            }
        } catch (OdooVersionException e) {
            e.printStackTrace();
        }
    }

    public ListRow getUserPerformance() {
        ListRow row = new ListRow();
        HashMap<String, ListRow> items = new HashMap<>();
        for (ListRow item : select()) {
            String type = item.getString("type");
            ListRow itemRow = items.containsKey(type) ? items.get(type) : new ListRow();
            itemRow.put(item.getString("field_name"), item.get("field_value"));
            items.put(type, itemRow);
        }
        for (String key : items.keySet()) {
            row.put(key, items.get(key));
        }
        return row;
    }

}
