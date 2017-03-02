package com.odoo.followup.orm.sync.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.odoo.core.utils.ODateUtils;
import com.odoo.followup.orm.OModel;
import com.odoo.followup.orm.models.ModelRegistry;

public class BaseContentProvider extends ContentProvider {
    public static final String TAG = BaseContentProvider.class.getSimpleName();
    private final int COLLECTION = 1;
    private final int SINGLE_ROW = 2;
    public UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    public OModel getModel(Context context, Uri uri) {
        return ModelRegistry.getModel(context, uri.getPathSegments().get(0));
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    private void setMatcher(OModel model) {
        String authority = model.authority();
        matcher.addURI(authority, model.getModelName(), COLLECTION);
        matcher.addURI(authority, model.getModelName() + "/#", SINGLE_ROW);
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String order) {
        OModel model = getModel(getContext(), uri);
        setMatcher(model);
        SQLiteDatabase db = model.getWritableDatabase();
        Cursor cr = db.query(model.getTableName(), projection, selection, selectionArgs, null, null, order);
        notifyDataChange(uri);
        return cr;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        OModel model = getModel(getContext(), uri);
        setMatcher(model);
        SQLiteDatabase db = model.getWritableDatabase();
        if (!contentValues.containsKey("write_date")) {
            contentValues.put("write_date", ODateUtils.getUTCDateTime());
        }
        long new_id = db.insert(model.getTableName(), null, contentValues);
        notifyDataChange(uri);
        return Uri.withAppendedPath(uri, new_id + "");
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        OModel model = getModel(getContext(), uri);
        setMatcher(model);
        int count = model.delete(s, strings);
        notifyDataChange(uri);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String where, String[] args) {
        OModel model = getModel(getContext(), uri);
        setMatcher(model);
        int match = matcher.match(uri);
        int count = 0;
        if (!contentValues.containsKey("write_date")) {
            contentValues.put("write_date", ODateUtils.getUTCDateTime());
        }
        switch (match) {
            case COLLECTION:
                break;
            case SINGLE_ROW:
                SQLiteDatabase db = model.getWritableDatabase();
                int updateId = Integer.parseInt(uri.getLastPathSegment());
                where = "_id = ?";
                args = new String[]{updateId + ""};
                count = db.update(model.getTableName(), contentValues, where, args);
                break;
        }
        notifyDataChange(uri);
        return count;
    }

    private void notifyDataChange(Uri uri) {
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null);
    }
}
