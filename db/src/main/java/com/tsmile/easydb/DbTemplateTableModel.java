package com.tsmile.easydb;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * 封装针对一张表数据的操作,简单实现ORM
 *
 * Created by tsmile on 15/9/16.
 */
public class DbTemplateTableModel<T> extends DbBaseTableModel {

    protected TypeAdapter<T> typeAdapter;

    public DbTemplateTableModel(DbTableDefinition dbTableDefinition,
                                DbDataBase dbDataBase, Class<? super T> raw) {
        super(dbTableDefinition, dbDataBase);
        typeAdapter = TypeAdapter.create(raw);
    }

    public boolean delete(T t) {
        ContentValues primaryValues = typeAdapter.
                mapToTable(t, dbTableDefinition.primaryColumns);
        StringBuilder where = new StringBuilder();
        String[] whereArgs = new String[primaryValues.size()];
        constructWhere(primaryValues, where, whereArgs);
        return delete(where.toString(), whereArgs);
    }

    public boolean update(T t) {
        ContentValues primaryValues = typeAdapter.
                mapToTable(t, dbTableDefinition.primaryColumns);
        StringBuilder where = new StringBuilder();
        String[] whereArgs = new String[primaryValues.size()];
        constructWhere(primaryValues, where, whereArgs);
        return update(typeAdapter.mapToTable(t, dbTableDefinition.withoutAutoIncrementColumns),
                where.toString(), whereArgs);
    }

    public boolean bulkUpdate(List<T> tList) {
        if (tList == null || tList.size() == 0) {
            return false;
        }
        ContentValues[] valuesArray = new ContentValues[tList.size()];
        String[] whereArray = new String[tList.size()];
        List<String[]> whereArgsArray = new ArrayList<>(tList.size());
        changeUpdateListToContentValues(tList, valuesArray, whereArray, whereArgsArray);
        return bulkUpdate(valuesArray, whereArray, whereArgsArray);
    }

    public boolean bulkUpdateInTransaction(List<T> tList) {
        if (tList == null || tList.size() == 0) {
            return false;
        }
        ContentValues[] valuesArray = new ContentValues[tList.size()];
        String[] whereArray = new String[tList.size()];
        List<String[]> whereArgsArray = new ArrayList<>(tList.size());
        changeUpdateListToContentValues(tList, valuesArray, whereArray, whereArgsArray);
        return bulkUpdateInTransaction(valuesArray, whereArray, whereArgsArray);
    }

    public Integer insert(T t) {
        int rowId = insert(typeAdapter.mapToTable(t,
                dbTableDefinition.withoutAutoIncrementColumns));
        if (rowId >= 0
                && dbTableDefinition.autoIncrementColumn != null) {
            typeAdapter.setToObject(t, Long.valueOf(rowId).intValue(),
                    dbTableDefinition.autoIncrementColumn);
        }
        return rowId;
    }

    public List<Integer> bulkInsert(List<T> tList) {
        if (tList == null || tList.size() == 0) {
            return new ArrayList<>();
        }
        return bulkInsert(changeInsertListToContentValues(tList));
    }

    public List<Integer> bulkInsertInTransaction(List<T> tList) {
        if (tList == null || tList.size() == 0) {
            return new ArrayList<>();
        }
        return bulkInsertInTransaction(changeInsertListToContentValues(tList));
    }

    public List<Integer> clearThenBulkInsert(List<T> tList) {
        if (tList == null || tList.size() == 0) {
            return new ArrayList<>();
        }
        return clearThenBulkInsert(changeInsertListToContentValues(tList));
    }

    public List<Integer> clearThenBulkInsertInTransaction(List<T> tList) {
        if (tList == null || tList.size() == 0) {
            return new ArrayList<>();
        }
        return clearThenBulkInsertInTransaction(changeInsertListToContentValues(tList));
    }

    private ContentValues[] changeInsertListToContentValues(List<T> tList) {
        ContentValues[] valuesArray = new ContentValues[tList.size()];
        for (int i = 0; i < tList.size(); i++) {
            valuesArray[i] = typeAdapter.mapToTable(tList.get(i),
                    dbTableDefinition.withoutAutoIncrementColumns);
        }
        return valuesArray;
    }

    private void changeUpdateListToContentValues(List<T> tList, ContentValues[] valuesArray,
                                                 String[] whereArray, List<String[]> whereArgsArray) {
        for (int i = 0; i < tList.size(); i++) {
            T t = tList.get(i);
            ContentValues primaryValues = typeAdapter.
                    mapToTable(t, dbTableDefinition.primaryColumns);
            StringBuilder where = new StringBuilder();
            String[] whereArgs = new String[primaryValues.size()];
            constructWhere(primaryValues, where, whereArgs);
            valuesArray[i] = typeAdapter.mapToTable(t, dbTableDefinition.withoutAutoIncrementColumns);
            whereArray[i] = where.toString();
            whereArgsArray.add(i, whereArgs);
        }
    }

    public List<T> getAll() {
        return get(null, new String[]{}, null, null, null);
    }

    public List<T> get(String selection,
                       String[] selectionArgs, String orderBy) {
        return get(selection, selectionArgs, null, null, orderBy);
    }

    public List<T> get(String selection,
                       String[] selectionArgs, String groupBy, String having,
                       String orderBy) {
        List<T> dataList = new ArrayList<>();
        Cursor cursor = null;
        try {
            dbDataBase.readLock().lock();
            SQLiteDatabase db = dbDataBase.getReadableDatabase();
            if (db == null) {
                return dataList;
            }
            cursor = db.query(dbTableDefinition.tableName, null, selection,
                    selectionArgs, groupBy, having, orderBy);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    dataList.add(typeAdapter.mapToObject(cursor, dbTableDefinition.columns));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbDataBase.readLock().unlock();
            if (cursor != null) {
                cursor.close();
            }
        }
        return dataList;
    }
}