package com.odoo.followup.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class AuthenticatorService extends Service {
    public static final String TAG = AuthenticatorService.class.getSimpleName();

    private Authenticator mAuthenticator;
    private static final Object mAuthLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (mAuthLock) {
            if (mAuthenticator == null) {
                mAuthenticator = new Authenticator(getApplicationContext());
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
