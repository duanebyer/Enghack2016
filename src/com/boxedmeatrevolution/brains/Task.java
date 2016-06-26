package com.boxedmeatrevolution.brains;

import java.io.Serializable;

/**
 * Created by aidan on 2016-06-25.
 */
public abstract class Task implements Serializable {

    public Task() {
        _id = _nextId++;
    }

    public long getId() {
        return _id;
    }

    public abstract Serializable call();

    private static long _nextId;
    private long _id;

}
