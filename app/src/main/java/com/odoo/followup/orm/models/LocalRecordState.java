package com.odoo.followup.orm.models;

import android.content.ContentValues;
import android.content.Context;

import com.odoo.core.utils.ODateUtils;
import com.odoo.followup.orm.ColumnType;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;
import com.odoo.followup.orm.data.ListRow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocalRecordState extends OModel {

    OColumn server_id = new OColumn("Server Id", ColumnType.INTEGER);
    OColumn model = new OColumn("Model ", ColumnType.VARCHAR);
    OColumn delete_on = new OColumn("Deleted on ", ColumnType.DATETIME);

    public LocalRecordState(Context context) {
        super(context, "local.record.state");
    }

    public boolean isValid(String model, int server_id, String write_date) {
        List<ListRow> records = select("model = ? and server_id = ?", model, server_id + "");
        if (!records.isEmpty()) {
            ListRow row = records.get(0);
            Date delete_date = ODateUtils.createDateObject(row.getString("delete_on"), ODateUtils.DEFAULT_FORMAT, false);
            Date server_write_date = ODateUtils.createDateObject(write_date, ODateUtils.DEFAULT_FORMAT, false);
            if (delete_date.compareTo(server_write_date) < 0) {
                delete(row.getInt("_id"));
            } else return false;
        }
        return true;
    }

    public void addDeleted(String modelName, List<Integer> serverIds) {
        List<ContentValues> values = new ArrayList<>();
        for (int id : serverIds) {
            ContentValues vals = new ContentValues();
            vals.put("model", modelName);
            vals.put("server_id", id);
            vals.put("delete_on", ODateUtils.getUTCDateTime());
            values.add(vals);
        }
        batchInsert(values);
    }

    public List<Integer> getServerIds(String model) {
        List<Integer> ids = new ArrayList<>();
        List<ListRow> records = select("model = ?", model);
        for (ListRow row : records) {
            ids.add(row.getInt("server_id"));
        }
        return ids;
    }
}
