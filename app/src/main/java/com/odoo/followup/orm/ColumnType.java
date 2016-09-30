package com.odoo.followup.orm;

public enum ColumnType {

    VARCHAR("VARCHAR"),
    INTEGER("INTEGER"),
    FLOAT("FLOAT"),
    BOOLEAN("BOOLEAN"),
    BLOB("BLOG");

    String type;

    ColumnType(String type) {
        this.type = type;
    }

}
