package com.odoo.followup.orm.sync;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.odoo.followup.orm.OModel;

public class SyncService extends Service {
    public static final String TAG = SyncService.class.getSimpleName();
    private static final Object mAdapterLock = new Object();
    private SyncAdapter mSyncAdapter;

    public OModel getModel(Context context) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (mAdapterLock) {
            if (mSyncAdapter == null) {
                mSyncAdapter = new SyncAdapter(getApplicationContext(), true, getModel(getApplicationContext()));
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mSyncAdapter.getSyncAdapterBinder();
    }
}
