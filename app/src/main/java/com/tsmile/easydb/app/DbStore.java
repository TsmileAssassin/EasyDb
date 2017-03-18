package com.tsmile.easydb.app;

import com.tsmile.easydb.DbDefinition;
import com.tsmile.easydb.DbTableDefinition;
import com.tsmile.easydb.DbTableGenerator;
import com.tsmile.easydb.DbUpgradeDefinition;

/**
 * Created by tsmile on 16/6/26.
 */
public class DbStore {
    public static final String DB_NAME = "item.db";
    public static final int DB_VERSION = 3;

    static final DbTableDefinition ITEM_TABLE = DbTableGenerator.generate(Item.class);
    static final DbTableDefinition CATEGORY_TABLE = DbTableGenerator.generate(Category.class);

    static DbUpgradeDefinition DB_1_2 = new DbUpgradeDefinition.Builder().fromVersion(1).toVersion(2)
            .sql(new String[]{
                    "ALTER TABLE 'item' add 'shareTitle' VARCHAR(300)",
                    "ALTER TABLE 'item' add 'shareContent' VARCHAR(700)"
            }).build();

    static DbUpgradeDefinition DB_2_3 = new DbUpgradeDefinition.Builder().fromVersion(2).toVersion(3)
            .sql(new String[]{
                    CATEGORY_TABLE.creationSql()
            }).build();

    static final DbDefinition ITEM_DB =
            new DbDefinition.Builder().version(DB_VERSION).name(DB_NAME)
                    .table(ITEM_TABLE)
                    .table(CATEGORY_TABLE)
                    .upgrade(DB_1_2)
                    .upgrade(DB_2_3)
                    .build();
}
