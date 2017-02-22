package com.odoo.followup;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;

import com.odoo.core.support.CBind;
import com.odoo.core.support.OUser;
import com.odoo.core.support.OdooActivity;
import com.odoo.followup.utils.BitmapUtils;

public class UserProfile extends OdooActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        OUser user = OUser.current(this);

        CollapsingToolbarLayout collapsingToolbarLayout;
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_layout);

        collapsingToolbarLayout.setTitle(user.getName());
        CBind.setText(findViewById(R.id.userName), user.getName());
        CBind.setText(findViewById(R.id.userHost), user.getHost());
        CBind.setText(findViewById(R.id.database), user.getDatabase().toString());
        CBind.setText(findViewById(R.id.timezone), user.getTimezone());

        if (!user.getAvatar().equals("false")) {
            CBind.setImage(findViewById(R.id.user_image), BitmapUtils.getBitmapImage(this,
                    user.getAvatar()));
        } else
            CBind.setImage(findViewById(R.id.user_image), R.drawable.user_profile);

    }
}
