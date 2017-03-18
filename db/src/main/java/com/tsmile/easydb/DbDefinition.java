package com.tsmile.easydb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库定义信息，包括表定义，版本，数据库升级信息
 * Created by tsmile on 15/9/16.
 */
public class DbDefinition {
    public int version;
    public String name;
    boolean isUpgradeReCreate;
    public List<DbTableDefinition> dbTableDefinitions;
    Map<Integer, DbUpgradeDefinition> dbUpgradeDefinitions;

    private DbDefinition(final Builder builder) {
        version = builder.version;
        name = builder.name;
        dbTableDefinitions = builder.dbTableDefinitions;
        isUpgradeReCreate = builder.isUpgradeReCreate;
        if (isUpgradeReCreate) {
            dbUpgradeDefinitions = new HashMap<>();
        } else {
            dbUpgradeDefinitions = builder.dbUpgradeDefinitions;
        }
    }

    List<String> getUpgradeSqls(int oldVersion) {
        ArrayList<String> sqls = new ArrayList<>();
        if (isUpgradeReCreate) {
            sqls.addAll(getDropSqls());
            sqls.addAll(getCreationSqls());
        } else {
            for (int i = oldVersion; i < version; i++) {
                sqls.addAll(dbUpgradeDefinitions.get(i).upgradeSqls);
            }
        }
        return sqls;
    }

    List<String> getCreationSqls() {
        ArrayList<String> sqls = new ArrayList<>();
        for (DbTableDefinition dbTableDefinition : dbTableDefinitions) {
            sqls.add(dbTableDefinition.creationSql());
        }
        return sqls;
    }

    List<String> getDropSqls() {
        ArrayList<String> sqls = new ArrayList<>();
        for (DbTableDefinition dbTableDefinition : dbTableDefinitions) {
            sqls.add(dbTableDefinition.dropSql());
        }
        return sqls;
    }

    public static class Builder {
        private int version;
        private String name;
        private boolean isUpgradeReCreate;
        private List<DbTableDefinition> dbTableDefinitions;
        private Map<Integer, DbUpgradeDefinition> dbUpgradeDefinitions;

        public Builder() {
            dbTableDefinitions = new ArrayList<>();
            dbUpgradeDefinitions = new HashMap<>();
        }

        public Builder version(int version) {
            this.version = version;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder table(DbTableDefinition dbTableDefinition) {
            dbTableDefinitions.add(dbTableDefinition);
            return this;
        }

        public Builder upgrade(DbUpgradeDefinition dbUpgradeDefinition) {
            if (dbUpgradeDefinition.toVersion
                    - dbUpgradeDefinition.fromVersion != 1) {
                throw new IllegalArgumentException("upgrade should like from n to n+1");
            }
            if (dbUpgradeDefinitions.get(dbUpgradeDefinition.fromVersion) != null) {
                throw new IllegalArgumentException("already define fromVersion"
                        + dbUpgradeDefinition.fromVersion);
            }
            dbUpgradeDefinitions.put(dbUpgradeDefinition.fromVersion, dbUpgradeDefinition);
            return this;
        }

        public Builder setUpgradeReCreate() {
            isUpgradeReCreate = true;
            return this;
        }

        public DbDefinition build() {
            return new DbDefinition(this);
        }
    }
}