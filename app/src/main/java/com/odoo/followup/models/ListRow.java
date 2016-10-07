/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 7/10/16 11:50 AM
 */
package com.odoo.followup.models;

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
        return containsKey(key) ? Integer.parseInt(get(key) + "") : -1;
    }

    public String getString(String key) {
        return containsKey(key) ? get(key) + "" : "false";
    }

    public Float getFloat(String key) {
        return containsKey(key) ? Float.parseFloat(get(key) + "") : -1;
    }
}
