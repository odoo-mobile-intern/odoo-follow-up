package com.odoo.widget.kanban;


import com.odoo.followup.orm.data.ListRow;

public class CardItem {
    public String title;
    public ListRow data;

    public CardItem(String title, ListRow data) {
        this.title = title;
        this.data = data;
    }
}