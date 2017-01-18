package com.odoo.followup.call;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class CallHandler {

    private SharedPreferences preferences;
    private CallStatusListener mCallStatusListener;

    public enum CallStatus {
        Incoming, Outgoing, MissedCall, Rejected
    }

    public CallHandler(Context context, CallStatusListener listener) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        mCallStatusListener = listener;
        if (!getCallProcess()) {
            setState(null);
            setIncomingNumber(null);
            setOutgoingNumber(null);
            setStateOffhook(false);
            setCallProcess(true);
        }
    }

    private void setCallProcess(boolean status) {
        preferences.edit().putBoolean("is_call_in_process", status).apply();
    }

    private boolean getCallProcess() {
        return preferences.getBoolean("is_call_in_process", false);
    }

    public void setState(String state) {
        preferences.edit().putString("key_call_state", state).apply();
        if (state != null && state.equals(TelephonyManager.EXTRA_STATE_RINGING)
                && getIncomingNumber() != null)
            setState(null);

        if (state != null && state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            setStateOffhook(true);
        }
    }

    public void setStateOffhook(boolean status) {
        preferences.edit().putBoolean("key_call_offhooked", status).apply();
    }

    public boolean getStateOffhooked() {
        return preferences.getBoolean("key_call_offhooked", false);
    }

    public void setIncomingNumber(String incomingNumber) {
        preferences.edit().putString("key_in_number", incomingNumber).apply();
    }

    public String getIncomingNumber() {
        return preferences.getString("key_in_number", null);
    }

    public void setOutgoingNumber(String outgoingNumber) {
        preferences.edit().putString("key_out_number", outgoingNumber).apply();
        setState(null);
    }

    public String getOutgoingNumber() {
        return preferences.getString("key_out_number", null);
    }

    public String getState() {
        return preferences.getString("key_call_state", null);
    }

    public void checkStatus() {
        if (mCallStatusListener == null || getState() == null)
            return;
        if (getState().equals(TelephonyManager.EXTRA_STATE_RINGING)
                && getIncomingNumber() != null) {
            mCallStatusListener.callStatus(getIncomingNumber(), CallStatus.Incoming);
            return;
        }

        if (getState().equals(TelephonyManager.EXTRA_STATE_IDLE)
                && (getIncomingNumber() != null || getOutgoingNumber() != null)) {

            if (!getStateOffhooked() && getIncomingNumber() != null) {
                mCallStatusListener.callStatus(getIncomingNumber(), CallStatus.MissedCall);
            } else {
                mCallStatusListener.callStatus(getIncomingNumber() != null ?
                                getIncomingNumber() : getOutgoingNumber(),
                        CallStatus.Rejected);
            }
            setCallProcess(false);
            return;
        }
        if (getState().equals(TelephonyManager.EXTRA_STATE_OFFHOOK)
                && getOutgoingNumber() != null && getStateOffhooked()) {
            mCallStatusListener.callStatus(getOutgoingNumber(), CallStatus.Outgoing);
        }
    }

    interface CallStatusListener {

        void callStatus(String number, CallStatus status);
    }
}
