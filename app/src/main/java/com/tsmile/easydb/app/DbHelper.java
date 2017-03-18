package com.tsmile.easydb.app;

import android.content.Context;
import android.support.annotation.NonNull;

import com.tsmile.easydb.DbDataBase;
import com.tsmile.easydb.DbTemplateTableModel;

/**
 * Created by tsmile on 2017/3/18.
 */

public class DbHelper {
    private static DbHelper sInstance = null;
    @NonNull
    public final DbTemplateTableModel<Item> itemDbTemplateTableModel;
    @NonNull
    public final DbTemplateTableModel<Category> categoryDbTemplateTableModel;

    private DbHelper(Context context) {
        DbDataBase stickerDb = new DbDataBase(context.getApplicationContext(), DbStore.ITEM_DB);
        try {
            stickerDb.init();
        } catch (Exception e) {
            //ignore
        }
        itemDbTemplateTableModel = new DbTemplateTableModel<>(DbStore.ITEM_TABLE,
                stickerDb, Item.class);
        categoryDbTemplateTableModel = new DbTemplateTableModel<>(DbStore.CATEGORY_TABLE,
                stickerDb, Category.class);
    }

    public static DbHelper getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DbHelper.class) {
                if (sInstance == null) {
                    sInstance = new DbHelper(context);
                }
            }
        }
        return sInstance;
    }
}