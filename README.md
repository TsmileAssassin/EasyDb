# EasyDb
Make Android app's SQLite database more easy to use.Through Apt and entity relationship mapping, simplifying the use of android sqlite database.
## Useage

```java
// declare table
@DbTable(tableName = "category")
public class Category {
    @DbColumn
    @DbPrimaryKey(autoincrement = false)
    public int sid;

    @DbColumn
    public String name;
}

// declare database
public class DbStore {
    public static final String DB_NAME = "item.db";
    public static final int DB_VERSION = 3;

    static final DbTableDefinition ITEM_TABLE = DbTableGenerator.generate(Item.class);
    static final DbTableDefinition CATEGORY_TABLE = DbTableGenerator.generate(Category.class);

    // declare upgrade db sql
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

// use to insert, get
DbDataBase stickerDb = new DbDataBase(this, DbStore.ITEM_DB);
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

// insert
dbTemplateTableModel.insert(item1);

TextView textView = (TextView) findViewById(R.id.text);
// get all
for (Item item : dbTemplateTableModel.getAll()) {
  textView.setText(item.toString());
}

```

```
// add the plugin to your buildscript:
buildscript {
    ...
    dependencies {
        ...
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
        ...
    }
}
 
// apply it in your module:
apply plugin: 'com.neenbedankt.android-apt'

// add dependencies
compile 'com.tsmile.easydb:db:1.0.0'    
compile 'com.tsmile.easydb:db-annotation:1.0.0'
apt 'com.tsmile.easydb:db-compiler:1.0.0'
    
    
```



