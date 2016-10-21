package com.odoo.followup.orm.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class SyncService extends Service {
    public static final String TAG = SyncService.class.getSimpleName();
    private static final Object mAdapterLock = new Object();
    private SyncAdapter mSyncAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (mAdapterLock) {
            if (mSyncAdapter == null) {
                mSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mSyncAdapter.getSyncAdapterBinder();
    }
}
