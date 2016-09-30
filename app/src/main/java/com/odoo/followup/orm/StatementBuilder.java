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
 * Created on 30/9/16 4:20 PM
 */
package com.odoo.followup.orm;

public class StatementBuilder {


    public OModel model;

    public StatementBuilder(OModel model) {
        this.model = model;
    }

    public String createStatement() {
        StringBuffer sql = new StringBuffer();
        sql.append("CREATE TABLE IF NOT EXISTS ")
                .append(model.getTableName())
                .append(" (");

        StringBuffer stringBuffer = new StringBuffer();
        for (OColumn column : model.getColumns()) {
            stringBuffer.append(column.name)
                    .append(" ")
                    .append(column.columnType.toString());

            if (column.isPrimaryKey) {
                stringBuffer.append(" PRIMARY KEY ");
            }
            if (column.isAutoIncrement) {
                stringBuffer.append(" AUTOINCREMENT");
            }
            if (column.defValue != null) {
                stringBuffer.append(" DEFAULT '").append(column.defValue.toString()).append("'");
            }
            stringBuffer.append(" , ");
        }

        String SQL = stringBuffer.toString();
        sql.append(SQL.substring(0, stringBuffer.length() - 2)).append(")");
        return sql.toString();
    }

}
