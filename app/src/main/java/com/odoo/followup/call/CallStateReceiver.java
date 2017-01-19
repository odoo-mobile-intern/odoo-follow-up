package com.odoo.followup.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import com.odoo.followup.orm.models.ResPartner;

public class CallStateReceiver extends BroadcastReceiver
        implements CallHandler.CallStatusListener {

    private String outgoingNumber, incomingNumber;
    private ResPartner partner;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;

        CallHandler callHandler = new CallHandler(context, this);
        String action = intent.getAction();
        Bundle data = intent.getExtras();
        if (action.equals("android.intent.action.NEW_OUTGOING_CALL")) {
            outgoingNumber = data.getString(Intent.EXTRA_PHONE_NUMBER);
            callHandler.setOutgoingNumber(outgoingNumber);
        }
        if (action.equals("android.intent.action.PHONE_STATE")) {
            String state = data.getString(TelephonyManager.EXTRA_STATE);
            if (outgoingNumber == null)
                incomingNumber = data.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            callHandler.setState(state);
            callHandler.setIncomingNumber(incomingNumber);
        }
        callHandler.checkStatus();
    }

    @Override
    public void callStatus(String number, CallHandler.CallStatus status) {
        partner = new ResPartner(mContext);
        String num = partner.getCallNumber(number);
        if (num != null) {
            createCallerWindow();
        }
    }

    private void createCallerWindow() {
    }
}
