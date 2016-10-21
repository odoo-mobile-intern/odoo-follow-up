package com.odoo.followup.orm.data;
import android.database.Cursor;

import java.util.HashMap;

public class ListRow extends HashMap<String, Object> {
    public ListRow(Cursor cursor) {

        for (String column : cursor.getColumnNames()) {
            int index = cursor.getColumnIndex(column);
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
}
