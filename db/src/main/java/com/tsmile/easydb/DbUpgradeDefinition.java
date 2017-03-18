package com.tsmile.easydb;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库升级信息定义
 * Created by tsmile on 15/9/16.
 */
public class DbUpgradeDefinition {
    int fromVersion;
    int toVersion;
    List<String> upgradeSqls;

    private DbUpgradeDefinition(final Builder builder) {
        fromVersion = builder.fromVersion;
        toVersion = builder.toVersion;
        upgradeSqls = builder.upgradeSqls;
    }

    public static class Builder {
        private int fromVersion;
        private int toVersion;
        private List<String> upgradeSqls;

        public Builder() {
        }

        public Builder fromVersion(int fromVersion) {
            this.fromVersion = fromVersion;
            return this;
        }

        public Builder toVersion(int toVersion) {
            this.toVersion = toVersion;
            return this;
        }

        public Builder sql(String[] upgradeSqls) {
            ArrayList<String> listSqls = new ArrayList<>();
            for (String sql : upgradeSqls) {
                listSqls.add(sql);
            }
            this.upgradeSqls = listSqls;
            return this;
        }

        public Builder sql(String upgradeSql) {
            this.upgradeSqls = DbUtils.splitSqlScript(upgradeSql, ';');
            return this;
        }

        public DbUpgradeDefinition build() {
            return new DbUpgradeDefinition(this);
        }
    }
}