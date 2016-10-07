/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 30/9/16 2:58 PM
 */
package com.odoo.followup.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.odoo.followup.models.ResPartner;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;

import java.util.ArrayList;
import java.util.List;

import odoo.Odoo;
import odoo.handler.OdooVersionException;
import odoo.helper.ODomain;
import odoo.helper.OUser;
import odoo.helper.OdooFields;
import odoo.helper.utils.gson.OdooRecord;
import odoo.helper.utils.gson.OdooResult;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String AUTHORITY = "com.odoo.followup.sync";
    private OUser mUser;
    private Odoo odoo;
    private AccountManager accountManager;
    private Context mContext;


    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        accountManager = (AccountManager) context.getSystemService(context.ACCOUNT_SERVICE);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s,
                              ContentProviderClient contentProviderClient, SyncResult syncResult) {

        mUser = getUser(account);

        try {
            odoo = Odoo.createQuickInstance(mContext, mUser.getHost());
            odoo.authenticate(mUser.getUsername(), mUser.getPassword(), mUser.getDatabase());

            ResPartner partner = new ResPartner(mContext);

            List<Integer> totalRecords = createOrUpdateRecords(partner);
            Log.e(">>>>total records>>>", totalRecords.size() + "");

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
        user.setPassword(accountManager.getPassword(account));
        user.setDatabase(accountManager.getUserData(account, "database"));
        return user;
    }
}
