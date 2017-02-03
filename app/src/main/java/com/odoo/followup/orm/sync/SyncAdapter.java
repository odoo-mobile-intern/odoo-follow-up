package com.odoo.followup.orm.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.odoo.core.rpc.Odoo;
import com.odoo.core.rpc.handler.OdooVersionException;
import com.odoo.core.rpc.helper.ODomain;
import com.odoo.core.rpc.helper.OdooFields;
import com.odoo.core.rpc.helper.utils.gson.OdooRecord;
import com.odoo.core.rpc.helper.utils.gson.OdooResult;
import com.odoo.core.support.OUser;
import com.odoo.followup.orm.OModel;
import com.odoo.followup.orm.models.LocalRecordState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = SyncAdapter.class.getSimpleName();
    private OUser mUser;
    private Odoo odoo;
    private AccountManager accountManager;
    private Context mContext;
    private int offset = 0;
    private int limit = 80;
    private OModel syncModel;
    private HashMap<String, HashSet<Integer>> relationRecordsSyncFinished = new HashMap<>();

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
                if (syncModel != null) {
                    Log.v(TAG, "Sync started for " + syncModel.getModelName());
                    syncData(syncModel, null, syncResult);

                    // Sync finished
                    syncModel.updateLastSyncDate();
                    if (syncResult != null) {
                        if (syncResult.stats.numInserts > 0)
                            Log.v(TAG, "Inserted " + syncResult.stats.numInserts + " record(s).");
                        if (syncResult.stats.numUpdates > 0)
                            Log.v(TAG, "Updated " + syncResult.stats.numUpdates + " record(s).");
                        if (syncResult.stats.numDeletes > 0)
                            Log.v(TAG, "Deleted " + syncResult.stats.numDeletes + " record(s) from local.");
                        if (syncResult.stats.numSkippedEntries > 0)
                            Log.v(TAG, "Deleted " + syncResult.stats.numSkippedEntries + " record(s) from server.");

                        Log.v(TAG, "Sync finished for " + syncModel.getModelName());
                    }
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

    private void syncData(OModel model, ODomain syncDomain, SyncResult syncResult) {
        // Step 1: read all data from server
        //      - add domain filters
        //      - set limit and offsets
        //      - set sorting column

        OdooFields fields = new OdooFields(model.getServerColumns());
        ODomain domain = new ODomain();

        if (syncDomain != null) {
            domain.append(syncDomain);
        } else {
            domain.append(model.syncDomain());
            // create date
            if (model.getLastSyncDate() != null) {
                domain.add("write_date", ">", model.getLastSyncDate());
            }
        }
        OdooResult result = odoo.searchRead(model.getModelName(), fields, domain, offset, limit,
                "create_date DESC");
        if (result == null) {
            Log.e(TAG, "FATAL : Request aborted.");
            return;
        }
        if (result.containsKey("error")) {
            Log.e(TAG, result.get("error") + "");
            return;
        }
        OdooRecordUtils recordUtils = OdooRecordUtils.getInstance(model, syncResult);
        if (syncResult != null)
            Log.v(TAG, "Processing " + result.getTotalRecords() + " record(s) for model " + model.getModelName());
        HashSet<Integer> recentSyncIds = new HashSet<>();
        for (OdooRecord record : result.getRecords()) {
            if (canUpdateOrInsert(model, record)) {
                recordUtils.processRecord(record);
            }
            recentSyncIds.add(record.getDouble("id").intValue());
        }

        // batch insert
        model.batchInsert(recordUtils.getRecordValuesToInsert());
        if (syncResult != null)
            syncResult.stats.numInserts += recordUtils.getRecordValuesToInsert().size();

        // batch update
        model.batchUpdate(recordUtils.getRecordValuesToUpdate());
        if (syncResult != null)
            syncResult.stats.numUpdates += recordUtils.getRecordValuesToUpdate().size();

        // creating list for relation records to sync
        if (recordUtils.getRelationRecordToSync().size() > 0) {
            List<String> relModels = new ArrayList<>(recordUtils.getRelationRecordToSync().keySet());
            for (String relModel : relModels) {
                OModel relModelObj = model.createModel(relModel);
                HashSet<Integer> relModelIds = recordUtils.getRelationRecordToSync().get(relModel);
                if (relationRecordsSyncFinished.containsKey(relModel)) {
                    HashSet<Integer> idsDone = relationRecordsSyncFinished.get(relModel);
                    relModelIds.removeAll(idsDone);
                }
                if (!relModelIds.isEmpty()) {
                    addRelationRecordSynced(relModel, relModelIds);
                    Log.v(TAG, "Processing relation " + relModelIds.size() + " record(s) for " + relModel
                            + (syncResult == null ? " of " + model.getModelName() : ""));
                    ODomain relDomain = new ODomain();
                    relDomain.add("id", "in", new ArrayList<>(relModelIds));
                    syncData(relModelObj, relDomain, null);
                }
            }
        }

        // Remove local records
        HashSet<Integer> localServerIds = new HashSet<>(model.getServerIds());
        localServerIds.removeAll(recentSyncIds);
        if (!localServerIds.isEmpty()) {
            deleteFromLocal(model, localServerIds, syncResult);
        }

        // removing record from server
        deleteFromServer(model, syncResult);
    }

    private void deleteFromLocal(OModel model, HashSet<Integer> checkIds, SyncResult syncResult) {
        ODomain domain = new ODomain();
        domain.add("id", "in", new ArrayList<>(checkIds));
        OdooResult result = odoo.searchRead(model.getModelName(), new OdooFields("id"), domain, 0, 0, null);
        if (result == null) {
            Log.e(TAG, "FATAL : Request aborted.");
            return;
        }
        if (result.containsKey("error")) {
            Log.e(TAG, result.get("error") + "");
            return;
        }
        HashSet<Integer> serverIds = new HashSet<>();
        for (OdooRecord record : result.getRecords()) {
            serverIds.add(record.getDouble("id").intValue());
        }
        checkIds.removeAll(serverIds);
        int deleted = model.delete("id in (" + TextUtils.join(", ", checkIds) + ")");
        if (syncResult != null) syncResult.stats.numDeletes += deleted;
    }

    private void deleteFromServer(OModel model, SyncResult syncResult) {
        LocalRecordState recordState = new LocalRecordState(mContext);
        List<Integer> ids = recordState.getServerIds(model.getModelName());
        if (!ids.isEmpty()) {
            OdooResult result = odoo.unlinkRecord(model.getModelName(), ids);
            if (result == null) {
                Log.e(TAG, "FATAL : Request aborted.");
                return;
            }
            if (result.containsKey("error")) {
                Log.e(TAG, result.get("error") + "");
                return;
            }
            if (result.getBoolean("result")) {
                syncResult.stats.numSkippedEntries += ids.size();
                recordState.delete("server_id in (" + TextUtils.join(", ", ids) + ") and model = ?", model.getModelName());
            }
        }
    }

    private boolean canUpdateOrInsert(OModel model, OdooRecord record) {
        LocalRecordState recordState = new LocalRecordState(mContext);
        int server_id = record.getDouble("id").intValue();
        return recordState.isValid(model.getModelName(), server_id,
                record.getString("write_date"));
    }

    private void addRelationRecordSynced(String model, HashSet<Integer> ids) {
        HashSet<Integer> recordIds = new HashSet<>();
        if (relationRecordsSyncFinished.containsKey(model)) {
            recordIds.addAll(relationRecordsSyncFinished.get(model));
        }
        recordIds.addAll(ids);
        relationRecordsSyncFinished.put(model, recordIds);
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
