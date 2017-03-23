package com.odoo.followup.orm.data;

import android.database.Cursor;
import android.util.Log;

import com.odoo.core.rpc.helper.ORecordValues;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;

import java.util.HashMap;

public class ListRow extends HashMap<String, Object> {
    public ListRow() {

    }

    public ListRow(Cursor cursor) {

        for (String column : cursor.getColumnNames()) {
            int index = cursor.getColumnIndex(column);
            if (index != -1) {
                switch (cursor.getType(index)) {
                    case Cursor.FIELD_TYPE_INTEGER:
                        put(column, cursor.getInt(index));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        put(column, cursor.getString(index));
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        put(column, cursor.getBlob(index));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        put(column, cursor.getFloat(index));
                        break;
                }
            } else {
                Log.d("index -1 for column :", column);
            }
        }
    }

    public int getInt(String key) {
        return containsKey(key) ? Integer.parseInt(get(key) + "") : null;
    }

    public String getString(String key) {
        return containsKey(key) ? get(key) + "" : "false";
    }

    public Float getFloat(String key) {
        return containsKey(key) ? Float.parseFloat(get(key) + "") : null;
    }

    public Long getLong(String key) {
        return containsKey(key) ? Long.parseLong(get(key) + "") : null;
    }

    public ORecordValues toRecordValues(OModel model) {
        ORecordValues recordValues = new ORecordValues();
        for (String key : keySet()) {
            OColumn column = model.getColumn(key);
            String columnName = column.getStoreColumn();
            if (!column.isLocal) {
                switch (column.columnType) {
                    case BLOB:
                    case VARCHAR:
                    case DATETIME:
                        if (!getString(key).equals("false"))
                            recordValues.put(columnName, get(key));
                        else
                            recordValues.put(columnName, "");
                        break;
                    case FLOAT:
                        if (!getString(key).equals("false"))
                            recordValues.put(columnName, getFloat(key));
                        else
                            recordValues.put(columnName, false);
                        break;
                    case INTEGER:
                        if (!getString(key).equals("false"))
                            recordValues.put(columnName, getInt(key));
                        else
                            recordValues.put(columnName, false);
                        break;
                    case BOOLEAN:
                        recordValues.put(columnName, getString(key).equals("true"));
                        break;
                    case MANY2ONE:
                        if (get(key) != null && !getString(key).equals("false")) {
                            OModel relModel = model.createModel(column.relModel);
                            int rel_id = relModel.selectRowId(getInt(key));
                            if (rel_id != OModel.INVALID_ROW_ID)
                                recordValues.put(columnName, relModel.selectServerId(getInt(key)));
                            else
                                recordValues.put(columnName, false);
                        } else {
                            recordValues.put(columnName, false);
                        }
                        break;
                }
            }
        }
        return recordValues;
    }
}
