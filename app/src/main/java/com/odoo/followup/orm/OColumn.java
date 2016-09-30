package com.odoo.followup.orm;

public class OColumn {

    public String name, label, relModel;
    public Boolean isPrimaryKey = false, isAutoIncrement = false, isLocal = false;
    public ColumnType columnType;
    public Object defValue;


    public OColumn(String label, ColumnType columnType) {
        this(label, columnType, null);
    }

    public OColumn(String label, ColumnType columnType, String relModel) {
        this.label = label;
        this.columnType = columnType;
        this.relModel = relModel;
    }

    public OColumn makePrimaryKey() {
        isPrimaryKey = true;
        return this;
    }

    public OColumn makeAtoIncrement() {
        isAutoIncrement = true;
        return this;
    }

    public OColumn makeLocal() {
        isLocal = true;
        return this;
    }

    public OColumn setDefaultValue(Object defValue) {
        this.defValue = defValue;
        return this;
    }

}
