package com.tsmile.easydb;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * 封装针对一张表数据的操作
 * Created by tsmile on 15/9/16.
 */
public abstract class DbBaseTableModel extends DbTableModel {
    protected final DbTableDefinition dbTableDefinition;

    public DbBaseTableModel(DbTableDefinition dbTableDefinition,
                            DbDataBase dbDataBase) {
        super(dbDataBase);
        this.dbTableDefinition = dbTableDefinition;
    }

    public boolean deleteAll() {
        return delete(null, new String[]{});
    }

    public boolean delete(String whereClause, String[] whereArgs) {
        try {
            dbDataBase.writeLock().lock();
            SQLiteDatabase db = dbDataBase.getWritableDatabase();
            if (db == null) {
                return false;
            }
            db.delete(dbTableDefinition.tableName, whereClause, whereArgs);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbDataBase.writeLock().unlock();
        }
        return false;
    }

    public boolean update(ContentValues values, String whereClause, String[] whereArgs) {
        try {
            dbDataBase.writeLock().lock();
            SQLiteDatabase db = dbDataBase.getWritableDatabase();
            if (db == null) {
                return false;
            }
            db.update(dbTableDefinition.tableName, values,
                    whereClause, whereArgs);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbDataBase.writeLock().unlock();
        }
        return false;
    }

    public boolean bulkUpdate(ContentValues[] valuesArray, String[] whereClauseArray,
                              List<String[]> whereArgsArray) {
        SQLiteDatabase db = null;
        try {
            dbDataBase.writeLock().lock();
            db = dbDataBase.getWritableDatabase();
            if (db == null) {
                return false;
            }
            db.beginTransaction();
            for (int i = 0; i < valuesArray.length; i++) {
                db.update(dbTableDefinition.tableName, valuesArray[i],
                        whereClauseArray[i], whereArgsArray.get(i));
            }
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.endTransaction();
                } catch (Exception e) {
                    // ignore
                }
            }
            dbDataBase.writeLock().unlock();
        }
        return false;
    }

    public int insert(ContentValues values) {
        try {
            dbDataBase.writeLock().lock();
            SQLiteDatabase db = dbDataBase.getWritableDatabase();
            if (db == null) {
                return -1;
            }
            return Long.valueOf(db.insert(dbTableDefinition.tableName,
                    null, values)).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbDataBase.writeLock().unlock();
        }
        return -1;
    }

    public List<Integer> bulkInsert(ContentValues[] valuesArray) {
        SQLiteDatabase db = null;
        List<Integer> rowIds = new ArrayList<>();
        try {
            dbDataBase.writeLock().lock();
            db = dbDataBase.getWritableDatabase();
            if (db == null) {
                return rowIds;
            }
            db.beginTransaction();
            for (ContentValues values : valuesArray) {
                rowIds.add(Long.valueOf(db.insert(dbTableDefinition.tableName,
                        null, values)).intValue());
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.endTransaction();
                } catch (Exception e) {
                    // ignore
                }
            }
            dbDataBase.writeLock().unlock();
        }
        return rowIds;
    }

    public List<Integer> clearThenBulkInsert(ContentValues[] valuesArray) {
        SQLiteDatabase db = null;
        List<Integer> rowIds = new ArrayList<>();
        try {
            dbDataBase.writeLock().lock();
            db = dbDataBase.getWritableDatabase();
            if (db == null) {
                return rowIds;
            }
            db.beginTransaction();
            db.delete(dbTableDefinition.tableName, null, new String[]{});
            for (ContentValues values : valuesArray) {
                rowIds.add(Long.valueOf(db.insert(dbTableDefinition.tableName,
                        null, values)).intValue());
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                try {
                    db.endTransaction();
                } catch (Exception e) {
                    // ignore
                }
            }
            dbDataBase.writeLock().unlock();
        }
        return rowIds;
    }

    public boolean bulkUpdateInTransaction(ContentValues[] valuesArray, String[] whereClauseArray,
                                           List<String[]> whereArgsArray) {
        SQLiteDatabase db = null;
        try {
            db = dbDataBase.getWritableDatabase();
            if (db == null) {
                return false;
            }
            for (int i = 0; i < valuesArray.length; i++) {
                db.update(dbTableDefinition.tableName, valuesArray[i],
                        whereClauseArray[i], whereArgsArray.get(i));
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Integer> bulkInsertInTransaction(ContentValues[] valuesArray) {
        SQLiteDatabase db = null;
        List<Integer> rowIds = new ArrayList<>();
        try {
            db = dbDataBase.getWritableDatabase();
            if (db == null) {
                return rowIds;
            }
            for (ContentValues values : valuesArray) {
                rowIds.add(Long.valueOf(db.insert(dbTableDefinition.tableName,
                        null, values)).intValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowIds;
    }

    public List<Integer> clearThenBulkInsertInTransaction(ContentValues[] valuesArray) {
        SQLiteDatabase db;
        List<Integer> rowIds = new ArrayList<>();
        try {
            db = dbDataBase.getWritableDatabase();
            if (db == null) {
                return rowIds;
            }
            db.delete(dbTableDefinition.tableName, null, new String[]{});
            for (ContentValues values : valuesArray) {
                rowIds.add(Long.valueOf(db.insert(dbTableDefinition.tableName,
                        null, values)).intValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowIds;
    }
}
