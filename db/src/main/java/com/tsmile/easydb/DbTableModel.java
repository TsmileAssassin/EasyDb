package com.tsmile.easydb;

import android.content.ContentValues;

import java.util.Set;

/**
 * 针对数据库表的操作，关联一个数据库
 * Created by tsmile on 15/9/25.
 */
public abstract class DbTableModel {
    protected final DbDataBase dbDataBase;

    public DbTableModel(DbDataBase dbDataBase) {
        this.dbDataBase = dbDataBase;
    }

    public static void constructWhere(ContentValues values,
                                      StringBuilder where, String[] whereArgs) {
        Set<String> keys = values.keySet();
        int index = 0;
        for (String key : keys) {
            if (index != 0) {
                where.append(" AND");
            }
            where.append(key).append(" = ?");
            whereArgs[index] = values.getAsString(key);
            index++;
        }
    }
}
