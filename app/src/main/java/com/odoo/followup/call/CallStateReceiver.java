package com.odoo.followup.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;

public class CallStateReceiver extends BroadcastReceiver
        implements CallHandler.CallStatusListener {

    private String outgoingNumber, incomingNumber;

    @Override
    public void onReceive(Context context, Intent intent) {

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
        //TODO: Implement for filtering data with database
    }
}
