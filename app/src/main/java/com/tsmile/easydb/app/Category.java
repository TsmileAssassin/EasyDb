package com.tsmile.easydb.app;


import com.tsmile.easydb.annotation.DbColumn;
import com.tsmile.easydb.annotation.DbPrimaryKey;
import com.tsmile.easydb.annotation.DbTable;

/**
 * Created by tsmile on 16/6/26.
 */
@DbTable(tableName = "category")
public class Category {
    @DbColumn
    @DbPrimaryKey(autoincrement = false)
    public int sid;

    @DbColumn
    public String name;
}
