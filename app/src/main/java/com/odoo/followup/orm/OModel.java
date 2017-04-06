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
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.Log;

import com.odoo.core.rpc.helper.ODomain;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.ODateUtils;
import com.odoo.followup.orm.annotation.Odoo;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.orm.models.IrModel;
import com.odoo.followup.orm.models.LocalRecordState;
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
    OColumn id = new OColumn("Server Id", ColumnType.INTEGER).setDefaultValue(0);
    OColumn write_date = new OColumn("Write date", ColumnType.DATETIME).makeLocal().setDefaultValue("false");

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

    public Context getContext() {
        return mContext;
    }

    public String getAuthority(@StringRes int res_id) {
        return mContext.getString(res_id);
    }

    public Uri getUri() {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority());
        uriBuilder.appendPath(getModelName());
        uriBuilder.scheme("content");
        return uriBuilder.build();
    }

    public OColumn getColumn(String column) {
        Field field = null;
        try {
            field = getClass().getDeclaredField(column);
        } catch (NoSuchFieldException e) {
            try {
                field = getClass().getSuperclass().getDeclaredField(column);
            } catch (NoSuchFieldException e1) {
            }
        }
        if (field != null) {
            field.setAccessible(true);
            if (field.getType().isAssignableFrom(OColumn.class)) {
                try {
                    OColumn columnObj = (OColumn) field.get(this);
                    columnObj.name = field.getName();
                    columnObj.storeName = getStoreName(field);
                    return columnObj;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private String getStoreName(Field field) {
        Odoo.storeColumn storeColumn = field.getAnnotation(Odoo.storeColumn.class);
        return storeColumn != null ? storeColumn.value() : null;
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
                    column.storeName = getStoreName(field);
                    columnList.add(column);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return columnList;
    }

    public int create(ContentValues values) {
        Long id = null;
        if (getUri() != null) {
            Uri uri = mContext.getContentResolver().insert(getUri(), values);
            return Integer.parseInt(uri.getLastPathSegment());
        } else {
            SQLiteDatabase database = getWritableDatabase();
            values.put("write_date", ODateUtils.getUTCDateTime());
            id = database.insert(getTableName(), null, values);
            database.close();
            return id.intValue();
        }
    }

    public ListRow getCallDetails(String number) {
        ListRow row = null;
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(getTableName(), null, "mobile = ?", new String[]{number},
                null, null, null);
        if (cursor.moveToFirst()) {
            row = new ListRow(cursor);
        }
        database.close();
        cursor.close();
        return row;
    }

    public int update(ContentValues values, String where, String... args) {
        SQLiteDatabase database = getWritableDatabase();
        values.put("write_date", ODateUtils.getUTCDateTime());
        int id = database.update(getTableName(), values, where, args);
        getContext().getContentResolver().notifyChange(getUri(), null);
        database.close();
        return id;
    }

    public int update(ContentValues values, int row_id) {
        return mContext.getContentResolver().update(Uri.withAppendedPath(getUri(), row_id + ""),
                values, null, null);
    }

    public int delete(String where, String... args) {
        LocalRecordState recordState = new LocalRecordState(mContext);
        List<Integer> serverIds = selectServerIds(where, args);
        recordState.addDeleted(getModelName(), serverIds);

        SQLiteDatabase database = getWritableDatabase();
        int id = database.delete(getTableName(), where, args);
        database.close();
        return id;
    }

    public int delete(int row_id) {
        return delete("_id = ?", row_id + "");
    }

    public int deleteAll(List<Integer> serverIds) {
        SQLiteDatabase database = getWritableDatabase();
        int id = database.delete(getTableName(), "id in (" + TextUtils.join(",", serverIds) + ")", null);
        database.close();
        return id;
    }

    public int delete(int row_id, boolean permenent) {
        SQLiteDatabase database = getWritableDatabase();
        int id = database.delete(getTableName(), "_id = ?", new String[]{row_id + ""});
        database.close();
        return id;
    }

    public int count() {
        return count(null, null);
    }

    public int count(String where, String[] args) {
        int count = 0;
        SQLiteDatabase database = getReadableDatabase();
        String query = where != null ? " WHERE " + where : "";
        Cursor cursor = database.rawQuery("SELECT COUNT(*) AS TOTAL FROM " + getTableName() + query, args);

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        database.close();
        return count;
    }

    public List<ListRow> select() {
        return select(null, null);
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

    public int selectServerId(int row_id) {
        List<Integer> ids = selectServerIds("_id = ?", row_id + "");
        if (!ids.isEmpty()) {
            return ids.get(0);
        }
        return INVALID_ROW_ID;
    }

    public List<Integer> selectServerIds(String where, String... args) {
        List<Integer> ids = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(getTableName(), new String[]{"id"}, where, args,
                null, null, null);
        if (cursor.moveToFirst()) {
            do {
                ids.add(cursor.getInt(0));
            } while (cursor.moveToNext());
        }
        database.close();
        cursor.close();
        return ids;
    }

    public ListRow browse(int row_id) {
        List<ListRow> rows = select("_id = ?", new String[]{row_id + ""});
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<ListRow> select(String where, String[] args) {
        return select(null, null, where, args);
    }

//    public List<ListRow> select(String orderBy, String where, String... args) {
//        return select(null, orderBy, where, args);
//    }

    public List<ListRow> select(String[] projection, String orderBy, String where, String... args) {
        List<ListRow> rows = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        args = args != null && args.length > 0 ? args : null;
        orderBy = orderBy == null ? "_id DESC" : orderBy;
        Cursor cursor = database.query(getTableName(), projection, where, args, null, null, orderBy);
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
        serverColumns.add("write_date");
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

    public OModel createModel(String modelName) {
        return ModelRegistry.getModel(mContext, modelName);
    }

    /**
     * Sets last sync date to current date time
     */
    public void updateLastSyncDate() {
        IrModel model = new IrModel(mContext);
        ContentValues values = new ContentValues();
        values.put("model", getModelName());
        values.put("last_sync_on", ODateUtils.getUTCDateTime());
        model.updateOrCreate(values, "model = ?", getModelName());
    }

    public String getLastSyncDate() {
        IrModel model = new IrModel(mContext);
        List<ListRow> items = model.select("model = ?", new String[]{getModelName()});
        if (!items.isEmpty()) {
            return items.get(0).getString("last_sync_on");
        }
        return null;
    }

    public List<Integer> getServerIds() {
        List<Integer> ids = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.query(getTableName(), new String[]{"id"}, "id != ?", new String[]{"0"}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                ids.add(cursor.getInt(0));
            } while (cursor.moveToNext());
        }
        database.close();
        cursor.close();
        return ids;
    }

    public OUser getUser() {
        return OUser.current(mContext);
    }

    public ODomain syncDomain() {
        return new ODomain();
    }

    public boolean isEmpty() {
        return count() <= 0;
    }

    public String selectWriteDate(int row_id) {
        List<ListRow> rows = select(new String[]{"write_date"}, "_id = ?", row_id + "");
        if (!rows.isEmpty()) {
            return rows.get(0).getString("write_date");
        }
        return null;
    }

    public String getName(int row_id) {
        ListRow row = browse(row_id);
        return row != null ? row.getString("name") : "false";
    }

    public void onSyncFinished(){

    }
}
