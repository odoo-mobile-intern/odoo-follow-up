package com.odoo.followup.orm.models;

import android.content.Context;

import com.odoo.followup.orm.OModel;

import java.util.HashMap;

public class ModelRegistry {

    public HashMap<String, OModel> models(Context context) {
        HashMap<String, OModel> model = new HashMap<>();
        model.put("res.partner", new ResPartner(context));
        model.put("res.state", new ResState(context));
        model.put("res.country", new ResCountry(context));
        model.put("crm.lead", new CRMLead(context));
        return model;
    }
}
