package com.tsmile.easydb;

public interface ObjectConstructor<T> {

    /**
     * Returns a new instance.
     */
    public T construct();
}