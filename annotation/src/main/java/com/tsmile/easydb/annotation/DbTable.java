package com.tsmile.easydb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Created by tsmile
 */
@Retention(CLASS)
@Target(ElementType.TYPE)
public @interface DbTable {

    String tableName();

    String tableConstraint() default "";
}
