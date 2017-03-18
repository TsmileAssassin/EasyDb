package com.tsmile.easydb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 基于definition的数据库
 * Created by tsmile on 15/9/16.
 */
public class DbDataBase extends DbSQLiteOpenHelper implements ReadWriteLock {

    DbDefinition dbDefinition;
    ReadWriteLock readWriteLock;

    public DbDataBase(String dbPath, DbDefinition dbDefinition) {
        super(dbPath, dbDefinition.name, null, dbDefinition.version);
        this.dbDefinition = dbDefinition;
        this.readWriteLock = new ReentrantReadWriteLock();
    }

    public DbDataBase(Context context, DbDefinition dbDefinition) {
        super(context.getApplicationContext(), dbDefinition.name, null, dbDefinition.version);
        this.dbDefinition = dbDefinition;
        this.readWriteLock = new ReentrantReadWriteLock();
    }

    public void init() {
        getWritableDatabase();
    }

    @Override
    public Lock readLock() {
        return this.readWriteLock.readLock();
    }

    @Override
    public Lock writeLock() {
        return this.readWriteLock.writeLock();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        List<String> creationSqls = this.dbDefinition.getCreationSqls();
        for (String sql : creationSqls) {
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.setVersion(newVersion);
        List<String> upgradeSqls = this.dbDefinition.getUpgradeSqls(oldVersion);
        List<String> createTableNameList = new ArrayList<>();
        for (String sql : upgradeSqls) {
            if (sql == null) {
                continue;
            }
            if (sql.startsWith("CREATE TABLE ")) {
                int startIndex = "CREATE TABLE ".length();
                int endIndex = sql.indexOf("(");
                if (endIndex > startIndex) {
                    createTableNameList.add(sql.substring(startIndex, endIndex).trim());
                }
                db.execSQL(sql);
            } else {
                boolean alterCreateTable = false;
                if (!createTableNameList.isEmpty()) {
                    for (String table : createTableNameList) {
                        if (sql.contains(table)) {
                            alterCreateTable = true;
                            break;
                        }
                    }
                }
                if (alterCreateTable) {
                    try {
                        db.execSQL(sql);
                    } catch (Throwable e) {
                        // ignore
                        Log.w("db-common", e);
                    }
                } else {
                    db.execSQL(sql);
                }
            }

        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // just not downgrade, and not set newVersion
    }

    public boolean beginTransactionLocked() {
        SQLiteDatabase db = null;
        try {
            writeLock().lock();
            db = getWritableDatabase();
            db.beginTransaction();
            return true;
        } catch (Exception e) {
            if (db != null) {
                try {
                    db.endTransaction();
                } catch (Exception e2) {
                    // ignore
                }
            }
            writeLock().unlock();
            return false;
        }
    }

    public void setTransactionSuccessful() {
        SQLiteDatabase db;
        try {
            db = getWritableDatabase();
            db.setTransactionSuccessful();
        } catch (Exception e) {
            //ignore
        }
    }

    public void endTransactionUnlocked() {
        SQLiteDatabase db;
        try {
            db = getWritableDatabase();
            db.endTransaction();
        } catch (Exception e) {
            //ignore
        } finally {
            writeLock().unlock();
        }
    }
}
