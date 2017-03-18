package com.tsmile.easydb.app;


import com.tsmile.easydb.annotation.DbColumn;
import com.tsmile.easydb.annotation.DbPrimaryKey;
import com.tsmile.easydb.annotation.DbTable;

/**
 * Created by tsmile on 16/6/26.
 */
@DbTable(tableName = "item")
public class Item {
    @DbColumn
    @DbPrimaryKey
    public int _id;

    @DbColumn
    public String name;

    @DbColumn(varcharLength = 1000)
    public String content;

    @DbColumn(notNull = true)
    public String text;

    @DbColumn
    public boolean isFav;

    @DbColumn
    public boolean isUseful;

    @DbColumn
    public String shareTitle;

    @DbColumn(name = "p1")
    public float percent;

    @DbColumn(name = "p2")
    public double percentD;

    @Override
    public String toString() {
        return "id:" + _id + ",name:" + name
                + ",content:" + content + ",text:" + text
                + ",isFav:" + isFav + ",isUseful:"
                + isUseful + ",shareTitle:" + shareTitle
                + ",percent:" + percent + ",percentD:" + percentD;
    }
}
