package com.boxedmeatrevolution.brains;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aidan on 2016-06-25.
 */
public abstract class Task implements Serializable {

    public Task() {
        _id = _nextId++;
        _listener = null;
    }

    public long getId() {
        return _id;
    }

    public List<Serializable> requestTasks(List<Task> tasks) throws IOException {
        return _listener.onTaskRequested(tasks);
    }

    public void registerListener(TaskRequestedEventListener listener) {
        _listener = listener;
    }

    public static interface TaskRequestedEventListener {
        List<Serializable> onTaskRequested(List<Task> task) throws IOException;
    }
    public abstract Serializable call() throws IOException;

    private static long _nextId;
    private long _id;

    private TaskRequestedEventListener _listener;

}
