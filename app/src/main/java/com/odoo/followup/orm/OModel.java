package com.odoo.followup.orm;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.odoo.followup.models.ModelRegistry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OModel extends SQLiteOpenHelper {

    private static final String DB_NAME = "followup.db";
    private static final int DB_VERSION = 1;
    private String mModelName;
    private Context mContext;

    OColumn _id = new OColumn("Local Id", ColumnType.INTEGER).makePrimaryKey()
            .makeAtoIncrement().makeLocal();

    public OModel(Context context, String model) {
        super(context, DB_NAME, null, DB_VERSION);
        mModelName = model;
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        HashMap<String, OModel> map = new ModelRegistry().models(mContext);
        for (OModel model : map.values()) {
            StatementBuilder statementBuilder = new StatementBuilder(model);
            String sql = statementBuilder.createStatement();
            if (sql != null) {
                db.execSQL(sql);
            }
        }
        Log.e(">>>>>>>>>", "model registered...");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public String getTableName() {
        return mModelName.replace(".", "_");
    }

    public String getModelName() {
        return mModelName;
    }

    public List<OColumn> getColumns() {
        List<OColumn> columnList = new ArrayList<>();
        List<Field> fieldList = new ArrayList<>();

        fieldList.addAll(Arrays.asList(getClass().getSuperclass().getDeclaredFields()));
        fieldList.addAll(Arrays.asList(getClass().getDeclaredFields()));

        for (Field field : fieldList) {
            field.setAccessible(true);
            if (field.getType().isAssignableFrom(OColumn.class)) {
                try {
                    OColumn column = (OColumn) field.get(this);
                    column.name = field.getName();
                    columnList.add(column);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return columnList;
    }

    public int create(ContentValues values) {
        SQLiteDatabase database = getWritableDatabase();
        Long id = database.insert(getTableName(), null, values);
        database.close();
        return id.intValue();
    }
}
