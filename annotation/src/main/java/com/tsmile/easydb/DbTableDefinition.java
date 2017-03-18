package com.tsmile.easydb;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库表定义
 * Created by tsmile on 15/9/16.
 */
public class DbTableDefinition {

    public String tableName;
    public List<Column> columns;
    public List<Column> primaryColumns;
    public Column autoIncrementColumn;
    public List<Column> withoutAutoIncrementColumns;

    private DbTableDefinition(final Builder builder) {
        tableName = builder.tableName;
        primaryColumns = builder.primaryColumns;
        autoIncrementColumn = builder.autoIncrementColumn;
        withoutAutoIncrementColumns = builder.withoutAutoIncrementColumns;
        columns = builder.columns;
    }

    public List<Column> getColumnsWithName(String name) {
        List<Column> customColumns = new ArrayList<>();
        for (Column column : columns) {
            if (name.equals(column.columnName)) {
                customColumns.add(column);
                break;
            }
        }
        return customColumns;
    }

    public String creationSql() {
        StringBuilder create = new StringBuilder("CREATE TABLE ");
        create.append(tableName).append('(');
        boolean first = true;
        for (Column column : columns) {
            if (!first) {
                create.append(',');
            }
            first = false;
            create.append(column.columnName).append(' ').append(column.columnConstraint);
        }
        create.append(')');
        return create.toString();
    }

    public String dropSql() {
        return "DROP TABLE IF EXISTS " + tableName;
    }

    public static class Builder {

        private String tableName;
        private List<Column> columns;
        private List<Column> primaryColumns;
        private Column autoIncrementColumn;
        private List<Column> withoutAutoIncrementColumns;

        public Builder() {
            columns = new ArrayList<>();
            primaryColumns = new ArrayList<>();
            withoutAutoIncrementColumns = new ArrayList<>();
        }

        public Builder tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        private Builder columnPrimaryAutoIncrement(String name, String constraint, String fieldName) {
            Column column = new Column(name, constraint, fieldName);
            this.columns.add(column);
            autoIncrementColumn = column;
            this.primaryColumns.add(column);
            return this;
        }

        private Builder columnPrimary(String name, String constraint, String fieldName) {
            Column column = new Column(name, constraint, fieldName);
            this.columns.add(column);
            this.withoutAutoIncrementColumns.add(column);
            this.primaryColumns.add(column);
            return this;
        }

        private Builder columnNormal(String name, String constraint, String fieldName) {
            Column column = new Column(name, constraint, fieldName);
            this.columns.add(column);
            this.withoutAutoIncrementColumns.add(column);
            return this;
        }

        public Builder realColumnPrimaryKey(String name) {
            return columnPrimary(name, "REAL PRIMARY KEY", name);
        }

        public Builder realColumnPrimaryKey(String name, String fieldName) {
            return columnPrimary(name, "REAL PRIMARY KEY", fieldName);
        }

        public Builder stringColumnPrimaryKey(String name) {
            return columnPrimary(name, "VARCHAR (300) PRIMARY KEY", name);
        }

        public Builder stringColumnPrimaryKey(String name, String fieldName) {
            return columnPrimary(name, "VARCHAR (300) PRIMARY KEY", fieldName);
        }

        public Builder intColumnPrimaryKey(String name) {
            return columnPrimary(name, "INTEGER PRIMARY KEY", name);
        }

        public Builder intColumnPrimaryKey(String name, String fieldName) {
            return columnPrimary(name, "INTEGER PRIMARY KEY", fieldName);
        }

        public Builder intColumnPrimaryKeyAutoIncrement(String name) {
            return columnPrimaryAutoIncrement(name, "INTEGER PRIMARY KEY AUTOINCREMENT", name);
        }

        public Builder intColumnPrimaryKeyAutoIncrement(String name, String fieldName) {
            return columnPrimaryAutoIncrement(name, "INTEGER PRIMARY KEY AUTOINCREMENT", fieldName);
        }

        public Builder intColumnDefault0(String name) {
            return intColumnDefault(name, 0);
        }

        public Builder intColumnDefault0(String name, String fieldName) {
            return intColumnDefault(name, 0, fieldName);
        }

        public Builder intColumnDefault(String name, int defaultValue) {
            return columnNormal(name, "INTEGER DEFAULT (" + defaultValue + ")", name);
        }

        public Builder intColumnDefault(String name, int defaultValue, String fieldName) {
            return columnNormal(name, "INTEGER DEFAULT (" + defaultValue + ")", fieldName);
        }

        public Builder intColumnDefault(String name, String defaultValue) {
            return columnNormal(name, "INTEGER DEFAULT (" + defaultValue + ")", name);
        }

        public Builder intColumnDefault(String name, String defaultValue, String fieldName) {
            return columnNormal(name, "INTEGER DEFAULT (" + defaultValue + ")", fieldName);
        }

        public Builder intColumn(String name) {
            return columnNormal(name, "INTEGER", name);
        }

        public Builder intColumn(String name, String fieldName) {
            return columnNormal(name, "INTEGER", fieldName);
        }

        public Builder intColumnNotNull(String name) {
            return columnNormal(name, "INTEGER NOT NULL", name);
        }

        public Builder intColumnNotNull(String name, String fieldName) {
            return columnNormal(name, "INTEGER NOT NULL", fieldName);
        }

        public Builder realColumn(String name) {
            return columnNormal(name, "REAL", name);
        }

        public Builder realColumn(String name, String fieldName) {
            return columnNormal(name, "REAL", fieldName);
        }

        public Builder realColumnNotNull(String name) {
            return columnNormal(name, "REAL NOT NULL", name);
        }

        public Builder realColumnNotNull(String name, String fieldName) {
            return columnNormal(name, "REAL NOT NULL", fieldName);
        }

        public Builder textColumn(String name) {
            return columnNormal(name, "TEXT", name);
        }

        public Builder textColumn(String name, String fieldName) {
            return columnNormal(name, "TEXT", fieldName);
        }

        public Builder stringColumn(String name, int maxLength) {
            return columnNormal(name, "VARCHAR (" + maxLength + ")", name);
        }

        public Builder stringColumn(String name, int maxLength, String fieldName) {
            return columnNormal(name, "VARCHAR (" + maxLength + ")", fieldName);
        }

        public Builder stringColumn(String name) {
            return columnNormal(name, "VARCHAR (300)", name);
        }

        public Builder stringColumn(String name, String fieldName) {
            return columnNormal(name, "VARCHAR (300)", fieldName);
        }

        public DbTableDefinition build() {
            return new DbTableDefinition(this);
        }
    }

    public static class Column {
        public String columnName;
        public String columnConstraint;
        public String fieldName;

        public Column(String columnName, String columnConstraint, String fieldName) {
            this.columnName = columnName;
            this.columnConstraint = columnConstraint;
            this.fieldName = fieldName;
        }
    }
}