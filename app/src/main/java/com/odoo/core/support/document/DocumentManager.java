package com.odoo.core.support.document;

import com.odoo.core.support.OdooActivity;

public class DocumentManager {

    private OdooActivity mOdooActivity;

    private DocumentManager(OdooActivity activity) {
        mOdooActivity = activity;
    }

    public static DocumentManager getInstance(OdooActivity activity) {
        return new DocumentManager(activity);
    }

}
