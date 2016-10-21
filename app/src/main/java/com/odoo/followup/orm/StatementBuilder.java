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
