package com.tsmile.easydb.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.tsmile.easydb.DbTemplateTableModel;


public class MainActivity extends AppCompatActivity {

    DbTemplateTableModel<Item> dbTemplateTableModel;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Item item1 = new Item();
        item1.name = "hahha1";
        item1.content = "content1";
        item1.text = "text1";
        item1.isFav = true;
        item1.percent = 0.3f;
        item1.percentD = 2.7d;

        DbHelper.getInstance(this).itemDbTemplateTableModel.insert(item1);

        TextView textView = (TextView) findViewById(R.id.text);
        for (Item item : DbHelper.getInstance(this).itemDbTemplateTableModel.getAll()) {
            textView.setText(item.toString());
        }
    }
}
