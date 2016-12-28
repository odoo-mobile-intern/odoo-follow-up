package com.odoo.followup.orm.sync;

import android.content.ContentValues;
import android.util.Log;

import com.odoo.core.rpc.helper.utils.gson.OdooRecord;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;

import java.util.ArrayList;
import java.util.List;

public class OdooRecordUtils {

    private OModel model;
    private List<OColumn> columns = new ArrayList<>();
    private List<ContentValues> recordValuesToUpdate = new ArrayList<>();
    private List<ContentValues> recordValuesToInsert = new ArrayList<>();

    public static OdooRecordUtils getInstance(OModel model) {
        return new OdooRecordUtils(model);
    }

    private OdooRecordUtils(OModel model) {
        this.model = model;
        columns.addAll(model.getColumns());
    }

    public void processRecord(OdooRecord record) {
        ContentValues values = new ContentValues();
        for (OColumn column : columns) {
            if (!column.isLocal) {
                switch (column.columnType) {
                    case DATETIME:
                    case VARCHAR:
                    case BLOB:
                        values.put(column.name, record.getString(column.name));
                        break;
                    case BOOLEAN:
                        break;
                    case FLOAT:
                        values.put(column.name, record.getDouble(column.name));
                        break;
                    case INTEGER:
                        values.put(column.name, record.getDouble(column.name).intValue());
                        break;

                    case MANY2ONE:

                        //TODO: check..
                        Log.e(">>", "Check..... " + column.relModel + "");

                        break;
                }
            }
        }
        int row_id = model.selectRowId(record.getInt("id"));
        if (row_id == OModel.INVALID_ROW_ID) {
            recordValuesToInsert.add(values);
        } else {
            values.put("_id", row_id);
            recordValuesToUpdate.add(values);
        }
    }

    public List<ContentValues> getRecordValuesToInsert() {
        return recordValuesToInsert;
    }

    public List<ContentValues> getRecordValuesToUpdate() {
        return recordValuesToUpdate;
    }
}
