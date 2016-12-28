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

import com.odoo.followup.orm.OModel;

public class BaseContentProvider extends ContentProvider {
    public static final String TAG = BaseContentProvider.class.getSimpleName();
    private final int COLLECTION = 1;
    private final int SINGLE_ROW = 2;
    public UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    public OModel getModel(Context context) {
        return null;
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
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        OModel model = getModel(getContext());
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        OModel model = getModel(getContext());
        SQLiteDatabase db = model.getWritableDatabase();
        long new_id = db.insert(model.getTableName(), null, contentValues);
        db.close();
        return Uri.withAppendedPath(uri, new_id + "");
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        OModel model = getModel(getContext());
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String where, String[] args) {
        OModel model = getModel(getContext());
        setMatcher(model);
        int match = matcher.match(uri);
        int count = 0;
        SQLiteDatabase db = null;
        switch (match) {
            case COLLECTION:
                break;
            case SINGLE_ROW:
                db = model.getWritableDatabase();
                int updateId = Integer.parseInt(uri.getLastPathSegment());
                where = "_id = ?";
                args = new String[]{updateId + ""};
                count = db.update(model.getTableName(), contentValues, where, args);
                break;
        }

        if (db != null) db.close();

        return count;
    }
}
