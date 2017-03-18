package com.tsmile.easydb.processor;

import com.google.auto.service.AutoService;
import com.tsmile.easydb.annotation.DbColumn;
import com.tsmile.easydb.annotation.DbPrimaryKey;
import com.tsmile.easydb.annotation.DbTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;


import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.WARNING;


/**
 * 解析数据库表生成注解
 * Created by tsmile on 16/2/26.
 */
@AutoService(Processor.class)
public class DbAnnotationProcessor extends AbstractProcessor {

    private static final String TABLE_CLASS_SUFFIX = "$$Table";

    private Set<String> supportedAnnotationTypes = new LinkedHashSet<>();
    private Elements elementUtils;
    private Filer filer;

    public DbAnnotationProcessor() {
        supportedAnnotationTypes.add(DbTable.class.getName());
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        elementUtils = env.getElementUtils();
        filer = env.getFiler();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return supportedAnnotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<TableClass> tableClassList = new ArrayList<>();
        for (TypeElement annotationType : annotations) {
            if (supportedAnnotationTypes.contains(annotationType.getQualifiedName().toString())) {
                for (Element element : roundEnv.getElementsAnnotatedWith(annotationType)) {
                    if (element.getKind() == ElementKind.CLASS) {
                        TypeElement typeElement = (TypeElement) element;
                        TableClass tableClass = constructClass(typeElement);
                        tableClassList.add(tableClass);
                    } else {
                        error(element, "Unexpected element type " + element.getKind());
                    }
                }
            } else {
                warn("Skipping unsupported annotation: " + annotationType);
            }
        }

        for (TableClass tableClass : tableClassList) {
            try {
                tableClass.brewJava().writeTo(filer);
            } catch (IOException e) {
                error("brew java fail:" + e.toString());
            }
        }
        return true;
    }

    private TableClass constructClass(TypeElement typeElement) {
        String classPackage = getPackageName(typeElement);
        String className = getClassName(typeElement, classPackage) + TABLE_CLASS_SUFFIX;
        TableClass tableClass = new TableClass(classPackage, className);
        DbTable tableAnnotation = typeElement.getAnnotation(DbTable.class);
        String tableName = tableAnnotation.tableName();
        if (isEmpty(tableName)) {
            tableName = className;
        }
        tableClass.tableName = tableName;
        tableClass.tableConstraint = tableAnnotation.tableConstraint();
        for (Element childElement : typeElement.getEnclosedElements()) {
            if (childElement instanceof VariableElement
                    && childElement.getAnnotation(DbColumn.class) != null) {
                DbColumn dbColumn = childElement.getAnnotation(DbColumn.class);
                ColumnClass columnClass = new ColumnClass();
                TypeMirror childElementType = childElement.asType();
                String mirrorString = childElementType.toString();
                String mirrorPackageName = getPackageFromFullyQualifiedName(mirrorString);
                String mirrorSimpleName = getSimpleNameFromFullyQualifiedName(mirrorString);
                if (isEmpty(mirrorPackageName)) {
                    if ("int".equals(mirrorSimpleName) || "short".equals(mirrorSimpleName)
                            || "long".equals(mirrorSimpleName)) {
                        columnClass.type = ColumnClass.TYPE_INT;
                    } else if ("boolean".equals(mirrorSimpleName)) {
                        columnClass.type = ColumnClass.TYPE_INT;
                        columnClass.defaultValue = "0";
                    } else if ("float".equals(mirrorSimpleName) || "double".equals(mirrorSimpleName)) {
                        columnClass.type = ColumnClass.TYPE_REAL;
                    } else {
                        error(childElement, "Column not support by us");
                        continue;
                    }
                } else if ("java.lang".equals(mirrorPackageName)) {
                    if ("String".equals(mirrorSimpleName)) {
                        columnClass.type = ColumnClass.TYPE_STRING;
                        columnClass.typeLength = dbColumn.varcharLength();
                    } else if ("Int".equals(mirrorSimpleName) || "Short".equals(mirrorSimpleName)
                            || "Long".equals(mirrorSimpleName)) {
                        columnClass.type = ColumnClass.TYPE_INT;
                    } else if ("Boolean".equals(mirrorSimpleName)) {
                        columnClass.type = ColumnClass.TYPE_INT;
                        columnClass.defaultValue = "0";
                    } else if ("Float".equals(mirrorSimpleName) || "Double".equals(mirrorSimpleName)) {
                        columnClass.type = ColumnClass.TYPE_REAL;
                    } else {
                        error(childElement, "Column not support by us");
                        continue;
                    }
                } else {
                    error(childElement, "Column not support by us");
                    continue;
                }

                String propertyName = childElement.getSimpleName().toString();
                String columnName = dbColumn.name();
                columnClass.name = isEmpty(columnName) ? propertyName : columnName;
                columnClass.fieldName = propertyName;
                if (columnClass.name.indexOf('$') >= 0) {
                    error(childElement, "Column names cannot contain the $ symbol");
                } else if (Character.isDigit(columnClass.name.charAt(0))) {
                    error(childElement, "Column names cannot begin with a digit");
                }

                columnClass.isNotNull = dbColumn.notNull();
                String defaultValue = dbColumn.defaultValue();
                if (!DbColumn.DEFAULT_NONE.equals(defaultValue)) {
                    columnClass.defaultValue = DbColumn.DEFAULT_NULL.equals(defaultValue)
                            ? "NULL" : defaultValue;
                }
                DbPrimaryKey dbPrimaryKey = childElement.getAnnotation(DbPrimaryKey.class);
                if (dbPrimaryKey != null) {
                    columnClass.isPrimaryKey = true;
                    columnClass.isPrimaryAutoIncrement = dbPrimaryKey.autoincrement();
                }
                tableClass.addColumn(columnClass);
            }
        }
        return tableClass;
    }

    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }

    private static String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
    }

    private void error(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(ERROR, message, element);
    }

    private void error(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(ERROR, message);
    }

    private void warn(String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(WARNING, message);
    }

    private static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String getPackageFromFullyQualifiedName(String name) {
        int split = getFQNSplitIndex(name);
        if (split < 0) {
            return "";
        }
        return name.substring(0, split);
    }

    public static String getSimpleNameFromFullyQualifiedName(String name) {
        int split = getFQNSplitIndex(name);
        if (split < 0) {
            return name;
        }
        return name.substring(split + 1);
    }

    private static int getFQNSplitIndex(String name) {
        return name.lastIndexOf('.');
    }
}
