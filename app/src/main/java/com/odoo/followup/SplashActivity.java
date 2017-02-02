package com.odoo.followup;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.odoo.followup.auth.Authenticator;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
        final Account[] accounts = manager.getAccountsByType(Authenticator.AUTH_TYPE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (accounts.length > 0) {
                    redirectToHome();
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }
            }
        }, accounts.length > 0 ? 700 : 1000);
    }

    private void redirectToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
