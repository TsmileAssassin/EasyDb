package com.tsmile.easydb.processor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.tsmile.easydb.DbTableDefinition;
import com.tsmile.easydb.IDbTableDefinitionGenerator;

import java.util.ArrayList;
import java.util.List;


import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * 生成的数据表创建类模版
 * Created by tsmile on 16/6/26.
 */
final class TableClass {
    private final String classPackage;
    private final String className;
    String tableName;
    String tableConstraint;

    List<ColumnClass> columnClassList = new ArrayList<>();

    public TableClass(String classPackage, String className) {
        this.classPackage = classPackage;
        this.className = className;
    }

    public void addColumn(ColumnClass columnClass) {
        columnClassList.add(columnClass);
    }

    JavaFile brewJava() {
        TypeSpec.Builder result = TypeSpec.classBuilder(className)
                .addModifiers(PUBLIC);
        result.addSuperinterface(IDbTableDefinitionGenerator.class);
        result.addMethod(createMethod());
        return JavaFile.builder(classPackage, result.build())
                .addFileComment("Generated code from db annotation. Do not modify!")
                .build();
    }

    private MethodSpec createMethod() {
        MethodSpec.Builder result = MethodSpec.methodBuilder("db")
                .addAnnotation(Override.class)
                .returns(DbTableDefinition.class)
                .addModifiers(PUBLIC);
        StringBuilder statementBuilder = new StringBuilder();
        statementBuilder.append("return new DbTableDefinition.Builder()");
        statementBuilder.append(".tableName(\"").append(tableName).append("\")");
        for (ColumnClass columnClass : columnClassList) {
            if (columnClass.isPrimaryKey) {
                if (columnClass.isPrimaryAutoIncrement) {
                    statementBuilder.append(".intColumnPrimaryKeyAutoIncrement(\"").append(columnClass.name).
                            append("\"").append(",\"").append(columnClass.fieldName).append("\")");
                } else {
                    if (columnClass.type == ColumnClass.TYPE_STRING) {
                        statementBuilder.append(".stringColumnPrimaryKey(\"").append(columnClass.name).
                                append("\"").append(",\"").append(columnClass.fieldName).append("\")");
                    } else if (columnClass.type == ColumnClass.TYPE_REAL) {
                        statementBuilder.append(".realColumnPrimaryKey(\"").append(columnClass.name).
                                append("\"").append(",\"").append(columnClass.fieldName).append("\")");
                    } else {
                        statementBuilder.append(".intColumnPrimaryKey(\"").append(columnClass.name).
                                append("\"").append(",\"").append(columnClass.fieldName).append("\")");
                    }
                }
            } else {
                if (columnClass.type == ColumnClass.TYPE_STRING) {
                    statementBuilder.append(".stringColumn(\"").append(columnClass.name);
                    if (columnClass.typeLength > 0) {
                        statementBuilder.append("\",").append(columnClass.typeLength);
                    }
                    statementBuilder.append(",\"").append(columnClass.fieldName).append("\")");
                } else if (columnClass.type == ColumnClass.TYPE_INT) {
                    if (columnClass.defaultValue != null
                            && columnClass.defaultValue.length() > 0) {
                        statementBuilder.append(".intColumnDefault(\"").append(columnClass.name);
                        statementBuilder.append("\",\"").append(columnClass.defaultValue).
                                append("\"").append(",\"").append(columnClass.fieldName).append("\")");
                    } else if (columnClass.isNotNull) {
                        statementBuilder.append(".intColumnNotNull(\"").append(columnClass.name).
                                append("\"").append(",\"").append(columnClass.fieldName).append("\")");
                    } else {
                        statementBuilder.append(".intColumn(\"").append(columnClass.name).
                                append("\"").append(",\"").append(columnClass.fieldName).append("\")");
                    }
                } else {
                    if (columnClass.isNotNull) {
                        statementBuilder.append(".realColumnNotNull(\"").append(columnClass.name).
                                append("\"").append(",\"").append(columnClass.fieldName).append("\")");
                    } else {
                        statementBuilder.append(".realColumn(\"").append(columnClass.name).
                                append("\"").append(",\"").append(columnClass.fieldName).append("\")");
                    }
                }
            }
        }
        statementBuilder.append(".build()");
        result.addStatement(statementBuilder.toString());
        return result.build();
    }
}
