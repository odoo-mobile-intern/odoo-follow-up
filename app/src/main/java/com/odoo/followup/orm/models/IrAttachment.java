package com.odoo.followup.orm.models;

import android.content.Context;

import com.odoo.followup.orm.ColumnType;
import com.odoo.followup.orm.OColumn;
import com.odoo.followup.orm.OModel;
import com.odoo.followup.orm.data.ListRow;

import java.util.List;

public class IrAttachment extends OModel {

    OColumn name = new OColumn("Name", ColumnType.VARCHAR);
    OColumn datas = new OColumn("File Content", ColumnType.BLOB);
    OColumn mimetype = new OColumn("MIME Type", ColumnType.VARCHAR);
    OColumn res_model = new OColumn("Ref Model", ColumnType.VARCHAR);
    OColumn res_id = new OColumn("Record Id", ColumnType.INTEGER);
    OColumn file_size = new OColumn("File Size", ColumnType.INTEGER);

    public IrAttachment(Context context) {
        super(context, "ir.attachment");
    }

    public List<ListRow> getRecordAttachments(int record_id, String model_name) {
        return select("res_model = ? and res_id = ?", new String[]{model_name, record_id + ""});

    }

    public int getRecordAttachmentsCount(int record_id, String model_name) {
        return count("res_model = ? and res_id = ?", new String[]{model_name, record_id + ""});
    }

}
