package com.odoo.followup.orm.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.odoo.core.rpc.Odoo;
import com.odoo.core.rpc.handler.OdooVersionException;
import com.odoo.core.rpc.helper.ODomain;
import com.odoo.core.rpc.helper.OdooFields;
import com.odoo.core.rpc.helper.utils.gson.OdooRecord;
import com.odoo.core.rpc.helper.utils.gson.OdooResult;
import com.odoo.core.support.OUser;
import com.odoo.followup.orm.OModel;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = SyncAdapter.class.getSimpleName();
    private OUser mUser;
    private Odoo odoo;
    private AccountManager accountManager;
    private Context mContext;
    private int offset = 0;
    private int limit = 80;
    private OModel syncModel;

    public SyncAdapter(Context context, boolean autoInitialize, OModel syncModel) {
        super(context, autoInitialize);
        mContext = context;
        accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        this.syncModel = syncModel;
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String authority,
                              ContentProviderClient contentProviderClient, SyncResult syncResult) {
        mUser = getUser(account);
        try {
            odoo = Odoo.createWithUser(mContext, mUser);
            if (authority.equals("com.odoo.followup.appdata.sync")) {
                // Sync app data with multiple models
                // fixme
                syncAppData();
            } else {
                // fixme
                // Sync with single model sync
                if (syncModel != null) {
                    syncData(syncModel, syncResult);
                } else {
                    Log.e(TAG, "No model specified for sync service :" + authority);
                }
            }
        } catch (OdooVersionException e) {
            e.printStackTrace();
        }
    }

    private void syncAppData() {
        //todo
        Log.e(TAG, "App data not synced. Configuration missing");
        /*
            Base models:
               - ir.model.data
               - res.groups
               - ir.model.model
               - ir.model.access
         */
    }

    private void syncData(OModel model, SyncResult syncResult) {
        Log.e(">>", "Sync started for :" + model.getModelName());

        // Step 1: read all data from server
        //      - add domain filters
        //      - set limit and offsets
        //      - set sorting column

        OdooFields fields = new OdooFields(model.getServerColumns());
        ODomain domain = new ODomain();

        // todo: add domain filters
        // create date
        // write_date based on last sync datetime

        OdooResult result = odoo.searchRead(model.getModelName(), fields, domain, offset, limit,
                "create_date DESC");

        if (result.containsKey("error")) {
            Log.e(TAG, result.get("error") + "");
            return;
        }
        OdooRecordUtils recordUtils = OdooRecordUtils.getInstance(model);
        for (OdooRecord record : result.getRecords()) {
            recordUtils.processRecord(record);
        }

        // batch insert
        model.batchInsert(recordUtils.getRecordValuesToInsert());

        // batch update
        model.batchUpdate(recordUtils.getRecordValuesToUpdate());

        // relation record sync

    }

    private OUser getUser(Account account) {
        OUser user = new OUser();
        user.setHost(accountManager.getUserData(account, "host"));
        user.setUsername(accountManager.getUserData(account, "username"));
        user.setDatabase(accountManager.getUserData(account, "database"));
        user.setSession_id(accountManager.getUserData(account, "session_id"));
        return user;
    }
}
