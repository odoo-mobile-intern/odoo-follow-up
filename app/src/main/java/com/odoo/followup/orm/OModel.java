package com.odoo.followup.orm;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.orm.models.ModelRegistry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class OModel extends SQLiteOpenHelper implements BaseColumns {

    public static final String TAG = OModel.class.getSimpleName();
    private static final String DB_NAME = "FollowUpSQLite.db";
    private static final int DB_VERSION = 1;
    public static final int INVALID_ROW_ID = -1;

    OColumn _id = new OColumn("Local Id", ColumnType.INTEGER).makePrimaryKey()
            .makeAtoIncrement().makeLocal();
    OColumn id = new OColumn("Server Id", ColumnType.INTEGER);
    OColumn write_date = new OColumn("Write date", ColumnType.DATETIME).makeLocal().setDefaultValue("false");
    OColumn is_dirty = new OColumn("Is dirty", ColumnType.BOOLEAN).makeLocal().setDefaultValue("false");
    private String mModelName;
    private Context mContext;

    public OModel(Context context, String model) {
        super(context, DB_NAME, null, DB_VERSION);
        mModelName = model;
        mContext = context;
    }

    public static OModel createInstance(String modelName, Context mContext) {

        HashMap<String, OModel> models = new ModelRegistry().models(mContext);
        for (String key : models.keySet()) {
            OModel model = models.get(key);
            if (model.getModelName().equals(modelName)) {
                return model;
            }
        }
        return null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        HashMap<String, OModel> map = new ModelRegistry().models(mContext);
        for (OModel model : map.values()) {
            StatementBuilder statementBuilder = new StatementBuilder(model);
            String sql = statementBuilder.createStatement();
            if (sql != null) {
                db.execSQL(sql);
                Log.d(TAG, "Table created: " + model.getTableName());
            }
        }

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

    public String authority() {
        return "com.odoo.followup.appdata.sync";
    }

    public Uri getUri() {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority());
        uriBuilder.appendPath(getModelName());
        uriBuilder.scheme("content");
        return uriBuilder.build();
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

    public int update(ContentValues values, String where, String... args) {
        SQLiteDatabase database = getReadableDatabase();
        int id = database.update(getTableName(), values, where, args);
        database.close();
        return id;
    }

    public void deleteAll() {
        delete(null);
    }

    public int delete(String where, String... args) {
        SQLiteDatabase database = getWritableDatabase();
        int id = database.delete(getTableName(), where, args);
        database.close();
        return id;
    }

    public int count() {
        int count = 0;
        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor = database.rawQuery("SELECT COUNT(*) AS TOTAL FROM " + getTableName(), null);

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        database.close();
        return count;
    }

    public List<ListRow> select() {
        return select(null);
    }

    public int selectRowId(int server_id) {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(getTableName(), new String[]{"_id"}, "id = ? ",
                new String[]{server_id + ""}, null, null, null);
        int row_id = INVALID_ROW_ID;
        if (cursor.moveToFirst()) {
            row_id = cursor.getInt(0);
        }
        database.close();
        cursor.close();
        return row_id;
    }

    public List<ListRow> select(String where, String... args) {
        List<ListRow> rows = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        args = args.length > 0 ? args : null;
        Cursor cursor = database.query(getTableName(), null, where, args, null, null, "_id DESC");
        if (cursor.moveToFirst()) {
            do {
                rows.add(new ListRow(cursor));
            } while (cursor.moveToNext());
        }

        database.close();
        cursor.close();
        return rows;
    }

    public String[] getServerColumns() {
        List<String> serverColumns = new ArrayList<>();
        for (OColumn column : getColumns()) {
            if (!column.isLocal) {
                serverColumns.add(column.name);
            }
        }
        return serverColumns.toArray(new String[serverColumns.size()]);
    }

    public int updateOrCreate(ContentValues values, String where, String... args) {
        List<ListRow> records = select(where, args);
        if (records.size() > 0) {
            ListRow row = records.get(0);
            update(values, where, args);
            return row.getInt(_ID);
        } else {
            create(values);
        }
        return 0;
    }

    public ContentProviderResult[] batchInsert(List<ContentValues> values) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        for (ContentValues value : values) {
            operations.add(ContentProviderOperation.newInsert(getUri())
                    .withValues(value).withYieldAllowed(true).build());
        }
        try {
            return mContext.getContentResolver().applyBatch(authority(), operations);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void batchUpdate(List<ContentValues> values) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();
        for (ContentValues value : values) {
            operations.add(ContentProviderOperation
                    .newUpdate(Uri.withAppendedPath(getUri(), value.get("_id") + ""))
                    .withValues(value)
                    .withYieldAllowed(true)
                    .build());
        }
        try {
            mContext.getContentResolver().applyBatch(authority(), operations);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
