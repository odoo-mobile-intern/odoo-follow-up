package com.odoo.followup.call;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.odoo.followup.HomeActivity;
import com.odoo.followup.R;
import com.odoo.followup.orm.data.ListRow;
import com.odoo.followup.orm.models.ResPartner;
import com.odoo.followup.utils.BitmapUtils;

import java.util.Locale;

public class CallStateReceiver extends BroadcastReceiver
        implements CallHandler.CallStatusListener {

    private String outgoingNumber, incomingNumber;
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
        Log.v("callStatus()", "call for : " + number + " : " + status);
        ResPartner partner = new ResPartner(mContext);
        ListRow callDetail = partner.getCallDetails(number);
        if (status == CallHandler.CallStatus.Rejected ||
                status == CallHandler.CallStatus.MissedCall) {
            CallerWindow.remove();
            switch (status) {
                case MissedCall:
                    if (callDetail != null) {
                        showMissedCall(number, callDetail);
                    }
                    break;
                case Rejected:
                    if (callDetail != null) {
                        //FIXME: Replace activity with schedule call activity
                        Log.e(">>", "Open auto schedule call activity");
                    }
                    break;
            }
        } else {
            if (callDetail != null) {
                CallerWindow.show(mContext, number, callDetail);
            }
        }
    }

    private void showMissedCall(String number, ListRow detail) {
        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder.setContentTitle(mContext.getString(R.string.title_missed_call_from_customer));
        builder.setContentText(String.format(Locale.getDefault(), "%s recently called you.", detail.getString("name")));
        builder.setSmallIcon(R.drawable.ic_action_phone_missed);
        if (!detail.getString("image_medium").equals("false"))
            builder.setLargeIcon(BitmapUtils.getBitmapImage(mContext, detail.getString("image_medium")));
        else {
            BitmapDrawable drawable = (BitmapDrawable) ContextCompat.getDrawable(mContext, R.drawable.user_profile);
            builder.setLargeIcon(drawable.getBitmap());
        }
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_ALL);

        // adding actions callback and schedule
        // call action
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number));
        PendingIntent callIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        builder.addAction(R.drawable.ic_action_call, mContext.getString(R.string.label_callback),
                callIntent);

        // schedule action
        //FIXME: Replace activity with schedule call activity
        Intent scheduleIntent = new Intent(mContext, HomeActivity.class);
        PendingIntent schedulePendingIntent = PendingIntent.getActivity(mContext, 0, scheduleIntent, 0);
        builder.addAction(R.drawable.ic_action_schedule, mContext.getString(R.string.label_schedule),
                schedulePendingIntent);

        manager.notify(detail.getInt("id"), builder.build());
    }
}
