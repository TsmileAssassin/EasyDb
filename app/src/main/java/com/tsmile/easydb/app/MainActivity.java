package com.tsmile.easydb.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.tsmile.easydb.DbDataBase;
import com.tsmile.easydb.DbTemplateTableModel;


public class MainActivity extends AppCompatActivity {

    DbTemplateTableModel<Item> dbTemplateTableModel;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DbDataBase stickerDb = new DbDataBase(this.getApplicationContext(), DbStore.ITEM_DB);
        try {
            stickerDb.init();
        } catch (Exception e) {
            //ignore
        }
        dbTemplateTableModel = new DbTemplateTableModel<>(DbStore.ITEM_TABLE,
                stickerDb, Item.class);
        Item item1 = new Item();
        item1.name = "hahha1";
        item1.content = "content1";
        item1.text = "text1";
        item1.isFav = true;
        item1.percent = 0.3f;
        item1.percentD = 2.7d;

        dbTemplateTableModel.insert(item1);

        TextView textView = (TextView) findViewById(R.id.text);
        for (Item item : dbTemplateTableModel.getAll()) {
            textView.setText(item.toString());
        }
    }
}
