package com.odoo.followup.models;

import android.content.Context;

import com.odoo.followup.orm.OModel;

import java.util.HashMap;

public class ModelRegistry {

    public HashMap<String, OModel> models(Context context) {
        HashMap<String, OModel> model = new HashMap<>();
        model.put("res.partner", new ResPartner(context));
        return model;
    }

}
