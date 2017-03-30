package com.odoo.followup.orm.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.odoo.followup.auth.Authenticator;
import com.odoo.followup.orm.OModel;

public class OSyncUtils {


    private Context context;
    private OModel model;

    private OSyncUtils(Context context, OModel model) {
        this.context = context;
        this.model = model;
    }

    public static OSyncUtils get(Context context, OModel model) {
        return new OSyncUtils(context, model);
    }

    public static void onSyncFinishListener(AppCompatActivity activity, final OnSyncFinishListener callback) {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(activity);
        manager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getExtras().containsKey(SyncAdapter.KEY_SYNC_MODEL) && callback != null) {
                    callback.onSyncFinish(intent.getExtras().getString(SyncAdapter.KEY_SYNC_MODEL));
                }
            }
        }, new IntentFilter(SyncAdapter.ACTION_SYNC_FINISH));
    }

    public void sync(Bundle data) {
        AccountManager manager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account[] accounts = manager.getAccountsByType(Authenticator.AUTH_TYPE);
        if (accounts.length > 0) {
            Bundle settings = new Bundle();
            settings.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            settings.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            settings.putString(SyncAdapter.KEY_SYNC_MODEL, model.getModelName());
            if (data != null)
                settings.putAll(data);
            ContentResolver.requestSync(accounts[0], model.authority(), settings);
        }
    }

    public interface OnSyncFinishListener {
        void onSyncFinish(String model);
    }
}
