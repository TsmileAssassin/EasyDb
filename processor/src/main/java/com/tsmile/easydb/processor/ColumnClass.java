package com.tsmile.easydb.processor;

/**
 * Created by tsmile on 16/6/26.
 */
class ColumnClass {
    public static final int TYPE_INT = 1;
    public static final int TYPE_REAL = 2;
    public static final int TYPE_STRING = 3;

    String name;
    String fieldName;
    String defaultValue;

    boolean isNotNull;
    boolean isPrimaryKey;
    boolean isPrimaryAutoIncrement;

    int type;
    int typeLength;
}
