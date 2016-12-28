package com.odoo.followup.orm.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.odoo.core.rpc.Odoo;
import com.odoo.core.rpc.handler.OdooVersionException;
import com.odoo.core.rpc.helper.ODomain;
import com.odoo.core.rpc.helper.OdooFields;
import com.odoo.core.rpc.helper.utils.gson.OdooRecord;
import com.odoo.core.rpc.helper.utils.gson.OdooResult;
import com.odoo.core.support.OUser;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;
import com.odoo.followup.orm.models.ResPartner;

import java.util.ArrayList;
import java.util.List;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String AUTHORITY = "com.odoo.followup.orm.sync";
    private OUser mUser;
    private Odoo odoo;
    private AccountManager accountManager;
    private Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s,
                              ContentProviderClient contentProviderClient, SyncResult syncResult) {
        mUser = getUser(account);
        try {
            odoo = Odoo.createWithUser(mContext, mUser);
            ResPartner partner = new ResPartner(mContext);
            List<Integer> totalRecords = createOrUpdateRecords(partner);
        } catch (OdooVersionException e) {
            e.printStackTrace();
        }
    }

    private List<Integer> createOrUpdateRecords(ResPartner partner) {
        List<Integer> recordIds = new ArrayList<>();
        OdooFields fields = new OdooFields();
        fields.addAll(partner.getServerColumns());
        ODomain domain = new ODomain();
        OdooResult result = odoo.searchRead(partner.getModelName(), fields, domain, 0, 0, null);
        for (OdooRecord record : result.getRecords()) {
            ContentValues values = new ContentValues();
            for (OColumn column : partner.getColumns()) {
                if (!column.isLocal) {
                    switch (column.columnType) {
                        case INTEGER:
                            values.put(column.name, record.getInt(column.name));
                            break;
                        case VARCHAR:
                            values.put(column.name, record.getString(column.name));
                            break;
                        case BLOB:
                            values.put(column.name, record.getString(column.name));
                            break;
                        case BOOLEAN:
                            values.put(column.name, record.getBoolean(column.name));
                            break;
                        case DATETIME:
                            values.put(column.name, record.getString(column.name));
                            break;
                        case MANY2ONE:
                            OdooRecord odooRecord = record.getM20(column.name);
                            int m2oRecords = 0;
                            if (odooRecord != null) {
                                String modelName = column.relModel;
                                OModel model = OModel.createInstance(modelName, mContext);
                                if (model != null) {
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put("id", odooRecord.getInt("id"));
                                    contentValues.put("name", odooRecord.getString("name"));
                                    m2oRecords = model.updateOrCreate(contentValues, "id = ?",
                                            odooRecord.getInt("id") + "");
                                }
                            }
                            values.put(column.name, m2oRecords);
                            break;
                    }
                }
            }
            int recordId = partner.updateOrCreate(values, "id = ? ", record.getInt("id") + "");
            recordIds.add(recordId);
        }
        return recordIds;
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
