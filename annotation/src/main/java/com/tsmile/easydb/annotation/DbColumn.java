package com.tsmile.easydb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Created by tsmile
 */
@Retention(CLASS)
@Target(ElementType.FIELD)
public @interface DbColumn {

    String DEFAULT_NONE = "!NONE!";
    String DEFAULT_NULL = "!NULL!";

    String name() default "";

    boolean notNull() default false;

    String defaultValue() default DEFAULT_NONE;

    int varcharLength() default 300;
}
