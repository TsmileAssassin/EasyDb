package com.tsmile.easydb;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by tsmile on 16/6/26.
 */
public class DbTableGenerator {
    static final Map<Class<?>, IDbTableDefinitionGenerator> GENERATORS = new LinkedHashMap<>();

    public static DbTableDefinition generate(Class<?> targetClass) {
        try {
            IDbTableDefinitionGenerator generator = findForClass(targetClass);
            return generator.db();
        } catch (Exception e) {
            throw new RuntimeException("Unable to generate db for " + targetClass.getName(), e);
        }
    }

    public static IDbTableDefinitionGenerator findForClass(Class<?> cls) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        IDbTableDefinitionGenerator generator = GENERATORS.get(cls);
        if (generator != null) {
            return generator;
        }

        String clsName = cls.getName();
        Class<?> generatorClass = Class.forName(clsName + "$$Table");
        //noinspection unchecked
        generator = (IDbTableDefinitionGenerator) generatorClass.newInstance();
        GENERATORS.put(cls, generator);
        return generator;
    }
}
