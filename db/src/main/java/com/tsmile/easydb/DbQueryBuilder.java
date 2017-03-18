package com.tsmile.easydb;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by tsmile on 15/9/25.
 */
public class DbQueryBuilder {

    private final HashMap<String, List<String>> tableColumns;
    private String selection;
    private String orderBy;
    private String groupBy;
    private String having;
    private boolean distinct;

    public DbQueryBuilder() {
        tableColumns = new HashMap<>();
    }

    public DbQueryBuilder tableColumns(String tableName) {
        tableColumns.put(tableName, new ArrayList<String>());
        return this;
    }

    public DbQueryBuilder tableColumns(String tableName,
                                       List<DbTableDefinition.Column> columns) {
        return tableColumns(tableName, columns, "");
    }

    public DbQueryBuilder tableColumns(String tableName,
                                       List<DbTableDefinition.Column> columns, String prefix) {
        List<String> stringColumns = new ArrayList<>(columns.size());
        for (int i = 0; i < columns.size(); i++) {
            StringBuilder columnNameBuilder = new StringBuilder();
            columnNameBuilder.append(tableName).append(".")
                    .append(columns.get(i).columnName);
            if (!TextUtils.isEmpty(prefix)) {
                columnNameBuilder.append(" as ")
                        .append(prefix)
                        .append(columns.get(i).columnName);

            }
            stringColumns.set(i, columnNameBuilder.toString());
        }
        tableColumns.put(tableName, stringColumns);
        return this;
    }

    public DbQueryBuilder where(String selection) {
        this.selection = selection;
        return this;
    }

    public DbQueryBuilder orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public DbQueryBuilder groupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public DbQueryBuilder having(String having) {
        this.having = having;
        return this;
    }

    public DbQueryBuilder distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public String build() {
        if (TextUtils.isEmpty(groupBy) && !TextUtils.isEmpty(having)) {
            throw new IllegalArgumentException(
                    "HAVING clauses are only permitted when using a groupBy clause");
        }
        if (tableColumns.size() == 0) {
            throw new IllegalArgumentException("tableColumns not be called");

        }

        StringBuilder query = new StringBuilder(120);

        query.append("SELECT ");
        if (distinct) {
            query.append("DISTINCT ");
        }

        StringBuilder columnsBuilder = new StringBuilder();
        Set<String> tables = tableColumns.keySet();
        for (String table : tables) {
            List<String> columns = tableColumns.get(table);
            if (columns.size() == 0) {
                columnsBuilder = new StringBuilder();
                columnsBuilder.append("* ");
                break;
            }
            appendColumns(columnsBuilder, columns);
        }

        query.append(columnsBuilder.toString());
        query.append("FROM ");
        appendColumns(query, tables);
        query.append(tables);
        appendClause(query, " WHERE ", selection);
        appendClause(query, " GROUP BY ", groupBy);
        appendClause(query, " HAVING ", having);
        appendClause(query, " ORDER BY ", orderBy);

        return query.toString();
    }

    public static void appendColumns(StringBuilder s, Collection<String> columns) {
        Iterator<String> iterator = columns.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            String column = iterator.next();
            if (column != null) {
                if (i > 0) {
                    s.append(", ");
                }
                s.append(column);
            }
            i++;
        }
        s.append(' ');
    }

    private static void appendClause(StringBuilder s, String name, String clause) {
        if (!TextUtils.isEmpty(clause)) {
            s.append(name);
            s.append(clause);
        }
    }
}
