package com.odoo.followup.orm;

public enum ColumnType {

    VARCHAR("VARCHAR"),
    INTEGER("INTEGER"),
    FLOAT("FLOAT"),
    BOOLEAN("BOOLEAN"),
    BLOB("BLOB"),
    MANY2ONE("INTEGER"),
    DATETIME("VARCHAR"),
    TEXT("TEXT");

    String type;

    ColumnType(String type) {
        this.type = type;
    }

}
