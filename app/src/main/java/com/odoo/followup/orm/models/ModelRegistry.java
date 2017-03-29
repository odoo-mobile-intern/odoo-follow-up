package com.odoo.followup.orm.models;

import android.content.Context;

import com.odoo.followup.addons.customers.models.ResPartner;
import com.odoo.followup.addons.dashboard.models.UserPerformance;
import com.odoo.followup.addons.mail.models.MailMessage;
import com.odoo.followup.addons.meetings.models.CalendarEvent;
import com.odoo.followup.addons.sales.models.CRMActivity;
import com.odoo.followup.addons.sales.models.CRMLead;
import com.odoo.followup.addons.sales.models.CRMTeam;
import com.odoo.followup.addons.sales.models.ProductTemplate;
import com.odoo.followup.orm.OModel;

import java.util.HashMap;

public class ModelRegistry {

    public HashMap<String, OModel> models(Context context) {
        HashMap<String, OModel> model = new HashMap<>();
        model.put("ir.model", new IrModel(context));
        model.put("local.record.state", new LocalRecordState(context));
        model.put("res.partner", new ResPartner(context));
        model.put("res.country.state", new ResState(context));
        model.put("res.country", new ResCountry(context));
        model.put("crm.lead", new CRMLead(context));
        model.put("product.template", new ProductTemplate(context));
        model.put("mail.message", new MailMessage(context));
        model.put("calendar.event", new CalendarEvent(context));
        model.put("crm.activity", new CRMActivity(context));
        model.put("ir.attachment", new IrAttachment(context));
        model.put("crm.team", new CRMTeam(context));
        model.put("user.performance", new UserPerformance(context));
        return model;
    }

    public static OModel getModel(Context context, String model) {
        return new ModelRegistry().models(context).get(model);
    }
}
