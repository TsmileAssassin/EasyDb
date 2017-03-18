package com.tsmile.easydb;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 反射映射Entity和Object
 * Created by tsmile on 15/9/17.
 */
public class TypeAdapter<T> {
    private final ObjectConstructor<T> constructor;
    private final Map<String, BoundField> boundFields;

    private static HashMap<Class, WeakReference<TypeAdapter>>
            sTypeAdapterCache = new HashMap<>();

    private TypeAdapter(ObjectConstructor<T> constructor,
                        Map<String, BoundField> boundFields) {
        this.constructor = constructor;
        this.boundFields = boundFields;
    }

    public static <T> TypeAdapter<T> create(Class<? super T> raw) {
        if (!Object.class.isAssignableFrom(raw)) {
            return null; // it's a primitive!
        }

        WeakReference<TypeAdapter> cached = sTypeAdapterCache.get(raw);
        if (cached != null) {
            return cached.get();
        }
        TypeAdapter<T> output = new TypeAdapter<T>(
                newDefaultConstructor(raw), getBoundFields(raw));
        sTypeAdapterCache.put(raw, new WeakReference<TypeAdapter>(output));
        return output;
    }

    public void setToObject(T t, int value, DbTableDefinition.Column column) {
        String key = nameToKey(column.fieldName);
        BoundField field = boundFields.get(key);
        if (field != null) {
            try {
                if (field.type == int.class
                        || field.type == Integer.class) {
                    field.writeField(t, value);
                }
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }
    }

    public T mapToObject(Cursor cursor, List<DbTableDefinition.Column> columns) {
        T instance = constructor.construct();
        try {
            for (DbTableDefinition.Column column : columns) {
                String key = nameToKey(column.fieldName);
                String columnName = column.columnName;
                BoundField field = boundFields.get(key);
                if (field != null) {
                    int columnIndex = cursor.getColumnIndex(columnName);
                    if (columnIndex < 0) {
                        continue;
                    }
                    if (field.type == int.class
                            || field.type == Integer.class) {
                        field.writeField(instance, cursor.getInt(columnIndex));
                    } else if (field.type == String.class) {
                        field.writeField(instance, cursor.getString(columnIndex));
                    } else if (field.type == boolean.class
                            || field.type == Boolean.class) {
                        field.writeField(instance, 1 == cursor.getInt(columnIndex));
                    } else if (field.type == double.class
                            || field.type == Double.class) {
                        field.writeField(instance, cursor.getDouble(columnIndex));
                    } else if (field.type == long.class
                            || field.type == Long.class) {
                        field.writeField(instance, cursor.getLong(columnIndex));
                    } else if (field.type == float.class
                            || field.type == Float.class) {
                        field.writeField(instance, cursor.getFloat(columnIndex));
                    } else if (field.type == short.class
                            || field.type == Short.class) {
                        field.writeField(instance, cursor.getShort(columnIndex));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
        return instance;
    }

    public ContentValues mapToTable(T t, List<DbTableDefinition.Column> columns) {
        ContentValues values = new ContentValues();
        try {

            for (DbTableDefinition.Column column : columns) {
                String key = nameToKey(column.fieldName);
                if (TextUtils.isEmpty(key)) {
                    continue;
                }
                BoundField field = boundFields.get(key);
                if (field == null) {
                    continue;
                }
                if (field.type == boolean.class
                        || field.type == Boolean.class) {
                    values.put(column.columnName, (Boolean) (field.readField(t)) ? 1 : 0);
                } else {
                    Object fieldValue = field.readField(t);
                    if (fieldValue != null) {
                        values.put(column.columnName, String.valueOf(fieldValue));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        }
        return values;
    }

    private static <T> ObjectConstructor<T> newDefaultConstructor(Class<? super T> raw) {
        try {
            final Constructor<? super T> constructor = raw.getDeclaredConstructor();
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return new ObjectConstructor<T>() {
                @SuppressWarnings("unchecked") // T is the same raw type as is requested
                public T construct() {
                    try {
                        Object[] args = null;
                        return (T) constructor.newInstance(args);
                    } catch (InstantiationException e) {
                        // TODO: JsonParseException ?
                        throw new RuntimeException("Failed to invoke " + constructor + " with no args", e);
                    } catch (InvocationTargetException e) {
                        // TODO: don't wrap if cause is unchecked!
                        // TODO: JsonParseException ?
                        throw new RuntimeException("Failed to invoke " + constructor + " with no args",
                                e.getTargetException());
                    } catch (IllegalAccessException e) {
                        throw new AssertionError(e);
                    }
                }
            };
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static BoundField createBoundField(final Field field) {
        return new BoundField(field.getType()) {
            @Override
            void writeField(Object instance, Object value) throws IllegalAccessException {
                field.set(instance, value);
            }

            @Override
            Object readField(Object instance) throws IllegalAccessException {
                return field.get(instance);
            }
        };
    }

    private static Map<String, BoundField> getBoundFields(Class<?> raw) {
        Map<String, BoundField> result = new LinkedHashMap<>();
        if (raw.isInterface()) {
            return result;
        }

        Field[] fields = raw.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            BoundField boundField = createBoundField(field);
            result.put(nameToKey(field.getName()), boundField);
        }
        return result;
    }

    private static String nameToKey(String name) {
        if (TextUtils.isEmpty(name)) {
            return "";
        }
        return name.replaceAll("_", "").toLowerCase();
    }

    private static abstract class BoundField {
        Type type;

        protected BoundField(Type type) {
            this.type = type;
        }

        abstract void writeField(Object instance, Object value) throws IllegalAccessException;

        abstract Object readField(Object instance) throws IllegalAccessException;
    }
}
